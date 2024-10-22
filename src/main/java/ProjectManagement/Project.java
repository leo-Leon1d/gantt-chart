package ProjectManagement;

import CalendarManagement.Calendar;
import ResourceManagement.Resource;
import TaskManagement.Task;
import TaskManagement.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

// Класс диаграммы
@Getter
@Setter
public class Project {

    String name;
    private List<Task> tasks;
    private List<Resource> resources;
    private Calendar calendar;

    // Конструктор
    public Project(String name, Calendar calendar) {
        this.name = name;
        this.calendar = calendar;
        tasks = new ArrayList<>();
        resources = new ArrayList<>();
    }

    // Получение сортированных заданий
    public List<Task> getSortedTasks() {
        List<Task> sortedTasks = new ArrayList<>();
        List<Task> prioritySorter = new ArrayList<>();
        Map<Task, Integer> taskDepCount = new HashMap<>(); // Количество зависимостей для каждой задачи
        Queue<Task> readyTasks = new LinkedList<>();

        // Инициализируем количество зависимостей для каждой задачи
        for (Task task : tasks) {
            taskDepCount.put(task, task.getDependencies().size());
            if (task.getDependencies().isEmpty()) {
                readyTasks.add(task);
            }
        }

        while (!readyTasks.isEmpty()) {
            Task currentTask = readyTasks.poll();
            sortedTasks.add(currentTask);

            for (Task subTask : currentTask.getSubTasks()) {
                taskDepCount.put(subTask, taskDepCount.get(subTask) - 1);
                if (taskDepCount.get(subTask) == 0) {
                    prioritySorter.add(subTask);
                }
            }

            if (prioritySorter.size() == 1) {
                readyTasks.add(prioritySorter.get(0));
            } else if (prioritySorter.size() > 1) {
                // Сортировка задач по приоритету от наибольшего к наименьшему
                prioritySorter.sort((task1, task2) -> Integer.compare(task2.getPriority(), task1.getPriority()));

                // Добавление уже отсортированных по приоритету задач в очередь readyTasks
                readyTasks.addAll(prioritySorter);
            }

            // Очищаем prioritySorter для следующего уровня
            prioritySorter.clear();
        }

        // Проверка на наличие циклов
        if (sortedTasks.size() != tasks.size()) {
            throw new IllegalStateException("There is a cycle in the tasks!");
        }

        return sortedTasks;
    }



    // Добавление задачи
    public void addTask(Task task) {
        tasks.add(task);
    }

    // Добавление исполнителя
    public void addResource(Resource resource) {
        resources.add(resource);
    }

    // Рассчитать длину проекта
    public Duration calculateProjectDuration() {
        if (tasks.isEmpty()) {
            return Duration.ZERO;
        }

        // Самая ранняя дата начала задачи
        LocalDateTime projectStartDate = tasks.stream()
                .map(task -> task.getStatus() == TaskStatus.COMPLETED ? task.getFactualStartDate() : task.getEstimatedStartDate())
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        // Самая поздняя дата окончания задачи
        LocalDateTime projectEndDate = tasks.stream()
                .map(task -> task.getStatus() == TaskStatus.COMPLETED ? task.getFactualEndDate() : task.getEstimatedEndDate())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        // Разница между началом и концом проекта
        return Duration.between(projectStartDate, projectEndDate);
    }

    // Пересчёт расписания проекта
    public void recalculateProjectSchedule() {
        Set<Task> updatedTasks = new HashSet<>();

        // Пересчитываем только те задачи, которые нуждаются в пересчете
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.NOT_STARTED) {

                // Дата начала задачи на основе её зависимостей
                LocalDateTime startDate = calculateStartDateForTask(task);
                if (!startDate.equals(task.getEstimatedStartDate())) {
                    task.setEstimatedStartDate(startDate);
                    updatedTasks.add(task);
                }

                // Дата окончания задачи
                LocalDateTime endDate = task.calculateEndDate(startDate, task.getEstimatedDuration(), calendar, task.getAssignedResource().getCalendar());
                if (!endDate.equals(task.getEstimatedEndDate())) {
                    task.setEstimatedEndDate(endDate);
                    updatedTasks.add(task);
                }
            }
        }

        // Пересчет зависимостей и подзадач
        for (Task updatedTask : updatedTasks) {
            updateDependentTasks(updatedTask);
            recalculateSubTasks(updatedTask);
        }
    }

    // Дата начала задачи на основе зависимостей
    private static LocalDateTime calculateStartDateForTask(Task task) {
        LocalDateTime earliestStartDate = task.getEstimatedStartDate(); // Начальное значение для даты начала задачи

        // Проверяем зависимости задачи
        for (Task dependency : task.getDependencies()) {
            if (dependency.getEstimatedEndDate() != null && dependency.getEstimatedEndDate().isAfter(earliestStartDate)) {
                // Если зависимость заканчивается позже, то задача может начинаться только после неё
                earliestStartDate = dependency.getEstimatedEndDate();
            }
        }

        return earliestStartDate;
    }

    // Пересчет зависимые задачи
    private void updateDependentTasks(Task task) {
        for (Task dependentTask : task.getSubTasks()) {
            // Пересчитываем дату начала зависимой задачи
            LocalDateTime newStartDate = calculateStartDateForTask(dependentTask);
            if (!newStartDate.equals(dependentTask.getEstimatedStartDate())) {
                dependentTask.setEstimatedStartDate(newStartDate);
            }

            // Пересчет даты окончания зависимой задачи
            LocalDateTime newEndDate = dependentTask.calculateEndDate(newStartDate, dependentTask.getEstimatedDuration(), calendar, dependentTask.getAssignedResource().getCalendar());
            if (!newEndDate.equals(dependentTask.getEstimatedEndDate())) {
                dependentTask.setEstimatedEndDate(newEndDate);
            }
        }
    }

    // Пересчет подзадач для каждой задачи
    private void recalculateSubTasks(Task task) {
        for (Task subTask : task.getSubTasks()) {
            LocalDateTime newStartDate = calculateStartDateForTask(subTask);
            if (!newStartDate.equals(subTask.getEstimatedStartDate())) {
                subTask.setEstimatedStartDate(newStartDate);
            }

            LocalDateTime newEndDate = subTask.calculateEndDate(newStartDate, subTask.getEstimatedDuration(), calendar, subTask.getAssignedResource().getCalendar());
            if (!newEndDate.equals(subTask.getEstimatedEndDate())) {
                subTask.setEstimatedEndDate(newEndDate);
            }
        }
    }

/*
    // Рекурсивная функция для топологической сортировки
    private void topologicalSort(Task task, Set<Task> visited, Set<Task> visiting, List<Task> sortedTasks) {
        if (visited.contains(task)) {
            return;
        }
        if (visiting.contains(task)) {
            throw new IllegalStateException("Loop detected in task dependencies: " + task.getName());
        }
        visiting.add(task);

        for (Task dependency : task.getDependencies()) {
            topologicalSort(dependency, visited, visiting, sortedTasks);
        }
        visiting.remove(task);
        visited.add(task);
        sortedTasks.add(task);
    }

 */


    // Получения следующей задачи для ресурса
    public Task getNextTaskForResource(Resource resource) {
        return tasks.stream()
                .filter(task -> resource.equals(task.getAssignedResource()) && task.canStart())
                .min(Comparator.comparing(Task::hasUnresolvedDependencies)
                .thenComparing(Comparator.comparing(Task::getPriority).reversed()))
                .orElse(null);
    }

    // Отмена задания
    public void cancelTask(Task task) {
        task.setStatus(TaskStatus.CANCELLED);
        this.recalculateProjectSchedule();
    }

    // Замена исполнителя
    public void changeResource(Task task, Resource newResource) {
        task.setAssignedResource(newResource);
        this.recalculateProjectSchedule();
    }

    // Представить задачи проетка в виде построчной иерархии (в консоли)
    public void displayTasksHierarchy(Task task, int level) {
        String indent = " ".repeat(level * 4);
        System.out.println(indent + task.getName());
        for (Task subTask : task.getSubTasks()) {
            displayTasksHierarchy(subTask, level + 1);
        }
    }

    // Вывести задачи проекта (в консоль)
    public void showTasks() {
        System.out.println("Проект: " + name);
        for (Task task : tasks) {
            System.out.println(task);
        }
    }

    // Вывести исполнителей проекта (в консоль)
    public void showDoers() {
        System.out.println("Проект: " + name);
        for (Resource resource : resources) {
            System.out.println(resource);
        }
    }
}

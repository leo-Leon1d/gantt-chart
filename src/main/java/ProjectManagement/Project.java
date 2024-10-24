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
    private Calendar projectCalendar;

    // Конструктор
    public Project(String name, Calendar projectCalendar) {
        this.name = name;
        this.projectCalendar = projectCalendar;
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


    // Расчет оценочной длительности проекта
    public Duration calculateProjectEstimatedDuration() {
        if (tasks.isEmpty()) {
            return Duration.ZERO;
        }

        // Самая ранняя оценочная дата начала задачи
        LocalDateTime estimatedStartDate = tasks.stream()
                .map(Task::getEstimatedStartDate)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // Самая поздняя оценочная дата окончания задачи
        LocalDateTime estimatedEndDate = tasks.stream()
                .map(Task::getEstimatedEndDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (estimatedStartDate == null || estimatedEndDate == null) {
            throw new IllegalStateException("Недостаточно данных для расчёта оценочной длительности проекта.");
        }

        // Возвращаем разницу между оценочной датой начала и окончания
        return Duration.between(estimatedStartDate, estimatedEndDate);
    }


    // Расчет фактической длительности проекта (только при его завершенности)
    public Duration calculateProjectFactualDuration() {
        if (tasks.isEmpty()) {
            return Duration.ZERO;
        }

        boolean allTasksCompleted = tasks.stream()
                .allMatch(task -> task.getStatus() == TaskStatus.COMPLETED);

        if (!allTasksCompleted) {
            throw new IllegalStateException("Проект не завершён. Невозможно рассчитать фактическую длительность.");
        }

        // Самая ранняя фактическая дата начала проекта
        LocalDateTime factualStartDate = tasks.stream()
                .map(Task::getFactualStartDate)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // Самая поздняя фактическая дата окончания проекта
        LocalDateTime factualEndDate = tasks.stream()
                .map(Task::getFactualEndDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (factualStartDate == null || factualEndDate == null) {
            throw new IllegalStateException("Недостаточно данных для расчёта фактической длительности проекта.");
        }

        return Duration.between(factualStartDate, factualEndDate);
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
                LocalDateTime endDate = task.calculateEndDate(startDate, task.getEstimatedDuration(), projectCalendar, task.getAssignedResource().getResourceCalendar());
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
            LocalDateTime newEndDate = dependentTask.calculateEndDate(newStartDate, dependentTask.getEstimatedDuration(), projectCalendar, dependentTask.getAssignedResource().getResourceCalendar());
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

            LocalDateTime newEndDate = subTask.calculateEndDate(newStartDate, subTask.getEstimatedDuration(), projectCalendar, subTask.getAssignedResource().getResourceCalendar());
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

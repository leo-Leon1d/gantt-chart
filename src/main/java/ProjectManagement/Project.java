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
    private LocalDateTime estimatedStartDate;
    private LocalDateTime factualStartDate;
    private LocalDateTime estimatedEndDate;
    private LocalDateTime factualEndDate;

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
                prioritySorter.sort(Comparator.comparingInt(Task::getPriority));

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

    // Расчет расписания
    public void calculateSchedule() {
        if (estimatedStartDate == null) {
            throw new IllegalStateException("Project start date must be set before calculating the schedule.");
        }

        // Сортируем задачи с учетом зависимостей
        List<Task> sortedTasks = getSortedTasks();

        // Инициализация времени доступности исполнителей
        Map<Resource, LocalDateTime> resourceAvailability = new HashMap<>();
        for (Resource resource : resources) {
            resourceAvailability.put(resource, estimatedStartDate);
        }

        // Расчет расписания задач
        for (Task task : sortedTasks) {
            Resource assignedResource = task.getAssignedResource();
            if (assignedResource == null) {
                throw new IllegalStateException("Task '" + task.getName() + "' has no assigned resource.");
            }

            // Определяем, когда можно начать задачу
            LocalDateTime taskStartDate = calculateStartDateForTask(task, resourceAvailability);

            // Учитываем длительность задачи
            LocalDateTime taskEndDate = calculateTaskEndDate(taskStartDate, task.getEstimatedDuration(), assignedResource.getResourceCalendar());

            // Устанавливаем рассчитанные даты
            task.setEstimatedStartDate(taskStartDate);
            task.setEstimatedEndDate(taskEndDate);

            // Обновляем время доступности исполнителя
            resourceAvailability.put(assignedResource, taskEndDate);
        }
    }

    // Расчет даты начала задачи
    private LocalDateTime calculateStartDateForTask(Task task, Map<Resource, LocalDateTime> resourceAvailability) {
        LocalDateTime earliestStart = estimatedStartDate;

        // Учитываем зависимости
        for (Task dependency : task.getDependencies()) {
            if (dependency.getEstimatedEndDate() != null && dependency.getEstimatedEndDate().isAfter(earliestStart)) {
                earliestStart = dependency.getEstimatedEndDate();
            }
        }

        // Учитываем доступность исполнителя
        Resource assignedResource = task.getAssignedResource();
        LocalDateTime resourceAvailable = resourceAvailability.getOrDefault(assignedResource, estimatedStartDate);
        if (resourceAvailable.isAfter(earliestStart)) {
            earliestStart = resourceAvailable;
        }

        // Учитываем календарь проекта
        return projectCalendar.getNextWorkingTime(earliestStart);
    }

    // Расчет даты конца задачи
    private LocalDateTime calculateTaskEndDate(LocalDateTime startDate, Duration duration, Calendar resourceCalendar) {
        LocalDateTime currentDate = startDate;
        long remainingHours = duration.toHours();

        while (remainingHours > 0) {
            // Проверяем, является ли текущий день рабочим
            if (resourceCalendar.isWorkDay(currentDate.toLocalDate())) {
                // Сколько часов можно использовать в текущий рабочий день
                long availableHours = resourceCalendar.workHoursLeftForDay(currentDate.toLocalDate(), currentDate);
                if (remainingHours <= availableHours) {
                    return currentDate.plusHours(remainingHours);
                } else {
                    remainingHours -= availableHours;
                    currentDate = currentDate.plusDays(1).withHour(resourceCalendar.getStartHour()).withMinute(0);
                }
            } else {
                // Если день нерабочий, переходим к следующему
                currentDate = currentDate.plusDays(1).withHour(resourceCalendar.getStartHour()).withMinute(0);
            }
        }

        return currentDate;
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
        LocalDateTime startDateCalc = this.getSortedTasks().get(0).getEstimatedStartDate();

        // Самая поздняя оценочная дата окончания задачи
        LocalDateTime endDateCalc = this.getSortedTasks().get(this.getSortedTasks().size()-1).getEstimatedEndDate();

        if (startDateCalc == null || endDateCalc == null) {
            throw new IllegalStateException("Недостаточно данных для расчёта оценочной длительности проекта.");
        }

        // Возвращаем разницу между оценочной датой начала и окончания
        return Duration.between(startDateCalc, endDateCalc);
    }


    // Расчет фактической длительности проекта (только при завершенности)
    public Duration calculateProjectFactualDuration() {
        if (tasks.isEmpty()) {
            return Duration.ZERO;
        }

        boolean allTasksCompleted = tasks.stream()
                .allMatch(task -> task.getStatus() == TaskStatus.COMPLETED);

        if (!allTasksCompleted) {
            throw new IllegalStateException("Проект не завершён. Невозможно рассчитать фактическую длительность.");
        }

        // Самая ранняя фактическая дата начала задачи
        LocalDateTime startDateCalc = this.getSortedTasks().get(0).getFactualStartDate();

        // Самая поздняя фактическая дата окончания задачи
        LocalDateTime endDateCalc = this.getSortedTasks().get(this.getSortedTasks().size()-1).getFactualEndDate();

        if (startDateCalc == null || endDateCalc == null) {
            throw new IllegalStateException("Недостаточно данных для расчёта фактической длительности проекта.");
        }

        return Duration.between(startDateCalc, endDateCalc);
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

    // Пересчет зависимых задач
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


    // Получение следующей задачи для ресурса
    public Task getNextTaskForResource(Resource resource) {
        return tasks.stream()
                .filter(task -> resource.equals(task.getAssignedResource()) && task.canStart())
                .min(Comparator.comparing(Task::hasUnresolvedDependencies)
                .thenComparing(Comparator.comparing(Task::getPriority).reversed()))
                .orElse(null);
    }

    // Отмена задачи
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

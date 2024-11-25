package TaskManagement;

import CalendarManagement.Calendar;
import ResourceManagement.Resource;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


// Класс здаачи
@Getter
@Setter
public class Task {

    private Long id;
    private String name;

    // Свойства сроков
    private Duration estimatedDuration;
    private LocalDateTime estimatedStartDate;
    private LocalDateTime factualStartDate;
    private LocalDateTime factualEndDate;
    private Duration factualDuration;
    private LocalDateTime estimatedEndDate;

    // Свойства зависимостей
    private Resource assignedResource;
    private List<Task> dependencies;
    private List<Task> subTasks;
    private TaskStatus status;;

    // Свойства паузы
    private LocalDateTime pauseStartTime;
    private Duration totalPauseDuration = Duration.ZERO;

    // Календарь
    private Calendar calendar;
    private Calendar resourceCalendar;

    // Приоритет (по умолчанию 50, от 1 до 100)
    private int priority;

    // Конструктор
    public Task(String name, Duration estimatedDuration) {
        this.name = name;
        this.estimatedDuration = estimatedDuration;
        this.estimatedEndDate = calculateEndDate(estimatedStartDate, estimatedDuration, calendar, resourceCalendar);
        this.status = TaskStatus.NOT_STARTED;
        this.dependencies = new ArrayList<>();
        this.subTasks = new ArrayList<>();
        this.priority = 50;
    }

    // Возможно ли начало выполнения задачи
    public boolean canStart() {
        if (this.status == TaskStatus.NOT_STARTED) {
            for (Task dependency : dependencies) {
                if (dependency.getStatus() != TaskStatus.COMPLETED ) {
                    return false;
                }
            }
            return assignedResource != null;
        }
        return false;
    }

    // Добавление "нижней" задачи
    public void addSubTask(Task subTask) {
        if (this == subTask || subTask.getDependencies().contains(this)) {
            throw new IllegalArgumentException("Unable to add a subtask due to loop creation");
        }
        this.subTasks.add(subTask);
        subTask.dependencies.add(this);
    }

    // Добавление списка "нижних" задач
    public void addSubTasks(List<Task> subTasksList) {
        for (Task subTask : subTasksList) {
            if (this == subTask || subTask.getDependencies().contains(this)) {
                throw new IllegalArgumentException("Unable to add a subtask due to loop creation");
            }
            this.subTasks.add(subTask);
            subTask.dependencies.add(this);
        }
    }

    // Добавление "верхней" задачи
    public void addDependentTask(Task dependentTask) {
        if (this == dependentTask || dependentTask.getSubTasks().contains(this)) {
            throw new IllegalArgumentException("Unable to add a dependency due to loop creation");
        }
        this.dependencies.add(dependentTask);
        dependentTask.subTasks.add(this);
    }

    // Добавление списка "верхних" задач
    public void addDependentTasks(List<Task> dependentTasksList) {
        for (Task dependentTask : dependentTasksList) {
            if (this == dependentTask || dependentTask.getSubTasks().contains(this)) {
                throw new IllegalArgumentException("Unable to add a dependency due to loop creation");
            }
            this.dependencies.add(dependentTask);
            dependentTask.subTasks.add(this);
        }
    }


    // Метод для проверки создания цикла
    private boolean createsCycle(Task parentTask, Task subTask) {
        if (subTask == parentTask) return true;
        for (Task dependency : subTask.getDependencies()) {
            if (createsCycle(parentTask, dependency)) {
                return true;
            }
        }
        return false;
    }

    // Начало задачи
    public void startTask() {
        if (canStart()) {
            this.status = TaskStatus.IN_PROGRESS;
            this.factualStartDate = LocalDateTime.now();
            System.out.println("Зависимости задачи: \n");
            for(Task dependency : this.dependencies) {
                System.out.println(dependency.name + "-" + dependency.getStatus().toString());
            }
            System.out.println("Задача " + name + " начата.");
        } else {
            System.out.println("Невозможно начать задачу " + name + ". Зависимые задачи не завершены.");
        }
    }

    // Переключение ПАУЗА-ПРОГРЕСС
    public void togglePauseTask() {
        if(this.status==TaskStatus.IN_PROGRESS) {
            this.status = TaskStatus.PAUSED;
            pauseStartTime = LocalDateTime.now();
            System.out.println("Задача " + name + " приостановлена.");
        } else if(this.status==TaskStatus.PAUSED) {
            this.status = TaskStatus.IN_PROGRESS;
            Duration pauseDuration = Duration.between(pauseStartTime, LocalDateTime.now());
            totalPauseDuration = totalPauseDuration.plus(pauseDuration);
            System.out.println("Задача " + name + " возобновлена.");
        }
    }

    // Завершение задачи
    public void completeTask() {
        if (this.status == TaskStatus.IN_PROGRESS) {
            this.status = TaskStatus.COMPLETED;
            this.factualEndDate = LocalDateTime.now();
            this.factualDuration = Duration.between(factualStartDate, LocalDateTime.now())
                    .minus(totalPauseDuration);
            System.out.println("Задача " + name + " завершена. " +
                    "Фактическая продолжительность: " + factualDuration +
                    "   Длительность перерывов: " + totalPauseDuration +
                    "   Время окончания задачи: " + factualEndDate);
        } else {
            System.out.println("Задачу нельзя завершить, так как она не выполняется.");
        }
    }

    public void recalculateSchedule() {
        if (this.status == TaskStatus.COMPLETED) {
            // Пересчитываем зависимости
            for (Task dependentTask : this.dependencies) {
                // Проверяем, что дата начала зависимой задачи еще не установлена или она раньше, чем фактическая дата завершения
                if (dependentTask.getEstimatedStartDate() == null || dependentTask.getEstimatedStartDate().isBefore(this.factualEndDate)) {
                    dependentTask.setEstimatedStartDate(this.factualEndDate.plusHours(1)); // Задача начнется через час после завершения
                    dependentTask.updateEstimatedEndDate(); // Пересчитываем оценочную дату окончания для зависимой задачи
                    System.out.println("Расписание для зависимой задачи " + dependentTask.getName() + " пересчитано.");
                }
            }

            // Пересчитываем подзадачи (если они есть)
            for (Task subTask : this.subTasks) {
                if (subTask.getEstimatedStartDate() == null || subTask.getEstimatedStartDate().isBefore(this.factualEndDate)) {
                    subTask.setEstimatedStartDate(this.factualEndDate.plusHours(1)); // Задача начнется через час после завершения
                    subTask.updateEstimatedEndDate(); // Пересчитываем оценочную дату окончания для подзадачи
                    System.out.println("Расписание для подзадачи " + subTask.getName() + " пересчитано.");
                }
            }
        }
    }


    public void changeStatus(TaskStatus newStatus) {
        if (this.status == newStatus) {
            System.out.println("Статус задачи " + name + " уже установлен: " + newStatus);
            return;
        }

        switch (newStatus) {
            case IN_PROGRESS:
                if (this.status == TaskStatus.PAUSED) togglePauseTask();
                if (canStart()) {
                    startTask(); // Ваш метод с дополнительной логикой
                    recalculateSchedule(); // Пересчет зависимых задач
                } else {
                    System.out.println("Невозможно начать задачу " + name);
                }
                break;

            case COMPLETED:
                if (this.status == TaskStatus.IN_PROGRESS) {
                    completeTask(); // Ваш метод для завершения
                    recalculateSchedule(); // Пересчет зависимых задач
                } else {
                    System.out.println("Невозможно завершить задачу " + name + " из статуса " + this.status);
                }
                break;

            case CANCELLED:
                cancelTask(); // Ваш метод для отмены
                recalculateSchedule(); // Пересчет зависимых задач
                break;

            case PAUSED:
                if (this.status == TaskStatus.IN_PROGRESS) {
                    togglePauseTask(); // Ваш метод для паузы
                } else {
                    System.out.println("Невозможно поставить задачу " + name + " на паузу");
                }
                break;

            default:
                System.out.println("Статус " + newStatus + " не поддерживается для задачи " + name);
                break;
        }

        // Уведомление о смене статуса
        System.out.println("Статус задачи " + name + " изменен на " + newStatus);
    }


    // Назначение исполнителя
    public void assignResource(Resource resource) {
        if(this.getAssignedResource()!=null) System.out.println("Этой задаче уже присвоен исполнитель.");
        else {
            if (this.status == TaskStatus.NOT_STARTED || this.status == TaskStatus.PAUSED) {
                this.assignedResource = resource;
                System.out.println("Задача " + name + " назначена исполнителю " + resource.getName());
            } else {
                System.out.println("Невозможно назначить исполнителя на уже начатую или завершённую задачу.");
            }
        }
    }

    // Переназначение исполнителя
    public void reassignResource(Resource newResource) {
        if (this.status == TaskStatus.PAUSED || this.status == TaskStatus.NOT_STARTED) {
            assignResource(newResource);
        } else if (this.status == TaskStatus.IN_PROGRESS) {
            togglePauseTask(); // Приостановка для переназначения
            assignResource(newResource);
            togglePauseTask(); // Возобновление
        } else {
            System.out.println("Невозможно переназначить ресурс для завершённой или отменённой задачи.");
        }
    }

    // Является ли время рабочим
    private boolean isWorkHour(LocalDateTime dateTime, int startHour, int endHour) {
        return dateTime.getHour() >= startHour && dateTime.getHour() < endHour && dateTime.getDayOfWeek() != DayOfWeek.SATURDAY && dateTime.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    // Расчет оценочной даты окончания задачи
    public LocalDateTime calculateEndDate(LocalDateTime startDate, Duration duration, Calendar projectCalendar, Calendar resourceCalendar) {
        if (startDate == null || duration == null) {
            return null;
        }

        LocalDateTime currentDateTime = startDate;
        long minutesLeft = duration.toMinutes();

        while (minutesLeft > 0) {
            boolean isWorkHour = true;
            if (projectCalendar != null) isWorkHour = projectCalendar.isWorkHour(currentDateTime);
            if (resourceCalendar != null) isWorkHour = isWorkHour && resourceCalendar.isWorkHour(currentDateTime);
            if (isWorkHour) minutesLeft--;
            currentDateTime = currentDateTime.plusMinutes(1);
            if (!isWorkHour) {
                currentDateTime = projectCalendar != null
                        ? projectCalendar.getNextWorkingTime(currentDateTime)
                        : resourceCalendar.getNextWorkingTime(currentDateTime);
            }
        }

        return currentDateTime;
    }

    public void cancelTask() {
        if (this.status == TaskStatus.IN_PROGRESS || this.status == TaskStatus.PAUSED || this.status == TaskStatus.NOT_STARTED) {
            this.status = TaskStatus.CANCELLED;
            System.out.println("Задача " + name + " отменена.");

        } else {
            System.out.println("Нельзя отменить задачу, которая уже завершена.");
        }
    }

    // Обновление estimatedEndDate при изменении estimatedStartDate или estimatedDuration
    public void updateEstimatedEndDate() {
        this.estimatedEndDate = calculateEndDate(this.estimatedStartDate, this.estimatedDuration, this.calendar, this.resourceCalendar);
    }

    public void setEstimatedStartDate(LocalDateTime newStartDate) {
        this.estimatedStartDate = newStartDate;
        updateEstimatedEndDate();
    }

    public void setEstimatedDuration(Duration newDuration) {
        this.estimatedDuration = newDuration;
        updateEstimatedEndDate();
    }

    // Есть ли у задачи незавершённые зависимости
    public boolean hasUnresolvedDependencies() {
        for (Task dependency : dependencies) {
            if (dependency.getStatus() != TaskStatus.COMPLETED) {
                return true;
            }
        }
        return false;
    }

    // Назначить приоритет
    public void setPriority(int priority) {
        if(priority>0 && priority<=100) this.priority = priority;
        else System.out.println("Priority must be over 0 and less than 100");
    }

    public boolean hasDependencies() {
        return this.getDependencies()!=null;
    }

    // Вывод задачи в консоль
    @Override
    public String toString() {
        return "Task{" + name + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(name, task.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

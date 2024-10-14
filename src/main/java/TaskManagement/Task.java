package TaskManagement;

import CalendarManagement.Calendar;
import ChartManagement.Project;
import DoerManagement.Resource;
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

    // Приоритет (по умолчанию 50, от 1 до 100)
    private int priority;

    // Конструктор
    public Task(Long id, String name, Duration estimatedDuration, LocalDateTime estimatedStartDate) {
        this.id = id;
        this.name = name;
        this.estimatedDuration = estimatedDuration;
        this.estimatedStartDate = estimatedStartDate;
        this.status = TaskStatus.NOT_STARTED;
        this.dependencies = new ArrayList<>();
        this.priority = 50;
    }

    // Возможно ли начало выполнения задачи
    public boolean canStart() {
        if (this.status == TaskStatus.NOT_STARTED) {
            for (Task dependency : dependencies) {
                if (dependency.getStatus() != TaskStatus.COMPLETED) {
                    return false;
                }
            }
            return assignedResource != null;
        }
        return false;
    }

    // Добавление зависимой задачи
    public void addSubTask(Task subTask) {
        this.subTasks.add(subTask);
        subTask.getDependencies().add(this); // Делает текущую задачу зависимостью для подзадачи
    }

    // Начало задачи
    public void startTask() {
        if (canStart()) {
            this.status = TaskStatus.IN_PROGRESS;
            this.factualStartDate = LocalDateTime.now();
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
                    "Длительность перерывов: " + totalPauseDuration +
                    "Время окончания задачи: " + factualEndDate);
        } else {
            System.out.println("Задачу нельзя завершить, так как она не выполняется.");
        }
    }

    // Присвоение исполнителя
    public void assignDoer(Resource resource) {
        if (this.status == TaskStatus.NOT_STARTED || this.status == TaskStatus.PAUSED) {
            this.assignedResource = resource;
            System.out.println("Задача " + name + " назначена исполнителю " + resource.getName());
        } else {
            System.out.println("Невозможно назначить исполнителя на уже начатую или завершённую задачу.");
        }
    }

    // Является ли время рабочим
    private boolean isWorkHour(LocalDateTime dateTime, int startHour, int endHour) {
        return dateTime.getHour() >= startHour && dateTime.getHour() < endHour && dateTime.getDayOfWeek() != DayOfWeek.SATURDAY && dateTime.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    // Рассчет даты конца задачи
    public LocalDateTime calculateEndDate(LocalDateTime startDate, Duration duration, Calendar projectCalendar, Calendar resourceCalendar) {
        LocalDateTime endDate = startDate;
        long minutesLeft = duration.toMinutes();
        while (minutesLeft > 0) {
            endDate = endDate.plusMinutes(1);
            if (projectCalendar.isWorkHour(endDate) && resourceCalendar.isWorkHour(endDate)) {
                minutesLeft--;
            }
        }

        return endDate;
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

    // Вывод задачи в консоль
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                "estimatedDuration=" + estimatedDuration +
                ", estimatedStartDate=" + estimatedStartDate +
                ", factualStartDate=" + factualStartDate +
                ", factualDuration=" + factualDuration +
                '}';
    }

    // Переопределение equals и hashCode для корректной работы в Set
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);  // Сравниваем по id
    }

    // Генерация хэш-кода по id
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package TaskManagement;

import ChartManagement.Chart;
import DoerManagement.Doer;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


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
    private Duration factualDuration;

    // Свойства зависимостей
    private Doer assignedDoer;
    private List<Task> dependencies;
    private List<Task> subTasks;
    private TaskStatus status;;

    // Свойства паузы
    private LocalDateTime pauseStartTime;
    private Duration totalPauseDuration = Duration.ZERO;

    // Конструктор
    public Task(Long id, String name, Duration estimatedDuration, LocalDateTime estimatedStartDate) {
        this.id = id;
        this.name = name;
        this.estimatedDuration = estimatedDuration;
        this.estimatedStartDate = estimatedStartDate;
        this.status = TaskStatus.NOT_STARTED;
        this.dependencies = new ArrayList<>();
    }

    // Возможно ли начало выполнения задачи
    public boolean canStart() {
        if(this.status==TaskStatus.NOT_STARTED) {
            for (Task dependency : dependencies) {
                if (dependency.getStatus() != TaskStatus.COMPLETED) {
                    return false;
                }
            }
            return assignedDoer != null;
        } return false;
    }

    // Добавление зависимой задачи
    public void addSubTask(Task subTask) {
        this.subTasks.add(subTask);
        subTask.getDependencies().add(this); // Делает текущую задачу зависимостью для подзадачи
    }

    // Отмена задания
    public void cancelTask(Task task) {
        task.setStatus(TaskStatus.CANCELLED);
        Chart.recalculateProjectSchedule();
    }

    // Замена исполнителя
    public void changeDoer(Task task, Doer newDoer) {
        task.setAssignedDoer(newDoer);
        Chart.recalculateProjectSchedule();  // Обновляем график после изменения исполнителя
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
            this.factualDuration = Duration.between(factualStartDate, LocalDateTime.now()).minus(totalPauseDuration);
            System.out.println("Задача " + name + " завершена. " +
                    "Фактическая продолжительность: " + factualDuration +
                    "Длительность перерывов: " + totalPauseDuration);
        } else {
            System.out.println("Задачу нельзя завершить, так как она не выполняется.");
        }
    }

    // Присвоение исполнителя
    public void assignDoer(Doer doer) {
        if (this.status == TaskStatus.NOT_STARTED || this.status == TaskStatus.PAUSED) {
            this.assignedDoer = doer;
            System.out.println("Задача " + name + " назначена исполнителю " + doer.getName());
        } else {
            System.out.println("Невозможно назначить исполнителя на уже начатую или завершённую задачу.");
        }
    }

    // Является ли время рабочим
    private boolean isWorkHour(LocalDateTime dateTime, int startHour, int endHour) {
        return dateTime.getHour() >= startHour && dateTime.getHour() < endHour && dateTime.getDayOfWeek() != DayOfWeek.SATURDAY && dateTime.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    // Рассчет даты конца задачи
    public LocalDateTime calculateEndDate(LocalDateTime startDate, Duration duration, List<LocalDate> holidays, int startHour, int endHour) {
        LocalDateTime endDate = startDate;
        long hoursLeft = duration.toHours();
        while (hoursLeft > 0) {
            endDate = endDate.plusHours(1);
            if (isWorkHour(endDate, startHour, endHour) && !holidays.contains(endDate.toLocalDate())) {
                hoursLeft--;
            }
        }
        return endDate;
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
}

package ResourceManagement;


import CalendarManagement.Calendar;
import TaskManagement.Task;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// Класс исполнителя
@Getter
@Setter
public class Resource {

    private Long id;
    private String name;
    private Calendar resourceCalendar;

    // Конструктор
    public Resource(String name, Calendar resourceCalendar) {
        this.name = name;
        this.resourceCalendar = resourceCalendar;
    }

    // Уведомление об изменениях
    public void notify(String message) {
        System.out.println("Уведомление для " + name + ": " + message);
    }

    // Назначение задачи
    public void assignTask(Task task) {
        task.assignResource(this);
    }

    // Назначение задач
    public void assignTasks(List<Task> tasks) {
        for(Task task : tasks) {
            task.assignResource(this);
        }
    }

    // Проверка доступности исполнителя в определенное время
    public boolean isAvailable(LocalDateTime startTime, int durationHours) {
        for (int i = 0; i < durationHours; i++) {
            LocalDateTime dateTime = startTime.plusHours(i);
            if (!resourceCalendar.isWorkHour(dateTime)) {
                return false;
            }
        }
        return true;
    }

}

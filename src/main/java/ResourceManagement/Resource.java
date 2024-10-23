package ResourceManagement;


import CalendarManagement.Calendar;
import TaskManagement.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// Класс исполнителя
@Getter
@Setter
public class Resource {

    private Long id;
    private String name;
    private Calendar resourceCalendar;

    public Resource(String name, Calendar resourceCalendar) {
        this.name = name;
        this.resourceCalendar = resourceCalendar;
    }

    public void assignTask(Task task) {
        task.assignResource(this);
    }

    public void assignTasks(List<Task> tasks) {
        for(Task task : tasks) {
            task.assignResource(this);
        }
    }

}

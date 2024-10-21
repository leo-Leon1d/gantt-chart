package ResourceManagement;


import CalendarManagement.Calendar;
import lombok.Getter;
import lombok.Setter;

// Класс исполнителя
@Getter
@Setter
public class Resource {

    private Long id;
    private String name;
    private Calendar calendar;

    public Resource(String name, Calendar calendar) {
        this.name = name;
        this.calendar = calendar;
    }

}

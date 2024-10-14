package DoerManagement;


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

    public Resource(Long id, String name, Calendar calendar) {
        this.id = id;
        this.name = name;
        this.calendar = calendar;
    }

}

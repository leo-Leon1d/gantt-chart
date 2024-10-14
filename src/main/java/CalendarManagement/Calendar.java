package CalendarManagement;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class Calendar {

    private Set<LocalDate> holidays; // Набор выходных дней/праздников
    private int workStartHour; // Начало рабочего дня
    private int workEndHour; // Конец рабочего дня

    // Конструктор с указанием графика работы и списка праздников
    public Calendar(int workStartHour, int workEndHour, Set<LocalDate> holidays) {
        this.holidays = holidays;
        this.workStartHour = workStartHour;
        this.workEndHour = workEndHour;
    }

    // Добавление выходного дня
    public void addHoliday(LocalDate holiday) {
        holidays.add(holiday);
    }

    // Удаление выходного дня
    public void removeHoliday(LocalDate holiday) {
        holidays.remove(holiday);
    }

    // Проверка, является ли данный день рабочим
    public boolean isWorkDay(LocalDate date) {
        return !holidays.contains(date) && date.getDayOfWeek().getValue() < 6; // Пн-Пт
    }

    // Проверка, является ли данное время рабочим
    public boolean isWorkHour(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        int hour = dateTime.getHour();

        return isWorkDay(date) && (hour >= workStartHour) && (hour <= workEndHour);
    }

}

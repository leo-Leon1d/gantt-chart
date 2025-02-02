package CalendarManagement;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Calendar {

    private List<LocalDate> holidays; // Набор выходных дней/праздников
    private int startHour; // Начало рабочего дня
    private int endHour; // Конец рабочего дня
    private int[] weekends; // Выходные

    // Конструктор с указанием графика работы и списка праздников
    public Calendar(int startHour, int endHour, List<LocalDate> holidays, int[] weekends) {
        this.holidays = holidays;
        this.startHour = startHour;
        this.endHour = endHour;
        this.weekends = weekends;
    }

    // Добавление выходного дня
    public void addHoliday(LocalDate holiday) {
        holidays.add(holiday);
    }

    // Удаление выходного дня
    public void removeHoliday(LocalDate holiday) {
        holidays.remove(holiday);
    }

    // Является ли данный день рабочим
    public boolean isWorkDay(LocalDate date) {
        return (holidays == null || !holidays.contains(date)) &&
                Arrays.stream(weekends).noneMatch(day -> day == date.getDayOfWeek().getValue());
    }

    // Является ли данное время рабочим
    public boolean isWorkHour(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        int hour = dateTime.getHour();

        return isWorkDay(date) && (hour >= startHour) && (hour <= endHour);
    }

    // Расчет следующего рабочего времени
    public LocalDateTime getNextWorkingTime(LocalDateTime currentDateTime) {
        LocalDate currentDate = currentDateTime.toLocalDate();

        while (!isWorkDay(currentDate)) currentDate = currentDate.plusDays(1);

        LocalDateTime startOfWorkDay = LocalDateTime.of(currentDate, LocalTime.of(getStartHour(), 0));

        if (currentDateTime.isAfter(startOfWorkDay.withHour(getEndHour())))
            return LocalDateTime.of(currentDate.plusDays(1), LocalTime.of(getStartHour(), 0));

        if (currentDateTime.isAfter(startOfWorkDay)) return currentDateTime;

        return startOfWorkDay;
    }


    // Расчет оставшихся рабочих часов в дне
    public long workHoursLeftForDay(LocalDate date, LocalDateTime currentDateTime) {
        if (!isWorkDay(date)) return 0;

        LocalDateTime startOfWorkDay = LocalDateTime.of(date, LocalTime.of(getStartHour(), 0));
        LocalDateTime endOfWorkDay = LocalDateTime.of(date, LocalTime.of(getEndHour(), 0));

        if (currentDateTime.isAfter(endOfWorkDay)) return 0;
        if (currentDateTime.isBefore(startOfWorkDay)) return Duration.between(startOfWorkDay, endOfWorkDay).toHours();


        return Duration.between(currentDateTime, endOfWorkDay).toHours();
    }



}

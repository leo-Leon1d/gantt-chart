package CalendarManagement;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
public class Calendar {

    private Set<LocalDate> holidays; // Набор выходных дней/праздников
    private int startHour; // Начало рабочего дня
    private int endHour; // Конец рабочего дня

    // Конструктор с указанием графика работы и списка праздников
    public Calendar(int startHour, int endHour, Set<LocalDate> holidays) {
        this.holidays = holidays;
        this.startHour = startHour;
        this.endHour = endHour;
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
        return holidays==null || !holidays.contains(date) && date.getDayOfWeek().getValue() < 6; // Пн-Пт
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

        // Если текущий день нерабочий, переносим на следующий рабочий день
        while (!isWorkDay(currentDate)) {
            currentDate = currentDate.plusDays(1);
        }

        // Определяем начало рабочего дня
        LocalDateTime startOfWorkDay = LocalDateTime.of(currentDate, LocalTime.of(getStartHour(), 0));

        // Если текущее время уже позже конца рабочего дня, переносим на следующий рабочий день
        if (currentDateTime.isAfter(startOfWorkDay.withHour(getEndHour()))) {
            return LocalDateTime.of(currentDate.plusDays(1), LocalTime.of(getStartHour(), 0));
        }

        // Если текущее время находится в пределах рабочего дня, возвращаем текущее время
        if (currentDateTime.isAfter(startOfWorkDay)) {
            return currentDateTime;
        }

        // Если текущее время до начала рабочего дня, возвращаем начало рабочего дня
        return startOfWorkDay;
    }


    // Расчет оставшихся рабочих часов в дне
    public long workHoursLeftForDay(LocalDate date, LocalDateTime currentDateTime) {
        if (!isWorkDay(date)) {
            return 0;
        }

        // Определяем начало и конец рабочего дня
        LocalDateTime startOfWorkDay = LocalDateTime.of(date, LocalTime.of(getStartHour(), 0));
        LocalDateTime endOfWorkDay = LocalDateTime.of(date, LocalTime.of(getEndHour(), 0));

        // Если текущее время уже позже конца рабочего дня, возвращаем 0
        if (currentDateTime.isAfter(endOfWorkDay)) {
            return 0;
        }

        // Если текущее время раньше начала рабочего дня, возвращаем все часы
        if (currentDateTime.isBefore(startOfWorkDay)) {
            return Duration.between(startOfWorkDay, endOfWorkDay).toHours();
        }

        // Возвращаем оставшиеся часы
        return Duration.between(currentDateTime, endOfWorkDay).toHours();
    }



}

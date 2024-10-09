package ChartManagement;

import DoerManagement.Doer;
import TaskManagement.Task;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

// Класс диаграммы
@Getter
@Setter
public class Chart {

    String name;
    private List<Task> tasks;
    private List<Doer> doers;

    // Конструктор (по имени)
    public Chart(String name) {
        this.name = name;
        this.tasks = new ArrayList<>();
        this.doers = new ArrayList<>();
    }

    // Добавление задачи
    public void addTask(Task task) {
        tasks.add(task);
    }

    // Добавление исполнителя
    public void addDoer(Doer doer) {
        doers.add(doer);
    }

    // Рассчитать длину проекта
    // НЕ РЕАЛИЗОВАНО
    public Duration calculateProjectDuration() {
        Duration totalDuration = Duration.ZERO;
        //...
        return totalDuration;
    }

    // Пересчитать расписание проекта
    // НЕ РЕАЛИЗОВАНО
    public static void recalculateProjectSchedule() {
        //...
    }

    // Представить задачи проетка в виде построчной иерархии (в консоли)
    public void displayTasksHierarchy(Task task, int level) {
        String indent = " ".repeat(level * 4);
        System.out.println(indent + task.getName());
        for (Task subTask : task.getSubTasks()) {
            displayTasksHierarchy(subTask, level + 1);
        }
    }

    // Вывести задачи проекта (в консоль)
    public void showTasks() {
        System.out.println("Проект: " + name);
        for (Task task : tasks) {
            System.out.println(task);
        }
    }

    // Вывести исполнителей проекта (в консоль)
    public void showDoers() {
        System.out.println("Проект: " + name);
        for (Doer doer : doers) {
            System.out.println(doer);
        }
    }
}

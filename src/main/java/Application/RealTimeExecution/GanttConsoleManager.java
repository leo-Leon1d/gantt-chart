package Application.RealTimeExecution;

import TaskManagement.Task;

import java.time.Duration;
import java.util.List;

public class GanttConsoleManager {

    // Метод для отображения дерева задач
    public static void displayTaskTree(List<Task> tasks) {
        System.out.println("\n=== Дерево задач ===");
        for (Task task : tasks) {
            displayTaskWithSubtasks(task, 0);
        }
    }

    // Рекурсивный метод для отображения задачи и ее подзадач
    private static void displayTaskWithSubtasks(Task task, int level) {
        String indent = "    ".repeat(level);
        System.out.println(indent + "- " + task.getName() +
                " (Длительность: " + formatDuration(task.getEstimatedDuration()) +
                ", Ресурс: " + (task.getAssignedResource() != null ? task.getAssignedResource().getName() : "не назначен") + ")");

        if (!task.getSubTasks().isEmpty()) {
            for (Task subTask : task.getSubTasks()) {
                displayTaskWithSubtasks(subTask, level + 1);
            }
        }
    }

    // Форматирование длительности задачи для вывода
    private static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        return (hours > 0 ? hours + " ч " : "") +
                (minutes > 0 ? minutes + " мин " : "") +
                seconds + " сек";
    }
}


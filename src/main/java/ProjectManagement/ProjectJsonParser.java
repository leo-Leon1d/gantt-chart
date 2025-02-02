package ProjectManagement;


import com.google.gson.*;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProjectJsonParser {

    // Метод для чтения JSON из файла и назначения ID
    public static ProjectJson parseAndAssignIds(String filePath) throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try (Reader reader = new FileReader(filePath)) {
            ProjectJson project = gson.fromJson(reader, ProjectJson.class);

            if (project.tasks != null) {
                List<TaskJson> allTasks = new ArrayList<>(project.tasks);

                for (TaskJson task : project.tasks) {
                    assignTaskIds(task, project);
                    addSubTasksToProject(task, allTasks);
                }

                project.tasks = allTasks;
            }

            if (project.resources != null) {
                for (ResourceJson resource : project.resources) {
                    assignResourceIds(resource, project);
                }
            }

            System.out.println("=== PARSED PROJECT STRUCTURE ===");
            for (ProjectJsonParser.TaskJson task : project.tasks) {
                System.out.println("Task: " + task.name);
                if (task.subtasks != null) {
                    for (ProjectJsonParser.TaskJson subtask : task.subtasks) {
                        System.out.println("  -> Sub Task: " + subtask.name);
                    }
                }
            }
            System.out.println("===============================");


            return project;
        }
    }


    // Метод для записи обновлённого проекта обратно в файл
    public static void writeProjectToFile(ProjectJson project, String filePath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(project, writer);
        }
    }

    private static void addSubTasksToProject(TaskJson task, List<TaskJson> allTasks) {
        if (task.subtasks != null) {
            for (TaskJson subTask : task.subtasks) {
                allTasks.add(subTask); // Добавляем подзадачу в общий список
                addSubTasksToProject(subTask, allTasks); // Рекурсивно добавляем её подзадачи
            }
        }
    }

    // Рекурсивное назначение ID задачам и подзадачам
    private static void assignTaskIds(TaskJson task, ProjectJson project) {
        if (task.id == null) {
            project.maxTaskId++;
            task.id = project.maxTaskId;
        }

        if (task.subtasks != null) {
            for (TaskJson subtask : task.subtasks) {
                assignTaskIds(subtask, project);
            }
        }

    }

    // Назначение ID ресурсам
    private static void assignResourceIds(ResourceJson resource, ProjectJson project) {
        if (resource.id == null) {
            project.maxResourceId++;
            resource.id = project.maxResourceId;
        }
    }

    // Вспомогательный метод: рассчитать длительность задачи
    private static Duration calculateDuration(TaskJson task) {
        long totalSeconds = task.durationSeconds +
                task.durationMinutes * 60L +
                task.durationHours * 3600L;
        return Duration.ofSeconds(totalSeconds);
    }

    public static class ProjectJson {
        public int maxTaskId;
        public int maxResourceId;
        public String startDate;
        public CalendarJson calendar;
        public List<ResourceJson> resources;
        public List<TaskJson> tasks;

        // Метод для преобразования строки в LocalDateTime
        public LocalDateTime getParsedStartDate() {
            if (startDate == null || startDate.isEmpty()) {
                return LocalDateTime.now(); // Текущее время, если дата не задана
            }
            return LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }


    }

    public static class CalendarJson {
        public int workStartHour;
        public int workEndHour;
        public List<String> holidays;
        public int[] weekends;
    }

    public static class ResourceJson {
        public Integer id;
        public String name;
        public CalendarJson calendar;
    }

    public static class TaskJson {
        public Integer id;
        public String name;
        public int durationSeconds;
        public int durationMinutes;
        public int durationHours;
        public int priority;
        public List<TaskJson> dependencies;
        public String assignedResourceName;
        public List<TaskJson> subtasks;
    }

}

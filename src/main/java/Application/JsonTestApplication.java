package Application;

import CalendarManagement.Calendar;
import ProjectManagement.Project;
import ResourceManagement.Resource;
import TaskManagement.Task;
import ProjectManagement.ProjectJsonParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonTestApplication {

    public static void main(String[] args) {

        try {
            // Загрузка JSON файла из ресурсов
            InputStream inputStream = JsonTestApplication.class.getClassLoader().getResourceAsStream("testdata/one.json");
            if (inputStream == null) {
                throw new RuntimeException("Файл не найден в ресурсах: testdata/one.json");
            }

            // Чтение и парсинг JSON
            Reader reader = new InputStreamReader(inputStream);
            try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/testdata/one.json"))) {
                String line;
                System.out.println("=== JSON FILE CONTENT ===");
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                System.out.println("=========================");
            }

            ProjectJsonParser.ProjectJson projectJson = ProjectJsonParser.parseAndAssignIds("src/main/resources/testdata/one.json");

            reader.close();

            // Создание календаря проекта
            Calendar projectCalendar = new Calendar(
                    projectJson.calendar.workStartHour,
                    projectJson.calendar.workEndHour,
                    parseHolidays(projectJson.calendar.holidays),
                    projectJson.calendar.weekends
            );

            // Инициализация проекта
            Project project = new Project("Project from JSON", projectCalendar);
            project.setFactualStartDate(projectJson.getParsedStartDate());

            // Добавление ресурсов
            List<Resource> resources = new ArrayList<>();
            for (ProjectJsonParser.ResourceJson resourceJson : projectJson.resources) {
                Calendar resourceCalendar = new Calendar(
                        resourceJson.calendar.workStartHour,
                        resourceJson.calendar.workEndHour,
                        parseHolidays(resourceJson.calendar.holidays),
                        resourceJson.calendar.weekends
                );
                resources.add(new Resource(resourceJson.name, resourceCalendar));
            }
            project.addResources(resources);

            // Добавление задач с учетом зависимостей
            List<Task> tasks = new ArrayList<>();
            for (ProjectJsonParser.TaskJson taskJson : projectJson.tasks) {
                Task task = createTaskFromJson(taskJson, resources);
                tasks.add(task);
            }
            project.addTasks(tasks);

            System.out.println(project.getSortedTasks());

            // Запуск проекта
            System.out.println("=== Запуск проекта из JSON ===");
            TestApplication.executeProjectTasks(project);

        } catch (Exception e) {
            System.err.println("Ошибка при запуске приложения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Метод для преобразования TaskJson в Task
    private static Task createTaskFromJson(ProjectJsonParser.TaskJson taskJson, List<Resource> resources) {
        Task task = new Task(taskJson.name, calculateDuration(taskJson));
        task.setPriority(taskJson.priority);

        // Присвоение ресурса
        if (taskJson.assignedResourceName != null) {
            resources.stream()
                    .filter(r -> r.getName().equals(taskJson.assignedResourceName))
                    .findFirst().ifPresent(task::assignResource);
        }

        System.out.println("Создана задача: " + task.getName());

        // Проверка на наличие подзадач перед началом цикла
        if (taskJson.subtasks == null || taskJson.subtasks.isEmpty()) {
            System.out.println("У задачи " + task.getName() + " нет подзадач!");
        } else {
            System.out.println("У задачи " + task.getName() + " есть " + taskJson.subtasks.size() + " подзадач(и)!");

            // Добавление подзадач с зависимостью
            for (ProjectJsonParser.TaskJson subtaskJson : taskJson.subtasks) {
                Task subtask = createTaskFromJson(subtaskJson, resources);
                subtask.addDependentTask(task);  // Установка зависимости
                task.addSubTask(subtask);
                System.out.println("  -> Вложенная задача: " + subtask.getName());
            }
        }

        return task;
    }



    // Метод для вычисления длительности задачи
    private static java.time.Duration calculateDuration(ProjectJsonParser.TaskJson taskJson) {
        long totalSeconds = taskJson.durationSeconds +
                taskJson.durationMinutes * 60L +
                taskJson.durationHours * 3600L;
        return java.time.Duration.ofSeconds(totalSeconds);
    }

    // Метод для парсинга праздников
    private static List<java.time.LocalDate> parseHolidays(List<String> holidays) {
        List<java.time.LocalDate> holidayDates = new ArrayList<>();
        if (holidays != null) {
            for (String holiday : holidays) {
                holidayDates.add(java.time.LocalDate.parse(holiday));
            }
        }
        return holidayDates;
    }
}

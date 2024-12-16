package com.example.demo.Application;

import CalendarManagement.Calendar;
import ProjectManagement.Project;
import ResourceManagement.Resource;
import TaskManagement.Task;
import TaskManagement.TaskStatus;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class TestApplication {

    public static void main(String[] args) {

        Calendar commonCalendar = new Calendar(9, 17, Set.of(LocalDate.of(2024, 11, 23)), null);

        Resource developer1 = new Resource("Dev1", commonCalendar);
        Resource developer2 = new Resource("Dev2", commonCalendar);
        Resource developer3 = new Resource("Dev3", commonCalendar);

        Task mainTask1 = new Task("Main Task 1", Duration.ofMinutes(1));
        Task subTask1 = new Task("Sub Task 1", Duration.ofMinutes(1));
        Task subTask2 = new Task("Sub Task 2", Duration.ofMinutes(1));
        Task mainTask2 = new Task("Main Task 2", Duration.ofMinutes(1));
        Task subTask3 = new Task("Sub Task 3", Duration.ofMinutes(1));
        Task subTask4 = new Task("Sub Task 4", Duration.ofMinutes(1));
        Task sideTask = new Task("Side Task", Duration.ofMinutes(1));

        mainTask1.addSubTasks(List.of(subTask1, subTask2));
        mainTask2.addDependentTasks(List.of(subTask1, subTask2));
        mainTask2.addSubTasks(List.of(subTask3, subTask4));

        sideTask.assignResource(developer1);
        mainTask1.assignResource(developer1);
        subTask1.assignResource(developer2);
        subTask2.assignResource(developer3);
        mainTask2.assignResource(developer1);
        subTask3.assignResource(developer2);
        subTask4.assignResource(developer2);

        sideTask.setPriority(90); // маленький приоритет (чтобы выполнялась после mainTask1)
        subTask3.setPriority(30); // большой приоритет (чтобы выполнялась перед subTask4)

        Project project = new Project("Project", commonCalendar);
        project.addTasks(List.of(mainTask1, mainTask2, subTask4, subTask3, subTask2, subTask1, sideTask));
        project.addResources(List.of(developer1, developer2, developer3));
        project.setFactualStartDate(LocalDateTime.of(2024, 12, 10, 16, 57));

        /*
        System.out.println("=== ТЕСТ 1: Стандартный поток выполнения ===");
        executeProjectTasks(project);
         */

        System.out.println("=== ТЕСТ 2: Динамическое управление проектом ===");
        System.out.println(project.getSortedTasks());
        Thread projectExecutionThread = new Thread(() -> executeProjectTasks(project));
        projectExecutionThread.start();
        manageProjectInRealTime(project, projectExecutionThread);
    }

    // Выполнить задачи проекта
    private static void executeProjectTasks(Project project) {
        LocalDateTime projectStart = project.getFactualStartDate();
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(projectStart)) {
            waitForStart(projectStart);
        }

        for (Task task : project.getTasks()) {
            executeTask(task);
        }

        System.out.println("Все задачи проекта завершены!");
    }

    // Ожидание старта
    private static void waitForStart(LocalDateTime startTime) {
        long delay = java.time.Duration.between(LocalDateTime.now(), startTime).toMillis();
        if (delay > 0) {
            System.out.println("Ожидаем начала проекта до " + startTime);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                System.err.println("Ожидание прервано: " + e.getMessage());
            }
        }
    }

    // Выполнить задачу

    private static void executeTask(Task task) {
        System.out.println("\n");
        ExecutorService executorService = Executors.newCachedThreadPool(); // Асинхронный пул потоков
        try {

            if (task.getStatus() == TaskStatus.COMPLETED) {
                return;
            }

            if (!task.getDependencies().isEmpty()) {
                List<Future<?>> dependencyFutures = new ArrayList<>();
                for (Task dependency : task.getDependencies()) {
                    if (dependency.getStatus() == TaskStatus.NOT_STARTED) {
                        System.out.println("Запуск зависимости: " + dependency.getName());
                        dependencyFutures.add(executorService.submit(() -> executeTask(dependency)));
                    }
                }

                for (Future<?> future : dependencyFutures) {
                    future.get(); // Блокируемся до завершения выполнения зависимости
                }

                for (Task dependency : task.getDependencies()) {
                    if (dependency.getStatus() != TaskStatus.COMPLETED) {
                        System.out.println("Невозможно начать задачу " + task.getName() +
                                ". Зависимость " + dependency.getName() + " не завершена.");
                        return;
                    }
                }
            }

            task.startTask();

            long taskDurationMillis = task.getEstimatedDuration().toMillis();
            Thread.sleep(taskDurationMillis);

            task.completeTask();

        } catch (InterruptedException e) {
            System.err.println("Ошибка выполнения задачи " + task.getName() + ": " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.err.println("Ошибка выполнения зависимости задачи " + task.getName() + ": " + e.getCause());
        } finally {
            executorService.shutdown();
        }
    }



    // Управление при помощи консоли
    private static void manageProjectInRealTime(Project project, Thread projectExecutionThread) {
        Scanner scanner = new Scanner(System.in);

        while (projectExecutionThread.isAlive()) {
            System.out.println("Введите команду (cancel [task], reassign [task] [resource]): ");
            String input = scanner.nextLine();

            String[] commandParts = input.split(" ", 2);

            if (commandParts.length < 2) {
                System.out.println("Ошибка: Неверный формат команды.");
                continue;
            }

            String command = commandParts[0];
            String taskOrResource = commandParts[1].trim();

            switch (command) {
                case "cancel":
                    Task taskToCancel = project.getTaskByName(taskOrResource);
                    if (taskToCancel != null) {
                        taskToCancel.cancelTask();
                    } else {
                        System.out.println("Ошибка: Задача " + taskOrResource + " не найдена.");
                    }
                    break;

                case "reassign":
                    String[] taskAndResource = taskOrResource.split(" ", 2);
                    if (taskAndResource.length < 2) {
                        System.out.println("Ошибка: Укажите задачу и исполнителя для переназначения.");
                        break;
                    }
                    String taskNameToReassign = taskAndResource[0].trim();
                    String resourceName = taskAndResource[1].trim();

                    Task taskToReassign = project.getTaskByName(taskNameToReassign);
                    Resource resource = project.getResourceByName(resourceName);
                    if (taskToReassign != null && resource != null) {
                        taskToReassign.assignResource(resource);
                        System.out.println("Задача " + taskNameToReassign + " переназначена исполнителю " + resourceName + ".");
                    } else {
                        System.out.println("Ошибка: Задача или исполнитель не найдены.");
                    }
                    break;

                default:
                    System.out.println("Неизвестная команда.");
            }
        }

        System.out.println("Проект завершен. Управление больше недоступно.");
    }

}


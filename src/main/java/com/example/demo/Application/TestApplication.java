package com.example.demo.Application;

import CalendarManagement.Calendar;
import ProjectManagement.Project;
import ResourceManagement.Resource;
import TaskManagement.Task;
import TaskManagement.TaskStatus;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class TestApplication {

    public static void main(String[] args) {

        Calendar commonCalendar = new Calendar(9, 17, Set.of(LocalDate.of(2024, 11, 23)), new int[]{6, 7});

        Resource developer1 = new Resource("Dev1", commonCalendar);
        Resource developer2 = new Resource("Dev2", commonCalendar);

        Task task1 = new Task("Task 1", Duration.ofMinutes(1));
        Task task2 = new Task("Task 2", Duration.ofMinutes(1));
        Task task3 = new Task("Task 3", Duration.ofMinutes(1));

        task2.addDependentTask(task1); // Task 2 зависит от Task 1
        task3.addDependentTask(task2); // Task 3 зависит от Task 2

        task1.assignResource(developer1);
        task2.assignResource(developer2);
        task3.assignResource(developer1);

        Project project = new Project("Project", commonCalendar);
        project.addTasks(List.of(task1, task2, task3));
        project.addResources(List.of(developer1, developer2));
        project.setFactualStartDate(LocalDateTime.of(2024, 11, 25, 17, 8));

        /*
        System.out.println("=== ТЕСТ 1: Стандартный поток выполнения ===");
        executeProjectTasks(project);
         */

        System.out.println("=== ТЕСТ 2: Динамическое управление проектом ===");
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
        try {
            if (!task.getDependencies().isEmpty()) {
                for (Task dependency : task.getDependencies()) {
                    while (dependency.getStatus() != TaskStatus.COMPLETED) {
                        if (dependency.getStatus() == TaskStatus.CANCELLED) {
                            System.out.println("Невозможно начать задачу " + task.getName() +
                                    ". Зависимость " + dependency.getName() + " была отменена.");
                            return;
                        }
                        System.out.println("Ожидание завершения зависимости: " + dependency.getName());
                        Thread.sleep(100);
                    }
                }
            }

            task.startTask();

            long taskDurationMillis = task.getEstimatedDuration().toMillis();
            Thread.sleep(taskDurationMillis);

            task.completeTask();

        } catch (InterruptedException e) {
            System.err.println("Ошибка выполнения задачи " + task.getName() + ": " + e.getMessage());
        }
    }

    // Управление с консоли
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


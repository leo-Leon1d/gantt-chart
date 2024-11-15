package com.example.demo.Application;

import CalendarManagement.Calendar;
import ProjectManagement.Project;
import ResourceManagement.Resource;
import TaskManagement.Task;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@EnableScheduling
@SpringBootApplication
public class GanttChartApplication {

	public static void main(String[] args) {

		// Коментарии в конце иннициализации каждой задачи можно не учитывать, они ничего не значат, просто для себя

		Task planProject = new Task("Project Planning", Duration.ofHours(4));
		Task research1 = new Task("First Research", Duration.ofHours(8));
		Task research2 = new Task("Second Research", Duration.ofHours(8));
		Task research3 = new Task("Third Research", Duration.ofHours(4));
		Task assembleProject = new Task("Project Assembling", Duration.ofHours(2));
		Task submitProject = new Task("Project Submition", Duration.ofHours(1));

		Task createWebPage = new Task("Web Page Creation", Duration.ofHours(24));
		Task createProduct = new Task("Product Creation", Duration.ofHours(36));
		Task planMarketing = new Task("Marketing Planning", Duration.ofHours(4));
		Task startSales = new Task("Sales Start", Duration.ofHours(48));

		Task buyMcLaren = new Task("Buying McLaren", Duration.ofHours(1));
		Task buyMansion = new Task("Buying Mansion", Duration.ofHours(1));
		Task buyPrivateJet = new Task("Buying Private Jet", Duration.ofHours(1));

		//Hard Test
		Calendar doer1Calendar = new Calendar(9, 15, Set.of(LocalDate.of(2024, 10, 10)));
		Calendar doer2Calendar = new Calendar(11, 17, Set.of(LocalDate.of(2024, 10, 10)));
		Calendar doer3Calendar = new Calendar(10, 16, Set.of(LocalDate.of(2024, 10, 10)));
		Calendar schoolProjectCalendar = new Calendar(9, 17, null);

		Resource doer1 = new Resource("Ben", doer1Calendar);
		Resource doer2 = new Resource("Max", doer2Calendar);
		Resource doer3 = new Resource("Joe", doer3Calendar);

		Project schoolProject = new Project("Biology School Project", schoolProjectCalendar);
		schoolProject.addResource(doer1);
		schoolProject.addResource(doer2);
		schoolProject.addResource(doer3);

		doer1.assignTasks(List.of(planProject, research1, assembleProject, createProduct, planMarketing, buyMcLaren));
		doer2.assignTasks(List.of(research2, submitProject, startSales, buyMansion));
		doer3.assignTasks(List.of(research3, createWebPage, buyPrivateJet));

		schoolProject.addTask(assembleProject);
		schoolProject.addTask(planProject);
		schoolProject.addTask(research1);
		schoolProject.addTask(research2);
		schoolProject.addTask(research3);
		schoolProject.addTask(submitProject);
		schoolProject.addTask(createWebPage);
		schoolProject.addTask(planMarketing);
		schoolProject.addTask(createProduct);
		schoolProject.addTask(startSales);
		schoolProject.addTask(buyMcLaren);
		schoolProject.addTask(buyMansion);
		schoolProject.addTask(buyPrivateJet);

		List<Task> researches = new ArrayList<>();
		researches.add(research1);
		researches.add(research2);
		researches.add(research3);

		research1.setPriority(90);
		research2.setPriority(80);
		research3.setPriority(70);

		List<Task> purchases = new ArrayList<>();
		purchases.add(buyMcLaren);
		purchases.add(buyMansion);
		purchases.add(buyPrivateJet);

		buyMcLaren.setPriority(90);
		buyMansion.setPriority(95);
		buyPrivateJet.setPriority(85);

		planProject.addSubTasks(researches);
		assembleProject.addDependentTasks(researches);
		assembleProject.addSubTask(submitProject);
		createWebPage.addDependentTask(assembleProject);
		createProduct.addDependentTask(assembleProject);
		planMarketing.addDependentTask(createProduct);
		planMarketing.addSubTask(startSales);
		startSales.addSubTasks(purchases);

		/*
		// Simple test
		planProject.addSubTask(research1);
		research1.addSubTask(assembleProject);
		 */

		schoolProject.setEstimatedStartDate(LocalDateTime.of(2024, 11, 1, 9,0));

		schoolProject.calculateSchedule();

		System.out.println("Calculated Schedule:");
		for (Task task : schoolProject.getSortedTasks()) {
			System.out.println(task.getName() + ": " +
					"Start - " + task.getEstimatedStartDate() +
					", End - " + task.getEstimatedEndDate() +
					"   (Resource: " + task.getAssignedResource().getName() + ")");
		}

		System.out.println("Project Estimated Duration: " + schoolProject.calculateProjectEstimatedDuration());
		SpringApplication.run(GanttChartApplication.class, args);
	}

}

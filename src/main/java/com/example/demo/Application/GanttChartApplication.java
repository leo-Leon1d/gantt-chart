package com.example.demo.Application;

import ProjectManagement.Project;
import ResourceManagement.Resource;
import TaskManagement.Task;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EnableScheduling
@SpringBootApplication
public class GanttChartApplication {

	public static void main(String[] args) {

		Task planProject = new Task("Project Planning", Duration.ofHours(4), LocalDateTime.of(2024, 11, 1, 8,0));
		Task research1 = new Task("First Research", Duration.ofHours(8), LocalDateTime.of(2024, 11, 1, 14,0));
		Task research2 = new Task("Second Research", Duration.ofHours(8), LocalDateTime.of(2024, 11, 2, 8,0));
		Task research3 = new Task("Third Research", Duration.ofHours(4), LocalDateTime.of(2024, 11, 2, 18,0));
		Task assembleProject = new Task("Project Assembling", Duration.ofHours(2), LocalDateTime.of(2024, 11, 3, 10,0));
		Task submitProject = new Task("Project Submition", Duration.ofHours(1), LocalDateTime.of(2024, 11, 4, 8,0));

		Task createWebPage = new Task("Web Page Creation", Duration.ofHours(24), LocalDateTime.of(2024, 11, 15, 8,0));
		Task planMarketing = new Task("Marketing Planning", Duration.ofHours(4), LocalDateTime.of(2024, 11, 14, 8,0));
		Task createProduct = new Task("Product Creation", Duration.ofHours(36), LocalDateTime.of(2024, 11, 20, 8,0));
		Task startSales = new Task("Sales Start", Duration.ofHours(48), LocalDateTime.of(2024, 12, 5, 8,0));
		Task becomeRich = new Task("Becoming Rich", Duration.ofHours(240), LocalDateTime.of(2025, 1, 1, 8,0));

		Task buyMcLaren = new Task("Buying McLaren", Duration.ofHours(240), LocalDateTime.of(2025, 1, 1, 8,0));
		Task buyMansion = new Task("Buying Mansion", Duration.ofHours(240), LocalDateTime.of(2025, 1, 1, 8,0));
		Task buyPrivateJet = new Task("Buying Private Jet", Duration.ofHours(240), LocalDateTime.of(2025, 1, 1, 8,0));

		Resource doer1 = new Resource("Ben", null);
		Resource doer2 = new Resource("Max", null);

		Project schoolProject = new Project("Biology School Project", null);
		schoolProject.addResource(doer1);
		schoolProject.addResource(doer2);

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
		schoolProject.addTask(becomeRich);
		schoolProject.addTask(buyMcLaren);
		schoolProject.addTask(buyMansion);
		schoolProject.addTask(buyPrivateJet);

		List<Task> researches = new ArrayList<>();
		researches.add(research1);
		researches.add(research2);
		researches.add(research3);

		List<Task> purchases = new ArrayList<>();
		purchases.add(buyMcLaren);
		purchases.add(buyMansion);
		purchases.add(buyPrivateJet);

		research1.setPriority(80);
		research2.setPriority(60);
		research3.setPriority(90);

		planProject.addSubTasks(researches);
		assembleProject.addDependentTasks(researches);
		assembleProject.addSubTask(submitProject);
		createWebPage.addDependentTask(assembleProject);

		/*
		// Simple test
		planProject.addSubTask(research1);
		research1.addSubTask(assembleProject);
		 */

		System.out.println("SORTED TASKS:" + schoolProject.getSortedTasks());
		SpringApplication.run(GanttChartApplication.class, args);
	}

}

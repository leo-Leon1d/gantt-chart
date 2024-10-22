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
		Task research1 = new Task("First Research", Duration.ofHours(8), LocalDateTime.of(2024, 11, 1, 14,0));;
		Task research2 = new Task("Second Research", Duration.ofHours(8), LocalDateTime.of(2024, 11, 2, 8,0));;
		Task assembleProject = new Task("Project Assembling", Duration.ofHours(2), LocalDateTime.of(2024, 11, 3, 10,0));;
		Task submitProject = new Task("Submit Project", Duration.ofHours(1), LocalDateTime.of(2024, 11, 4, 8,0));;

		Resource doer1 = new Resource("Ben", null);
		Resource doer2 = new Resource("Max", null);

		Project schoolProject = new Project("Biology School Project", null);
		schoolProject.addResource(doer1);
		schoolProject.addResource(doer2);

		schoolProject.addTask(assembleProject);
		schoolProject.addTask(planProject);
		schoolProject.addTask(research1);
		schoolProject.addTask(research2);
		schoolProject.addTask(submitProject);

		List<Task> researches = new ArrayList<>();
		researches.add(research1);
		researches.add(research2);

		planProject.addSubTasks(researches);
		assembleProject.addDependentTasks(researches);
		assembleProject.addSubTask(submitProject);

		/*

		// Simple test
		planProject.addSubTask(research1);
		research1.addSubTask(assembleProject);

		 */

		System.out.println("SORTED TASKS:" + schoolProject.getSortedTasks());
		schoolProject.displayTasksHierarchy(planProject, 1);

		SpringApplication.run(GanttChartApplication.class, args);
	}

}

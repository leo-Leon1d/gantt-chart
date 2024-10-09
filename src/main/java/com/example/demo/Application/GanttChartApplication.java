package com.example.demo.Application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GanttChartApplication {

	public static void main(String[] args) {
		SpringApplication.run(GanttChartApplication.class, args);
	}

}

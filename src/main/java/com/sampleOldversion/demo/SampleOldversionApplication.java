package com.sampleOldversion.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jdk.internal.org.jline.utils.Log;

@SpringBootApplication
public class SampleOldversionApplication {

	public static void main(String[] args) {
		System.out.println("Into main Application");
		SpringApplication.run(SampleOldversionApplication.class, args);
	}

}

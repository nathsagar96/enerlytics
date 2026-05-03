package com.enerlytics.ingestions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class IngestionServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(IngestionServiceApplication.class, args);
    }
}

package com.bottega.bottega_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // <-- Aggiunto questo!

@SpringBootApplication
@EnableScheduling //Accende l'orologio interno di Spring Boot
public class BottegaWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(BottegaWebApplication.class, args);
    }
}
package com.ecrans.vente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling          // active @Scheduled, pour le planificateur (7.5)
public class VenteApplication {

    public static void main(String[] args) {
        SpringApplication.run(VenteApplication.class, args);
    }
}

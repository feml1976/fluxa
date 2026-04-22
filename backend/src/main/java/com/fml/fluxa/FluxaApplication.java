package com.fml.fluxa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FluxaApplication {

    public static void main(String[] args) {
        SpringApplication.run(FluxaApplication.class, args);
    }
}

package com.matrimonial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry point for the Matrimonial Platform application.
 *
 * @SpringBootApplication enables:
 *   - @Configuration   → marks this as a config class
 *   - @EnableAutoConfiguration → auto-configures Spring beans
 *   - @ComponentScan   → scans all classes under com.matrimonial
 *
 * @EnableAsync → required for @Async email methods in EmailService
 *   to run in a background thread (so API response is not delayed)
 */
@SpringBootApplication
@EnableAsync
public class MatrimonialApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatrimonialApplication.class, args);
    }
}

package de.khudhurayaz.freshradar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppStarter {
    public static void main(String[] args) {
        EnvLoader.load();
        SpringApplication.run(AppStarter.class, args);
    }
}

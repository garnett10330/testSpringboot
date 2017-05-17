package com.test.testSpringboot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.test.testSpringboot.listener.MyApplicationStartedEventListener;
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
//        SpringApplication app = new SpringApplication(Application.class); 
//        app.addListeners(new MyApplicationStartedEventListener());
//        app.run(args);
    }
}
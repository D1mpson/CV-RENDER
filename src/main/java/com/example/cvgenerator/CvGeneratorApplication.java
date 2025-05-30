package com.example.cvgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class CvGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CvGeneratorApplication.class, args);
    }
}
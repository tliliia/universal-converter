package com.demo.universalconverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UniversalConverterApplication {
    public static String filePath = "src/main/java/com/demo/universalconverter/helper/data.csv";

    public static void main(String[] args) {

        if (args.length > 0) {
            filePath = args[0];
        }
        SpringApplication.run(UniversalConverterApplication.class, args);
    }
}

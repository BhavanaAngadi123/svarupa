package com.svarupa;

import com.svarupa.config.SvarupaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SvarupaProperties.class)
public class SvarupaApplication {
  public static void main(String[] args) {
    SpringApplication.run(SvarupaApplication.class, args);
  }
}

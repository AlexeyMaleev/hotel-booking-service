package com.example.hotels_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@EnableJpaRepositories(basePackages = "com.example.hotels_app.repository")
@EnableReactiveMongoRepositories(basePackages = "com.example.hotels_app.statistics.repository")
@SpringBootApplication
public class HotelsAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelsAppApplication.class, args);
	}

}

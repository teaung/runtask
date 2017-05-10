package com.byd5.ats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.byd5.ats.rabbitmq.SenderDepart;
import com.byd5.ats.service.RunTaskService;

@SpringBootApplication
@EnableScheduling
public class ApplicationTrainDepart {

/*	@Bean
	public RunTaskService runTaskService() {
		return new RunTaskService();
	}*/
	
    public static void main(String[] args) {
        SpringApplication.run(ApplicationTrainDepart.class, args);
    }
}

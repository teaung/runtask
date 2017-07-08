package com.byd5.ats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.byd5.ats.rabbitmq.SenderDepart;
import com.byd5.ats.service.RunTaskService;

@SpringBootApplication
@EnableScheduling
@EnableEurekaClient//@EnableDiscoveryClient
@EnableCircuitBreaker            //增加的断路器注解
public class ApplicationTrainDepart {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationTrainDepart.class, args);
    }
    
    @LoadBalanced
	@Bean
	public RestTemplate getRest() {
		return new RestTemplate();
	}

}

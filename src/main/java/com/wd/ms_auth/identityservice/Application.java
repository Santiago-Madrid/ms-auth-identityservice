package com.wd.ms_auth.identityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.wd.ms_auth.identityservice",
		"com.world_dance.wd_lib_common"
})
@EntityScan(basePackages = {
		"com.world_dance.wd_lib_common.entity"
})
@EnableJpaRepositories(basePackages = {
		"com.world_dance.wd_lib_common.repository"
})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
package com.momo.book;

import com.momo.book.Role.Role;
import com.momo.book.Role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.LocalDateTime;

@EnableAsync
@SpringBootApplication
public class BookNetworkApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookNetworkApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(RoleRepository roleRepository) {
		return args -> {
			if (roleRepository.findByName("User").isEmpty()) {
				roleRepository.save(Role.builder()
						.name("User")
						.createdDate(LocalDateTime.now()) // Set createdDate
						.build()
				);
			}
		};
	}
}



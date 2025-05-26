package com.projeto.levelupapi.projeto_levelupapi;

import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.model.Role;
import com.projeto.levelupapi.projeto_levelupapi.repository.UserRepository;
import com.projeto.levelupapi.projeto_levelupapi.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjetoLevelupapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjetoLevelupapiApplication.class, args);
	}

	@Bean
	public CommandLineRunner createAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder, UserService userService) {
		return args -> {
			if (userRepository.findByUsername("admin").isEmpty()) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword("admin"); // senha em texto puro, será codificada pelo UserService
				admin.setRole(Role.ADMIN);
				admin.setInSafeZone(true);
				userService.create(admin);
				System.out.println("Usuário ADMIN padrão criado: admin/admin");
			}
		};
	}
}

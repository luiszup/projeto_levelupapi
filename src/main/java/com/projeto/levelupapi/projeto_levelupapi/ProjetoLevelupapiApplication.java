package com.projeto.levelupapi.projeto_levelupapi;

import com.projeto.levelupapi.projeto_levelupapi.model.Mission;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.model.Role;
import com.projeto.levelupapi.projeto_levelupapi.repository.MissionRepository;
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

	@Bean
	public CommandLineRunner createInitialMissions(MissionRepository missionRepository) {
		return args -> {
			if (missionRepository.count() == 0) {
				System.out.println("Criando missões iniciais...");
				
				// Missões para nível 1
				Mission primeiraExploracao = new Mission(null, "Primeira Exploração", 
					"Dê seus primeiros passos no mundo", 20, 1, true, false);
				missionRepository.save(primeiraExploracao);
				
				Mission coletandoRecursos = new Mission(null, "Coletando Recursos", 
					"Colete recursos básicos para sobrevivência", 15, 1, true, true);
				missionRepository.save(coletandoRecursos);
				
				Mission conhecendoSistema = new Mission(null, "Conhecendo o Sistema", 
					"Familiarize-se com os controles básicos", 10, 1, true, false);
				missionRepository.save(conhecendoSistema);
				
				// Missões para nível 2
				Mission exploracaoAvancada = new Mission(null, "Exploração Avançada", 
					"Aventure-se em territórios mais perigosos", 30, 2, true, false);
				missionRepository.save(exploracaoAvancada);
				
				Mission missaoCombate = new Mission(null, "Missão de Combate", 
					"Enfrente seus primeiros inimigos", 25, 2, true, true);
				missionRepository.save(missaoCombate);
				
				Mission desenvolvendoHabilidades = new Mission(null, "Desenvolvendo Habilidades", 
					"Aprimore suas capacidades", 20, 2, true, false);
				missionRepository.save(desenvolvendoHabilidades);
				
				// Missões para nível 3
				Mission desafioEpico = new Mission(null, "Desafio Épico", 
					"Enfrente um desafio realmente difícil", 50, 3, true, false);
				missionRepository.save(desafioEpico);
				
				System.out.println("7 missões iniciais criadas com sucesso!");
			}
		};
	}
}

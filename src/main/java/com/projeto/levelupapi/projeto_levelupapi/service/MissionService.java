package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.dto.MissionCompletionResponseDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.MissionResponseDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.MissionHistoryDto;
import com.projeto.levelupapi.projeto_levelupapi.exception.BadRequestException;
import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.CompletedMission;
import com.projeto.levelupapi.projeto_levelupapi.model.Mission;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.repository.CompletedMissionRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.MissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MissionService {
    
    private static final Logger logger = LoggerFactory.getLogger(MissionService.class);
    
    private final MissionRepository missionRepository;
    private final CompletedMissionRepository completedMissionRepository;
    private final UserService userService;
    private final XpService xpService;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public MissionService(MissionRepository missionRepository,
                         CompletedMissionRepository completedMissionRepository,
                         UserService userService,
                         XpService xpService) {
        this.missionRepository = missionRepository;
        this.completedMissionRepository = completedMissionRepository;
        this.userService = userService;
        this.xpService = xpService;
    }
    
    public Mission createMission(Mission mission) {
        logger.info("Creating new mission: {}", mission.getName());
        Mission saved = missionRepository.save(mission);
        logger.info("Mission created successfully: {} (ID: {})", saved.getName(), saved.getId());
        return saved;
    }
    
    public List<MissionResponseDto> getAvailableMissions(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        int userLevel = user.getXpData() != null ? user.getXpData().getLevel() : 1;
        
        List<Mission> availableMissions = missionRepository.findAvailableMissionsForLevel(userLevel);
        
        return availableMissions.stream()
                .map(mission -> {
                    MissionResponseDto dto = new MissionResponseDto();
                    dto.setId(mission.getId());
                    dto.setName(mission.getName());
                    dto.setDescription(mission.getDescription());
                    dto.setXpReward(mission.getXpReward());
                    dto.setRequiredLevel(mission.getRequiredLevel());
                    dto.setIsRepeatable(mission.getIsRepeatable());
                    
                    // Verifica se o usuário pode completar esta missão
                    boolean canComplete = mission.getIsRepeatable() || 
                            !completedMissionRepository.existsByUserAndMission(user, mission);
                    dto.setCanComplete(canComplete);
                    
                    return dto;
                })
                .filter(dto -> dto.getCanComplete()) // Só mostra missões que podem ser completadas
                .collect(Collectors.toList());
    }
    
    @Transactional
    public MissionCompletionResponseDto completeMission(Long missionId, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada"));
        
        // Verificar se a missão está ativa
        if (!mission.getIsActive()) {
            throw new BadRequestException("Esta missão não está mais disponível");
        }
        
        // Verificar nível do usuário
        int userLevel = user.getXpData() != null ? user.getXpData().getLevel() : 1;
        if (userLevel < mission.getRequiredLevel()) {
            throw new BadRequestException("Nível insuficiente para esta missão. Necessário: " + 
                    mission.getRequiredLevel());
        }
        
        // Verificar se já completou (para missões não repetíveis)
        if (!mission.getIsRepeatable() && 
            completedMissionRepository.existsByUserAndMission(user, mission)) {
            throw new BadRequestException("Você já completou esta missão");
        }
        
        // Obter XP e nível antes da conclusão
        Xp xpBefore = xpService.obterXp(userId);
        int levelBefore = xpBefore.getLevel();
          // Adicionar XP
        xpService.adicionarXp(userId, mission.getXpReward());
        
        // Obter XP e nível após a conclusão
        Xp xpAfter = xpService.obterXp(userId);
        boolean levelUp = xpAfter.getLevel() > levelBefore;
        
        // Registrar missão completada
        CompletedMission completed = new CompletedMission(user, mission, mission.getXpReward());
        completedMissionRepository.save(completed);
        
        logger.info("Mission completed: {} by user {} (+{} XP)", 
                mission.getName(), user.getUsername(), mission.getXpReward());
        
        String message = levelUp ? 
                String.format("Missão '%s' completada! +%d XP ganhos! PARABÉNS! Você subiu para o nível %d!", 
                        mission.getName(), mission.getXpReward(), xpAfter.getLevel()) :
                String.format("Missão '%s' completada! +%d XP ganhos!", 
                        mission.getName(), mission.getXpReward());
        
        return new MissionCompletionResponseDto(
                message,
                mission.getXpReward(),
                xpAfter.getXpPoints(),
                xpAfter.getLevel(),
                levelUp,
                mission.getName()
        );
    }
    
    public List<Mission> listAllMissions() {
        return missionRepository.findAll();
    }
      public List<MissionHistoryDto> getUserMissionHistory(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        List<CompletedMission> completedMissions = completedMissionRepository.findByUserOrderByCompletedAtDesc(user);
        
        return completedMissions.stream()
                .map(cm -> new MissionHistoryDto(
                        cm.getId(),
                        cm.getMission().getName(),
                        cm.getMission().getDescription(),
                        cm.getXpGained(),
                        cm.getCompletedAt(),
                        cm.getUser().getUsername()
                ))
                .collect(Collectors.toList());
    }    @Transactional
    public void resetMissions() {
        logger.info("Reinicializando missões...");
        
        // Remove todas as missões completadas
        completedMissionRepository.deleteAll();
        logger.info("Missões completadas removidas");
        
        // Remove todas as missões
        missionRepository.deleteAll();
        logger.info("Todas as missões removidas");
        
        // Força o flush das operações de delete usando EntityManager
        entityManager.flush();
        entityManager.clear();
        logger.info("Flush executado com sucesso");
        
        // Recria as missões iniciais
        createInitialMissions();
        
        logger.info("Missões reinicializadas com sucesso!");
    }
      private void createInitialMissions() {
        logger.info("Criando missões iniciais...");
        
        // Missões para nível 1
        Mission primeiraExploracao = new Mission(null, "Primeira Exploração", 
            "Dê seus primeiros passos no mundo", 20, 1, true, false);
        primeiraExploracao = missionRepository.save(primeiraExploracao);
        logger.info("Missão criada: {} (ID: {})", primeiraExploracao.getName(), primeiraExploracao.getId());
        
        Mission coletandoRecursos = new Mission(null, "Coletando Recursos", 
            "Colete recursos básicos para sobrevivência", 15, 1, true, true);
        coletandoRecursos = missionRepository.save(coletandoRecursos);
        logger.info("Missão criada: {} (ID: {})", coletandoRecursos.getName(), coletandoRecursos.getId());
        
        Mission conhecendoSistema = new Mission(null, "Conhecendo o Sistema", 
            "Familiarize-se com os controles básicos", 10, 1, true, false);
        conhecendoSistema = missionRepository.save(conhecendoSistema);
        logger.info("Missão criada: {} (ID: {})", conhecendoSistema.getName(), conhecendoSistema.getId());
        
        // Missões para nível 2
        Mission exploracaoAvancada = new Mission(null, "Exploração Avançada", 
            "Aventure-se em territórios mais perigosos", 30, 2, true, false);
        exploracaoAvancada = missionRepository.save(exploracaoAvancada);
        logger.info("Missão criada: {} (ID: {})", exploracaoAvancada.getName(), exploracaoAvancada.getId());
        
        Mission missaoCombate = new Mission(null, "Missão de Combate", 
            "Enfrente seus primeiros inimigos", 25, 2, true, true);
        missaoCombate = missionRepository.save(missaoCombate);
        logger.info("Missão criada: {} (ID: {})", missaoCombate.getName(), missaoCombate.getId());
        
        Mission desenvolvendoHabilidades = new Mission(null, "Desenvolvendo Habilidades", 
            "Aprimore suas capacidades", 20, 2, true, false);
        desenvolvendoHabilidades = missionRepository.save(desenvolvendoHabilidades);
        logger.info("Missão criada: {} (ID: {})", desenvolvendoHabilidades.getName(), desenvolvendoHabilidades.getId());
        
        // Missões para nível 3
        Mission desafioEpico = new Mission(null, "Desafio Épico", 
            "Enfrente um desafio realmente difícil", 50, 3, true, false);
        desafioEpico = missionRepository.save(desafioEpico);
        logger.info("Missão criada: {} (ID: {})", desafioEpico.getName(), desafioEpico.getId());
        
        // Flush final para garantir que tudo foi salvo
        entityManager.flush();
        
        long totalMissions = missionRepository.count();
        logger.info("Total de {} missões iniciais criadas com sucesso!", totalMissions);
    }
}

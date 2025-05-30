package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.dto.MissionCompletionResponseDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.MissionHistoryDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.MissionResponseDto;
import com.projeto.levelupapi.projeto_levelupapi.exception.BadRequestException;
import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.CompletedMission;
import com.projeto.levelupapi.projeto_levelupapi.model.Mission;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.repository.CompletedMissionRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.MissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private CompletedMissionRepository completedMissionRepository;

    @Mock
    private UserService userService;

    @Mock
    private XpService xpService;

    @InjectMocks
    private MissionService missionService;    @Test
    @DisplayName("Deve retornar missões disponíveis para usuário de nível 1")
    void shouldReturnAvailableMissionsForLevel1User() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        
        Xp xpData = new Xp();
        xpData.setLevel(1);
        user.setXpData(xpData);

        Mission mission1 = new Mission();
        mission1.setId(1L);
        mission1.setName("Primeira Missão");
        mission1.setDescription("Missão para iniciantes");
        mission1.setXpReward(10);
        mission1.setRequiredLevel(1);
        mission1.setIsRepeatable(true);

        List<Mission> availableMissions = Arrays.asList(mission1);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(missionRepository.findAvailableMissionsForLevel(1)).thenReturn(availableMissions);

        // Act
        List<MissionResponseDto> result = missionService.getAvailableMissions(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        MissionResponseDto dto = result.get(0);
        assertEquals(mission1.getId(), dto.getId());
        assertEquals(mission1.getName(), dto.getName());
        assertEquals(mission1.getDescription(), dto.getDescription());
        assertEquals(mission1.getXpReward(), dto.getXpReward());
        assertTrue(dto.getCanComplete());
        
        verify(userService).findById(userId);
        verify(missionRepository).findAvailableMissionsForLevel(1);
    }

    @Test
    @DisplayName("Deve retornar missões disponíveis excluindo não repetíveis já completadas")
    void shouldReturnAvailableMissionsExcludingCompletedNonRepeatableMissions() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        
        Xp xpData = new Xp();
        xpData.setLevel(5);
        user.setXpData(xpData);

        Mission repeatableMission = new Mission();
        repeatableMission.setId(1L);
        repeatableMission.setName("Missão Repetível");
        repeatableMission.setIsRepeatable(true);
        repeatableMission.setRequiredLevel(5);

        Mission nonRepeatableMission = new Mission();
        nonRepeatableMission.setId(2L);
        nonRepeatableMission.setName("Missão Única");
        nonRepeatableMission.setIsRepeatable(false);
        nonRepeatableMission.setRequiredLevel(5);

        List<Mission> allMissions = Arrays.asList(repeatableMission, nonRepeatableMission);        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(missionRepository.findAvailableMissionsForLevel(5)).thenReturn(allMissions);
        when(completedMissionRepository.existsByUserAndMission(user, nonRepeatableMission)).thenReturn(true);

        // Act
        List<MissionResponseDto> result = missionService.getAvailableMissions(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Missão Repetível", result.get(0).getName());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não existe")
    void shouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {
        // Arrange
        Long userId = 999L;
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            missionService.getAvailableMissions(userId);
        });

        assertTrue(exception.getMessage().contains("Usuário não encontrado"));
        verify(userService).findById(userId);
        verify(missionRepository, never()).findAvailableMissionsForLevel(any(Integer.class));
    }

    @Test
    @DisplayName("Deve completar missão com sucesso")
    void shouldCompleteMissionSuccessfully() {
        // Arrange
        Long userId = 1L;
        Long missionId = 1L;
        
        User user = new User();
        user.setId(userId);
        
        Xp xpData = new Xp();
        xpData.setLevel(5);
        user.setXpData(xpData);

        Mission mission = new Mission();
        mission.setId(missionId);
        mission.setName("Missão Teste");
        mission.setXpReward(50);
        mission.setRequiredLevel(3);
        mission.setIsActive(true);

        CompletedMission completedMission = new CompletedMission();
        completedMission.setUser(user);
        completedMission.setMission(mission);
        completedMission.setCompletedAt(LocalDateTime.now());        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(completedMissionRepository.save(any(CompletedMission.class))).thenReturn(completedMission);
        
        // Mock do XpService
        Xp xpBefore = new Xp();
        xpBefore.setLevel(5);
        xpBefore.setXpPoints(100);
        
        Xp xpAfter = new Xp();
        xpAfter.setLevel(5);
        xpAfter.setXpPoints(150);
        
        when(xpService.obterXp(userId)).thenReturn(xpBefore).thenReturn(xpAfter);
        when(xpService.adicionarXp(userId, 50)).thenReturn("XP adicionado com sucesso");        // Act
        MissionCompletionResponseDto result = missionService.completeMission(missionId, userId);

        // Assert
        assertNotNull(result);
        assertEquals("Missão Teste", result.getMissionName());
        assertEquals(50, result.getXpGained());
        assertNotNull(result.getMessage());
        
        verify(userService).findById(userId);
        verify(missionRepository).findById(missionId);
        verify(completedMissionRepository).save(any(CompletedMission.class));
        verify(xpService).adicionarXp(userId, 50);
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando usuário não tem nível suficiente")
    void shouldThrowBadRequestExceptionWhenUserLevelIsInsufficient() {
        // Arrange
        Long userId = 1L;
        Long missionId = 1L;
        
        User user = new User();
        user.setId(userId);
        
        Xp xpData = new Xp();
        xpData.setLevel(2);
        user.setXpData(xpData);

        Mission mission = new Mission();
        mission.setId(missionId);
        mission.setRequiredLevel(5);
        mission.setIsActive(true);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            missionService.completeMission(userId, missionId);
        });        assertTrue(exception.getMessage().contains("Nível insuficiente"));
        verify(completedMissionRepository, never()).save(any(CompletedMission.class));
        verify(xpService, never()).adicionarXp(any(Long.class), any(Integer.class));
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando missão está inativa")
    void shouldThrowBadRequestExceptionWhenMissionIsInactive() {
        // Arrange
        Long userId = 1L;
        Long missionId = 1L;
        
        User user = new User();
        user.setId(userId);
        
        Xp xpData = new Xp();
        xpData.setLevel(5);
        user.setXpData(xpData);        Mission mission = new Mission();
        mission.setId(missionId);
        mission.setRequiredLevel(3);
        mission.setIsActive(false);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            missionService.completeMission(userId, missionId);
        });

        assertTrue(exception.getMessage().contains("Esta missão não está mais disponível"));
        verify(completedMissionRepository, never()).save(any(CompletedMission.class));
    }

    @Test
    @DisplayName("Deve retornar histórico de missões completadas")
    void shouldReturnMissionHistory() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Mission mission1 = new Mission();
        mission1.setName("Missão 1");
        mission1.setXpReward(25);        CompletedMission completedMission1 = new CompletedMission();
        completedMission1.setUser(user);
        completedMission1.setMission(mission1);
        completedMission1.setXpGained(25);
        completedMission1.setCompletedAt(LocalDateTime.now());

        List<CompletedMission> completedMissions = Arrays.asList(completedMission1);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(completedMissionRepository.findByUserOrderByCompletedAtDesc(user)).thenReturn(completedMissions);        // Act
        List<MissionHistoryDto> result = missionService.getUserMissionHistory(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        MissionHistoryDto historyDto = result.get(0);
        assertEquals("Missão 1", historyDto.getMissionName());
        assertEquals(25, historyDto.getXpGained());
        assertNotNull(historyDto.getCompletedAt());
        
        verify(userService).findById(userId);
        verify(completedMissionRepository).findByUserOrderByCompletedAtDesc(user);
    }    @Test
    @DisplayName("Deve retornar nível 1 como padrão quando usuário não tem XP data")
    void shouldReturnLevel1AsDefaultWhenUserHasNoXpData() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setXpData(null);

        Mission mission1 = new Mission();
        mission1.setId(1L);
        mission1.setRequiredLevel(1);
        mission1.setIsRepeatable(true);

        List<Mission> availableMissions = Arrays.asList(mission1);        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(missionRepository.findAvailableMissionsForLevel(1)).thenReturn(availableMissions);

        // Act
        List<MissionResponseDto> result = missionService.getAvailableMissions(userId);

        // Assert
        assertNotNull(result);
        verify(missionRepository).findAvailableMissionsForLevel(1);
    }

    @Test
    @DisplayName("Deve criar missão com sucesso")
    void deveCriarMissaoComSucesso() {
        // Arrange
        Mission mission = new Mission();
        mission.setName("Nova Missão");
        mission.setDescription("Descrição da missão");
        mission.setXpReward(50);
        mission.setRequiredLevel(2);

        Mission savedMission = new Mission();
        savedMission.setId(1L);
        savedMission.setName("Nova Missão");
        savedMission.setDescription("Descrição da missão");
        savedMission.setXpReward(50);
        savedMission.setRequiredLevel(2);

        when(missionRepository.save(mission)).thenReturn(savedMission);

        // Act
        Mission result = missionService.createMission(mission);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Nova Missão", result.getName());
        verify(missionRepository).save(mission);
    }

    @Test
    @DisplayName("Deve listar todas as missões")
    void deveListarTodasAsMissoes() {
        // Arrange
        Mission mission1 = new Mission();
        mission1.setId(1L);
        mission1.setName("Missão 1");

        Mission mission2 = new Mission();
        mission2.setId(2L);
        mission2.setName("Missão 2");

        List<Mission> missions = Arrays.asList(mission1, mission2);
        when(missionRepository.findAll()).thenReturn(missions);

        // Act
        List<Mission> result = missionService.listAllMissions();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Missão 1", result.get(0).getName());
        assertEquals("Missão 2", result.get(1).getName());
        verify(missionRepository).findAll();
    }

    @Test
    @DisplayName("Deve falhar ao completar missão inativa")
    void deveFalharAoCompletarMissaoInativa() {
        // Arrange
        Long userId = 1L;
        Long missionId = 1L;

        User user = new User();
        user.setId(userId);

        Mission mission = new Mission();
        mission.setId(missionId);
        mission.setIsActive(false);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> missionService.completeMission(missionId, userId)
        );

        assertEquals("Esta missão não está mais disponível", exception.getMessage());
        verify(userService).findById(userId);
        verify(missionRepository).findById(missionId);
    }

    @Test
    @DisplayName("Deve falhar ao completar missão com nível insuficiente")
    void deveFalharAoCompletarMissaoComNivelInsuficiente() {
        // Arrange
        Long userId = 1L;
        Long missionId = 1L;

        User user = new User();
        user.setId(userId);

        Xp xp = new Xp();
        xp.setLevel(1);
        user.setXpData(xp);

        Mission mission = new Mission();
        mission.setId(missionId);
        mission.setIsActive(true);
        mission.setRequiredLevel(5);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> missionService.completeMission(missionId, userId)
        );

        assertTrue(exception.getMessage().contains("Nível insuficiente"));
        verify(userService).findById(userId);
        verify(missionRepository).findById(missionId);
    }

    @Test
    @DisplayName("Deve falhar ao completar missão não repetível já completada")
    void deveFalharAoCompletarMissaoNaoRepetivelJaCompletada() {
        // Arrange
        Long userId = 1L;
        Long missionId = 1L;

        User user = new User();
        user.setId(userId);

        Xp xp = new Xp();
        xp.setLevel(5);
        user.setXpData(xp);

        Mission mission = new Mission();
        mission.setId(missionId);
        mission.setIsActive(true);
        mission.setRequiredLevel(1);
        mission.setIsRepeatable(false);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(completedMissionRepository.existsByUserAndMission(user, mission)).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> missionService.completeMission(missionId, userId)
        );

        assertEquals("Você já completou esta missão", exception.getMessage());
        verify(userService).findById(userId);
        verify(missionRepository).findById(missionId);
        verify(completedMissionRepository).existsByUserAndMission(user, mission);
    }

    @Test
    @DisplayName("Deve falhar ao completar missão inexistente")
    void deveFalharAoCompletarMissaoInexistente() {
        // Arrange
        Long userId = 1L;
        Long missionId = 999L;

        User user = new User();
        user.setId(userId);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(missionRepository.findById(missionId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> missionService.completeMission(missionId, userId)
        );

        assertEquals("Missão não encontrada", exception.getMessage());
        verify(userService).findById(userId);
        verify(missionRepository).findById(missionId);
    }

    @Test
    @DisplayName("Deve falhar ao obter histórico de usuário inexistente")
    void deveFalharAoObterHistoricoDeUsuarioInexistente() {
        // Arrange
        Long userId = 999L;
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> missionService.getUserMissionHistory(userId)
        );

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(userService).findById(userId);
    }
}

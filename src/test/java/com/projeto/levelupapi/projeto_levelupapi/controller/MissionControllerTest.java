package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.dto.MissionCompletionResponseDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.MissionRequestDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.MissionResponseDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.MissionHistoryDto;
import com.projeto.levelupapi.projeto_levelupapi.exception.BadRequestException;
import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.Mission;
import com.projeto.levelupapi.projeto_levelupapi.service.MissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do MissionController")
class MissionControllerTest {

    @Mock
    private MissionService missionService;

    @InjectMocks
    private MissionController missionController;

    @Test
    @DisplayName("Deve retornar missões disponíveis para usuário válido")
    void deveRetornarMissoesDisponiveisParaUsuarioValido() {
        // Arrange
        Long userId = 1L;
        List<MissionResponseDto> missoes = Arrays.asList(
            createMissionResponseDto(1L, "Treinar Combate", 50, 1),
            createMissionResponseDto(2L, "Coletar Recursos", 30, 1),
            createMissionResponseDto(3L, "Explorar Território", 100, 2)
        );
        
        when(missionService.getAvailableMissions(userId)).thenReturn(missoes);

        // Act
        ResponseEntity<List<MissionResponseDto>> response = missionController.getAvailableMissions(userId);

        // Assert
        assertAll("Verificações de missões disponíveis",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals(3, response.getBody().size()),
            () -> assertEquals("Treinar Combate", response.getBody().get(0).getName()),
            () -> assertEquals(50, response.getBody().get(0).getXpReward()),
            () -> assertEquals(1, response.getBody().get(0).getRequiredLevel())
        );
        
        verify(missionService).getAvailableMissions(userId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem missões disponíveis")
    void deveRetornarListaVaziaQuandoUsuarioNaoTemMissoesDisponiveis() {
        // Arrange
        Long userId = 1L;
        List<MissionResponseDto> missoesVazias = Collections.emptyList();
        
        when(missionService.getAvailableMissions(userId)).thenReturn(missoesVazias);

        // Act
        ResponseEntity<List<MissionResponseDto>> response = missionController.getAvailableMissions(userId);

        // Assert
        assertAll("Verificações de lista vazia",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertTrue(response.getBody().isEmpty())
        );
        
        verify(missionService).getAvailableMissions(userId);
    }

    @Test
    @DisplayName("Deve completar missão com sucesso")
    void deveCompletarMissaoComSucesso() {
        // Arrange
        Long missionId = 1L;
        Long userId = 1L;
          MissionCompletionResponseDto resposta = new MissionCompletionResponseDto(
            "Missão 'Treinar Combate' completada! +50 XP. Parabéns, você subiu para o nível 2!",
            50,
            150,
            2,
            true,
            "Treinar Combate"
        );
        
        when(missionService.completeMission(missionId, userId)).thenReturn(resposta);        // Act
        ResponseEntity<MissionCompletionResponseDto> response = missionController.completeMission(missionId, userId);

        // Assert
        assertAll("Verificações de missão completada",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("Treinar Combate", response.getBody().getMissionName()),
            () -> assertEquals(50, response.getBody().getXpGained()),
            () -> assertEquals(2, response.getBody().getCurrentLevel()),
            () -> assertTrue(response.getBody().getLevelUp())
        );
        
        verify(missionService).completeMission(missionId, userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando tentar completar missão inexistente")
    void deveLancarExcecaoQuandoTentarCompletarMissaoInexistente() {
        // Arrange
        Long missionId = 999L;
        Long userId = 1L;
        
        when(missionService.completeMission(missionId, userId))
            .thenThrow(new ResourceNotFoundException("Missão não encontrada"));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> missionController.completeMission(missionId, userId)
        );
        
        assertEquals("Missão não encontrada", exception.getMessage());
        verify(missionService).completeMission(missionId, userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não tiver nível suficiente")
    void deveLancarExcecaoQuandoUsuarioNaoTiverNivelSuficiente() {
        // Arrange
        Long missionId = 1L;
        Long userId = 1L;
        
        when(missionService.completeMission(missionId, userId))
            .thenThrow(new BadRequestException("Nível insuficiente para esta missão. Necessário: 5"));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> missionController.completeMission(missionId, userId)
        );
        
        assertEquals("Nível insuficiente para esta missão. Necessário: 5", exception.getMessage());
        verify(missionService).completeMission(missionId, userId);
    }

    @Test
    @DisplayName("Deve retornar histórico de missões do usuário")
    void deveRetornarHistoricoDeMissoesDoUsuario() {
        // Arrange
        Long userId = 1L;
        List<MissionHistoryDto> historico = Arrays.asList(
            createMissionHistoryDto("Treinar Combate", 50, LocalDateTime.now().minusDays(2)),
            createMissionHistoryDto("Coletar Recursos", 30, LocalDateTime.now().minusDays(1)),
            createMissionHistoryDto("Explorar Território", 100, LocalDateTime.now())
        );
        
        when(missionService.getUserMissionHistory(userId)).thenReturn(historico);

        // Act
        ResponseEntity<List<MissionHistoryDto>> response = missionController.getMissionHistory(userId);

        // Assert
        assertAll("Verificações do histórico",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals(3, response.getBody().size()),
            () -> assertEquals("Treinar Combate", response.getBody().get(0).getMissionName()),
            () -> assertEquals(50, response.getBody().get(0).getXpGained())
        );
        
        verify(missionService).getUserMissionHistory(userId);
    }

    @Test
    @DisplayName("Deve retornar histórico vazio quando usuário não completou missões")
    void deveRetornarHistoricoVazioQuandoUsuarioNaoCompletouMissoes() {
        // Arrange
        Long userId = 1L;
        List<MissionHistoryDto> historicoVazio = Collections.emptyList();
        
        when(missionService.getUserMissionHistory(userId)).thenReturn(historicoVazio);

        // Act
        ResponseEntity<List<MissionHistoryDto>> response = missionController.getMissionHistory(userId);

        // Assert
        assertAll("Verificações de histórico vazio",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertTrue(response.getBody().isEmpty())
        );
        
        verify(missionService).getUserMissionHistory(userId);
    }

    @Test
    @DisplayName("Deve criar nova missão com sucesso")
    void deveCriarNovaMissaoComSucesso() {
        // Arrange
        MissionRequestDto requestDto = new MissionRequestDto();
        requestDto.setName("Nova Missão");
        requestDto.setDescription("Descrição da nova missão");
        requestDto.setXpReward(75);
        requestDto.setRequiredLevel(3);
        requestDto.setIsRepeatable(true);
        
        Mission missaoCriada = new Mission();
        missaoCriada.setId(1L);
        missaoCriada.setName("Nova Missão");
        missaoCriada.setDescription("Descrição da nova missão");
        missaoCriada.setXpReward(75);
        missaoCriada.setRequiredLevel(3);
        missaoCriada.setIsRepeatable(true);
        missaoCriada.setIsActive(true);
        
        when(missionService.createMission(any(Mission.class))).thenReturn(missaoCriada);

        // Act
        ResponseEntity<Mission> response = missionController.createMission(requestDto);

        // Assert
        assertAll("Verificações da criação de missão",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("Nova Missão", response.getBody().getName()),
            () -> assertEquals(75, response.getBody().getXpReward()),
            () -> assertEquals(3, response.getBody().getRequiredLevel()),
            () -> assertTrue(response.getBody().getIsRepeatable()),
            () -> assertTrue(response.getBody().getIsActive())
        );
        
        verify(missionService).createMission(any(Mission.class));
    }

    @Test
    @DisplayName("Deve listar todas as missões do sistema")
    void deveListarTodasAsMissoesDoSistema() {
        // Arrange
        List<Mission> todasMissoes = Arrays.asList(
            createMission(1L, "Missão 1", true),
            createMission(2L, "Missão 2", false),
            createMission(3L, "Missão 3", true)
        );
        
        when(missionService.listAllMissions()).thenReturn(todasMissoes);

        // Act
        ResponseEntity<List<Mission>> response = missionController.getAllMissions();

        // Assert
        assertAll("Verificações de listagem de todas as missões",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals(3, response.getBody().size()),
            () -> assertEquals("Missão 1", response.getBody().get(0).getName()),
            () -> assertTrue(response.getBody().get(0).getIsActive()),
            () -> assertFalse(response.getBody().get(1).getIsActive())
        );
        
        verify(missionService).listAllMissions();
    }

    @Test
    @DisplayName("Deve reinicializar missões com sucesso")
    void deveReinicializarMissoesComSucesso() {
        // Arrange
        doNothing().when(missionService).resetMissions();

        // Act
        ResponseEntity<String> response = missionController.resetMissions();

        // Assert
        assertAll("Verificações de reset de missões",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals("Missões reinicializadas com sucesso!", response.getBody())
        );
        
        verify(missionService).resetMissions();
    }

    @Test
    @DisplayName("Deve lançar exceção quando serviço falhar ao resetar missões")
    void deveLancarExcecaoQuandoServicoFalharAoResetarMissoes() {
        // Arrange
        doThrow(new RuntimeException("Erro interno"))
            .when(missionService).resetMissions();

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> missionController.resetMissions()
        );
        
        assertEquals("Erro interno", exception.getMessage());
        verify(missionService).resetMissions();
    }

    private MissionResponseDto createMissionResponseDto(Long id, String name, int xpReward, int requiredLevel) {
        MissionResponseDto dto = new MissionResponseDto();
        dto.setId(id);
        dto.setName(name);
        dto.setXpReward(xpReward);
        dto.setRequiredLevel(requiredLevel);
        return dto;
    }

    private MissionHistoryDto createMissionHistoryDto(String missionName, int xpGained, LocalDateTime completedAt) {
        MissionHistoryDto dto = new MissionHistoryDto();
        dto.setMissionName(missionName);
        dto.setXpGained(xpGained);
        dto.setCompletedAt(completedAt);
        return dto;
    }

    private Mission createMission(Long id, String name, boolean isActive) {
        Mission mission = new Mission();
        mission.setId(id);
        mission.setName(name);
        mission.setIsActive(isActive);
        return mission;
    }
}

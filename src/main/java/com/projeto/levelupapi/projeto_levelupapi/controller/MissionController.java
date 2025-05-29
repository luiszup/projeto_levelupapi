package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.dto.MissionCompletionResponseDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.MissionRequestDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.MissionResponseDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.MissionHistoryDto;
import com.projeto.levelupapi.projeto_levelupapi.model.Mission;
import com.projeto.levelupapi.projeto_levelupapi.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@Tag(name = "🎯 Sistema de Missões", description = "Missões que os jogadores podem completar para ganhar XP")
@SecurityRequirement(name = "bearerAuth")
public class MissionController {
    
    private final MissionService missionService;
    
    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }
    
    @GetMapping("/available/{userId}")
    @Operation(
        summary = "Listar missões disponíveis",
        description = "Retorna todas as missões que o jogador pode completar baseado no seu nível atual. " +
                     "Missões já completadas (não repetíveis) não aparecem na lista."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de missões disponíveis retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<List<MissionResponseDto>> getAvailableMissions(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId) {
        
        List<MissionResponseDto> missions = missionService.getAvailableMissions(userId);
        return ResponseEntity.ok(missions);
    }
    
    @PostMapping("/{missionId}/complete/{userId}")
    @Operation(
        summary = "Completar missão",
        description = "Completa uma missão instantaneamente e concede o XP correspondente. " +
                     "IMPORTANTE: Verifica se o jogador tem nível suficiente e se a missão pode ser repetida."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Missão completada com sucesso - XP concedido automaticamente"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Erro nas regras de negócio",
            content = @Content(
                examples = {
                    @ExampleObject(name = "Nível insuficiente", value = "Nível insuficiente para esta missão. Necessário: 3"),
                    @ExampleObject(name = "Missão já completada", value = "Você já completou esta missão"),
                    @ExampleObject(name = "Missão inativa", value = "Esta missão não está mais disponível")
                }
            )
        ),
        @ApiResponse(responseCode = "404", description = "Jogador ou missão não encontrada"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<MissionCompletionResponseDto> completeMission(
        @Parameter(description = "ID único da missão", required = true)
        @PathVariable Long missionId,
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId) {
        
        MissionCompletionResponseDto result = missionService.completeMission(missionId, userId);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/history/{userId}")
    @Operation(
        summary = "Histórico de missões completadas",
        description = "Retorna o histórico de todas as missões já completadas pelo jogador, ordenadas por data."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso"),        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<List<MissionHistoryDto>> getMissionHistory(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId) {
        
        List<MissionHistoryDto> history = missionService.getUserMissionHistory(userId);
        return ResponseEntity.ok(history);
    }
    
    @PostMapping
    @Operation(
        summary = "Criar nova missão (Admin)",
        description = "Cria uma nova missão no sistema que os jogadores podem completar. " +
                     "Apenas administradores podem criar missões."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Missão criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos - verifique os campos obrigatórios"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores")
    })
    public ResponseEntity<Mission> createMission(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da nova missão",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                        "name": "Treinar Combate",
                        "description": "Pratique suas habilidades de combate por 30 minutos",
                        "xpReward": 50,
                        "requiredLevel": 1,
                        "isRepeatable": false
                    }
                    """
                )
            )
        )
        @Valid @RequestBody MissionRequestDto missionRequestDto) {
        
        Mission mission = new Mission();
        mission.setName(missionRequestDto.getName());
        mission.setDescription(missionRequestDto.getDescription());
        mission.setXpReward(missionRequestDto.getXpReward());
        mission.setRequiredLevel(missionRequestDto.getRequiredLevel());
        mission.setIsRepeatable(missionRequestDto.getIsRepeatable());
        mission.setIsActive(true);
        
        Mission created = missionService.createMission(mission);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping
    @Operation(
        summary = "Listar todas as missões (Admin)",
        description = "Lista todas as missões do sistema, incluindo inativas. Apenas para administradores."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de missões retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores")
    })
    public ResponseEntity<List<Mission>> getAllMissions() {
        List<Mission> missions = missionService.listAllMissions();
        return ResponseEntity.ok(missions);
    }
    
    @PostMapping("/reset")
    @Operation(
        summary = "Reinicializar missões (Admin/Teste)",
        description = "Remove todas as missões e cria as missões iniciais novamente. " +
                     "Útil para testes e desenvolvimento."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Missões reinicializadas com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores")
    })
    public ResponseEntity<String> resetMissions() {
        missionService.resetMissions();
        return ResponseEntity.ok("Missões reinicializadas com sucesso!");
    }
}

package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.dto.AuthResponseDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.JwtResponse;
import com.projeto.levelupapi.projeto_levelupapi.dto.LoginRequestDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.UserRequestDto;
import com.projeto.levelupapi.projeto_levelupapi.infra.jwt.JwtTokenProvider;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "🔐 Autenticação", description = "Endpoints para autenticação de jogadores no sistema RPG")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Realizar login no sistema",
        description = "Autentica um jogador no sistema e retorna um token JWT" +
                     "O token deve ser usado no header Authorization: Bearer {token}"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Login realizado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas - usuário ou senha incorretos"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos - verifique o formato dos dados")
    })
    public ResponseEntity<JwtResponse> authenticateUser(
        @Valid @RequestBody LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),
                        loginRequestDto.getPassword()
                )
        );
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtResponse(jwt));
    }
}

package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.exception.BadRequestException;
import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.service.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ItemController")
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    @Test
    @DisplayName("Deve criar novo item com sucesso")
    void deveCriarNovoItemComSucesso() {
        // Arrange
        Map<String, String> body = Map.of(
            "name", "Espada de Ferro",
            "description", "Uma espada resistente forjada em ferro puro"
        );
          Item itemCriado = new Item();
        itemCriado.setId(1L);
        itemCriado.setName("Espada de Ferro");
        itemCriado.setDescription("Uma espada resistente forjada em ferro puro");
        
        when(itemService.createItem("Espada de Ferro", "Uma espada resistente forjada em ferro puro"))
            .thenReturn(itemCriado);        // Act
        ResponseEntity<Item> response = itemController.createItem(body);

        // Assert
        assertAll("Verificações da criação de item",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals(1L, response.getBody().getId()),
            () -> assertEquals("Espada de Ferro", response.getBody().getName()),
            () -> assertEquals("Uma espada resistente forjada em ferro puro", response.getBody().getDescription())
        );
        
        verify(itemService).createItem("Espada de Ferro", "Uma espada resistente forjada em ferro puro");
    }

    @Test
    @DisplayName("Deve criar item com campos nulos quando não fornecidos")
    void deveCriarItemComCamposNulosQuandoNaoFornecidos() {        // Arrange
        Map<String, String> body = new HashMap<>(); // Body vazio
        
        Item itemCriado = new Item();
        itemCriado.setId(1L);
        
        when(itemService.createItem(null, null)).thenReturn(itemCriado);

        // Act
        ResponseEntity<Item> response = itemController.createItem(body);

        // Assert
        assertAll("Verificações da criação com campos nulos",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals(1L, response.getBody().getId())
        );
        
        verify(itemService).createItem(null, null);
    }    @Test
    @DisplayName("Deve propagar exceção quando serviço falhar na criação")
    void devePropagarExcecaoQuandoServicoFalharNaCriacao() {
        // Arrange
        Map<String, String> body = Map.of(
            "name", "Item Inválido",
            "description", "Descrição inválida"
        );
        
        when(itemService.createItem("Item Inválido", "Descrição inválida"))
            .thenThrow(new BadRequestException("Nome do item já existe"));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> itemController.createItem(body)
        );
        
        assertEquals("Nome do item já existe", exception.getMessage());
        verify(itemService).createItem("Item Inválido", "Descrição inválida");
    }

    @Test
    @DisplayName("Deve listar todos os itens com sucesso")
    void deveListarTodosOsItensComSucesso() {
        // Arrange
        List<Item> itens = Arrays.asList(
            createItem(1L, "Espada de Ferro", "Espada resistente"),
            createItem(2L, "Poção de Cura", "Restaura vida"),
            createItem(3L, "Escudo de Bronze", "Proteção básica"),
            createItem(4L, "Arco Élfico", "Arco de longo alcance")
        );
        
        when(itemService.listAll()).thenReturn(itens);

        // Act
        List<Item> response = itemController.getAllItems();

        // Assert
        assertAll("Verificações da listagem de itens",
            () -> assertNotNull(response),
            () -> assertEquals(4, response.size()),
            () -> assertEquals("Espada de Ferro", response.get(0).getName()),
            () -> assertEquals("Poção de Cura", response.get(1).getName()),
            () -> assertEquals("Escudo de Bronze", response.get(2).getName()),
            () -> assertEquals("Arco Élfico", response.get(3).getName())
        );
        
        verify(itemService).listAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver itens")
    void deveRetornarListaVaziaQuandoNaoHouverItens() {
        // Arrange
        List<Item> itensVazios = Collections.emptyList();
        when(itemService.listAll()).thenReturn(itensVazios);

        // Act
        List<Item> response = itemController.getAllItems();

        // Assert
        assertAll("Verificações da lista vazia",
            () -> assertNotNull(response),
            () -> assertTrue(response.isEmpty()),
            () -> assertEquals(0, response.size())
        );
        
        verify(itemService).listAll();
    }

    @Test
    @DisplayName("Deve listar itens paginados com sucesso")
    void deveListarItensPaginadosComSucesso() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        
        List<Item> itens = Arrays.asList(
            createItem(1L, "Espada Lendária", "Arma poderosa"),
            createItem(2L, "Poção Maior", "Cura superior")
        );
        
        Page<Item> paginaItens = new PageImpl<>(itens, pageable, 15); // Total de 15 itens
        
        when(itemService.listAll(pageable)).thenReturn(paginaItens);

        // Act
        Page<Item> response = itemController.getAllItemsPaged(pageable);

        // Assert
        assertAll("Verificações da paginação",
            () -> assertNotNull(response),
            () -> assertEquals(2, response.getContent().size()),
            () -> assertEquals(0, response.getNumber()),
            () -> assertEquals(10, response.getSize()),
            () -> assertEquals(15, response.getTotalElements()),
            () -> assertEquals(2, response.getTotalPages()),
            () -> assertEquals("Espada Lendária", response.getContent().get(0).getName()),
            () -> assertEquals("Poção Maior", response.getContent().get(1).getName())
        );
        
        verify(itemService).listAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não houver itens para paginação")
    void deveRetornarPaginaVaziaQuandoNaoHouverItensParaPaginacao() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Item> itensVazios = Collections.emptyList();
        Page<Item> paginaVazia = new PageImpl<>(itensVazios, pageable, 0);
        
        when(itemService.listAll(pageable)).thenReturn(paginaVazia);        // Act
        Page<Item> response = itemController.getAllItemsPaged(pageable);

        // Assert
        assertAll("Verificações da página vazia",
            () -> assertNotNull(response),
            () -> assertTrue(response.getContent().isEmpty()),
            () -> assertEquals(0, response.getTotalElements()),
            () -> assertEquals(0, response.getTotalPages()),
            () -> assertEquals(0, response.getNumber())
        );
        
        verify(itemService).listAll(pageable);
    }

    @Test
    @DisplayName("Deve listar itens da segunda página corretamente")
    void deveListarItensDaSegundaPaginaCorretamente() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 5); // Segunda página, 5 itens por página
        
        List<Item> itensSegundaPagina = Arrays.asList(
            createItem(6L, "Machado de Guerra", "Arma pesada"),
            createItem(7L, "Anel Mágico", "Aumenta magia"),
            createItem(8L, "Botas Rápidas", "Aumenta velocidade")
        );
        
        Page<Item> paginaItens = new PageImpl<>(itensSegundaPagina, pageable, 13); // Total de 13 itens
        
        when(itemService.listAll(pageable)).thenReturn(paginaItens);

        // Act
        Page<Item> response = itemController.getAllItemsPaged(pageable);

        // Assert
        assertAll("Verificações da segunda página",
            () -> assertNotNull(response),
            () -> assertEquals(3, response.getContent().size()),
            () -> assertEquals(1, response.getNumber()), // Página 1 (0-indexed)
            () -> assertEquals(5, response.getSize()),
            () -> assertEquals(13, response.getTotalElements()),
            () -> assertEquals(3, response.getTotalPages()), // 13 items / 5 per page = 3 pages
            () -> assertEquals("Machado de Guerra", response.getContent().get(0).getName()),
            () -> assertEquals("Anel Mágico", response.getContent().get(1).getName()),
            () -> assertEquals("Botas Rápidas", response.getContent().get(2).getName())
        );
        
        verify(itemService).listAll(pageable);
    }    @Test
    @DisplayName("Deve propagar exceção quando serviço falhar na listagem")
    void devePropagarExcecaoQuandoServicoFalharNaListagem() {
        // Arrange
        when(itemService.listAll()).thenThrow(new RuntimeException("Erro de banco de dados"));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> itemController.getAllItems()
        );
        
        assertEquals("Erro de banco de dados", exception.getMessage());
        verify(itemService).listAll();
    }    @Test
    @DisplayName("Deve propagar exceção quando serviço falhar na listagem paginada")
    void devePropagarExcecaoQuandoServicoFalharNaListagemPaginada() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(itemService.listAll(pageable)).thenThrow(new RuntimeException("Erro de paginação"));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> itemController.getAllItemsPaged(pageable)
        );
        
        assertEquals("Erro de paginação", exception.getMessage());
        verify(itemService).listAll(pageable);
    }    private Item createItem(Long id, String name, String description) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        return item;
    }
}

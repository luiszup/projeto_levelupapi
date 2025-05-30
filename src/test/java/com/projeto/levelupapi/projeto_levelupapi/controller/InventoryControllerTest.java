package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.exception.BadRequestException;
import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.InventoryItem;
import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.service.InventoryService;
import com.projeto.levelupapi.projeto_levelupapi.service.UserService;
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
@DisplayName("Testes do InventoryController")
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private InventoryController inventoryController;

    @Test
    @DisplayName("Deve retornar inventário do usuário com sucesso")
    void deveRetornarInventarioDoUsuarioComSucesso() {        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("João");
        
        List<InventoryItem> inventario = Arrays.asList(
            createInventoryItem("Espada de Ferro", 1),
            createInventoryItem("Poção de Cura", 5),
            createInventoryItem("Escudo de Bronze", 1)
        );
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(inventoryService.getInventory(user)).thenReturn(inventario);

        // Act
        ResponseEntity<List<InventoryItem>> response = inventoryController.getInventory(userId);

        // Assert
        assertAll("Verificações do inventário",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals(3, response.getBody().size()),
            () -> assertEquals("Espada de Ferro", response.getBody().get(0).getItem().getName()),
            () -> assertEquals(5, response.getBody().get(1).getQuantity())
        );
        
        verify(userService).findById(userId);
        verify(inventoryService).getInventory(user);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado para visualizar inventário")
    void deveLancarExcecaoQuandoUsuarioNaoForEncontradoParaVisualizarInventario() {
        // Arrange
        Long userId = 999L;
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryController.getInventory(userId)
        );
        
        assertEquals("Usuário com ID 999 não encontrado", exception.getMessage());
        verify(userService).findById(userId);
        verify(inventoryService, never()).getInventory(any());
    }    @Test
    @DisplayName("Deve retornar inventário paginado com sucesso")
    void deveRetornarInventarioPaginadoComSucesso() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("João");
        
        Pageable pageable = PageRequest.of(0, 10);
        
        List<InventoryItem> itens = Arrays.asList(
            createInventoryItem("Poção de Cura", 3),
            createInventoryItem("Espada Mágica", 1)
        );
        Page<InventoryItem> paginaInventario = new PageImpl<>(itens, pageable, itens.size());
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(inventoryService.listInventory(user, pageable)).thenReturn(paginaInventario);

        // Act
        Page<InventoryItem> response = inventoryController.getInventoryPaged(userId, pageable);

        // Assert
        assertAll("Verificações do inventário paginado",
            () -> assertNotNull(response),
            () -> assertEquals(2, response.getContent().size()),
            () -> assertEquals(0, response.getNumber()),
            () -> assertEquals(10, response.getSize()),
            () -> assertEquals(2, response.getTotalElements())
        );
        
        verify(userService).findById(userId);
        verify(inventoryService).listInventory(user, pageable);
    }    @Test
    @DisplayName("Deve adicionar item ao inventário com sucesso")
    void deveAdicionarItemAoInventarioComSucesso() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("João");
        
        Map<String, Object> body = Map.of(
            "itemName", "Poção de Vida",
            "quantity", 3
        );
        
        InventoryItem itemAdicionado = createInventoryItem("Poção de Vida", 3);
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(inventoryService.addItem(user, "Poção de Vida", 3)).thenReturn(itemAdicionado);

        // Act
        ResponseEntity<InventoryItem> response = inventoryController.addItem(userId, body);

        // Assert
        assertAll("Verificações da adição de item",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("Poção de Vida", response.getBody().getItem().getName()),
            () -> assertEquals(3, response.getBody().getQuantity())
        );
        
        verify(userService).findById(userId);
        verify(inventoryService).addItem(user, "Poção de Vida", 3);
    }

    @Test
    @DisplayName("Deve adicionar item com quantidade padrão quando não especificada")
    void deveAdicionarItemComQuantidadePadraoQuandoNaoEspecificada() {        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("João");
        
        Map<String, Object> body = Map.of("itemName", "Espada Simples");
        
        InventoryItem itemAdicionado = createInventoryItem("Espada Simples", 1);
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(inventoryService.addItem(user, "Espada Simples", 1)).thenReturn(itemAdicionado);

        // Act
        ResponseEntity<InventoryItem> response = inventoryController.addItem(userId, body);

        // Assert
        assertAll("Verificações da adição com quantidade padrão",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("Espada Simples", response.getBody().getItem().getName()),
            () -> assertEquals(1, response.getBody().getQuantity())
        );
        
        verify(userService).findById(userId);
        verify(inventoryService).addItem(user, "Espada Simples", 1);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome do item não for fornecido para adicionar")
    void deveLancarExcecaoQuandoNomeDoItemNaoForFornecidoParaAdicionar() {
        // Arrange
        Long userId = 1L;
        Map<String, Object> body = Map.of("quantity", 5); // Sem itemName

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> inventoryController.addItem(userId, body)
        );
        
        assertEquals("Nome do item é obrigatório", exception.getMessage());
        verify(userService, never()).findById(any());
        verify(inventoryService, never()).addItem(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade for zero ou negativa para adicionar")
    void deveLancarExcecaoQuandoQuantidadeForZeroOuNegativaParaAdicionar() {
        // Arrange
        Long userId = 1L;
        Map<String, Object> body = Map.of(
            "itemName", "Poção de Vida",
            "quantity", 0
        );

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> inventoryController.addItem(userId, body)
        );
        
        assertEquals("Quantidade deve ser maior que zero", exception.getMessage());
        verify(userService, never()).findById(any());
        verify(inventoryService, never()).addItem(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade for inválida para adicionar")
    void deveLancarExcecaoQuandoQuantidadeForInvalidaParaAdicionar() {
        // Arrange
        Long userId = 1L;
        Map<String, Object> body = Map.of(
            "itemName", "Poção de Vida",
            "quantity", "abc"
        );

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> inventoryController.addItem(userId, body)
        );
        
        assertEquals("Quantidade inválida", exception.getMessage());
        verify(userService, never()).findById(any());
        verify(inventoryService, never()).addItem(any(), any(), anyInt());
    }    @Test
    @DisplayName("Deve remover item do inventário com sucesso")
    void deveRemoverItemDoInventarioComSucesso() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("João");
        
        Map<String, Object> body = Map.of(
            "itemName", "Poção de Vida",
            "quantity", 2
        );
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<Void> response = inventoryController.removeItem(userId, body);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        verify(userService).findById(userId);
        verify(inventoryService).removeItem(user, "Poção de Vida", 2);
    }    @Test
    @DisplayName("Deve remover item com quantidade padrão quando não especificada")
    void deveRemoverItemComQuantidadePadraoQuandoNaoEspecificada() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("João");
        
        Map<String, Object> body = Map.of("itemName", "Espada Antiga");
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<Void> response = inventoryController.removeItem(userId, body);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        verify(userService).findById(userId);
        verify(inventoryService).removeItem(user, "Espada Antiga", 1);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome do item não for fornecido para remover")
    void deveLancarExcecaoQuandoNomeDoItemNaoForFornecidoParaRemover() {
        // Arrange
        Long userId = 1L;
        Map<String, Object> body = Map.of("quantity", 2); // Sem itemName

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> inventoryController.removeItem(userId, body)
        );
        
        assertEquals("Nome do item é obrigatório", exception.getMessage());
        verify(userService, never()).findById(any());
        verify(inventoryService, never()).removeItem(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade for zero ou negativa para remover")
    void deveLancarExcecaoQuandoQuantidadeForZeroOuNegativaParaRemover() {
        // Arrange
        Long userId = 1L;
        Map<String, Object> body = Map.of(
            "itemName", "Poção de Vida",
            "quantity", -1
        );

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> inventoryController.removeItem(userId, body)
        );
        
        assertEquals("Quantidade deve ser maior que zero", exception.getMessage());
        verify(userService, never()).findById(any());
        verify(inventoryService, never()).removeItem(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado para adicionar item")
    void deveLancarExcecaoQuandoUsuarioNaoForEncontradoParaAdicionarItem() {
        // Arrange
        Long userId = 999L;
        Map<String, Object> body = Map.of("itemName", "Qualquer Item");
        
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryController.addItem(userId, body)
        );
        
        assertEquals("Usuário com ID 999 não encontrado", exception.getMessage());
        verify(userService).findById(userId);
        verify(inventoryService, never()).addItem(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado para remover item")
    void deveLancarExcecaoQuandoUsuarioNaoForEncontradoParaRemoverItem() {
        // Arrange
        Long userId = 999L;
        Map<String, Object> body = Map.of("itemName", "Qualquer Item");
        
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryController.removeItem(userId, body)
        );
        
        assertEquals("Usuário com ID 999 não encontrado", exception.getMessage());
        verify(userService).findById(userId);
        verify(inventoryService, never()).removeItem(any(), any(), anyInt());
    }    @Test
    @DisplayName("Deve falhar ao adicionar item com quantidade zero")
    void deveFalharAoAdicionarItemComQuantidadeZero() {
        // Arrange
        Long userId = 1L;
        Map<String, Object> body = Map.of("itemName", "Poção", "quantity", 0);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> inventoryController.addItem(userId, body)
        );
        
        assertEquals("Quantidade deve ser maior que zero", exception.getMessage());
        verifyNoInteractions(userService, inventoryService);
    }    @Test
    @DisplayName("Deve falhar ao adicionar item com quantidade negativa")
    void deveFalharAoAdicionarItemComQuantidadeNegativa() {
        // Arrange
        Long userId = 1L;
        Map<String, Object> body = Map.of("itemName", "Poção", "quantity", -5);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> inventoryController.addItem(userId, body)
        );
        
        assertEquals("Quantidade deve ser maior que zero", exception.getMessage());
        verifyNoInteractions(userService, inventoryService);
    }    @Test
    @DisplayName("Deve falhar ao remover item com quantidade zero")
    void deveFalharAoRemoverItemComQuantidadeZero() {
        // Arrange
        Long userId = 1L;
        Map<String, Object> body = Map.of("itemName", "Poção", "quantity", 0);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> inventoryController.removeItem(userId, body)
        );
        
        assertEquals("Quantidade deve ser maior que zero", exception.getMessage());
        verifyNoInteractions(userService, inventoryService);
    }    @Test
    @DisplayName("Deve falhar ao remover item com quantidade negativa")
    void deveFalharAoRemoverItemComQuantidadeNegativa() {
        // Arrange
        Long userId = 1L;
        Map<String, Object> body = Map.of("itemName", "Poção", "quantity", -3);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> inventoryController.removeItem(userId, body)
        );
        
        assertEquals("Quantidade deve ser maior que zero", exception.getMessage());
        verifyNoInteractions(userService, inventoryService);
    }    @Test
    @DisplayName("Deve propagar exceção quando usuário não existe ao adicionar item")
    void devePropagarExcecaoQuandoUsuarioNaoExisteAoAdicionarItem() {
        // Arrange
        Long userId = 999L;
        Map<String, Object> body = Map.of("itemName", "Poção", "quantity", 1);
        
        when(userService.findById(userId))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryController.addItem(userId, body)
        );
        
        assertEquals("Usuário com ID 999 não encontrado", exception.getMessage());
        verify(userService).findById(userId);
        verifyNoInteractions(inventoryService);
    }    @Test
    @DisplayName("Deve propagar exceção quando usuário não existe ao remover item")
    void devePropagarExcecaoQuandoUsuarioNaoExisteAoRemoverItem() {
        // Arrange
        Long userId = 999L;
        Map<String, Object> body = Map.of("itemName", "Poção", "quantity", 1);
        
        when(userService.findById(userId))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryController.removeItem(userId, body)
        );
        
        assertEquals("Usuário com ID 999 não encontrado", exception.getMessage());
        verify(userService).findById(userId);
        verifyNoInteractions(inventoryService);
    }    @Test
    @DisplayName("Deve propagar exceção do service ao adicionar item")
    void devePropagarExcecaoDoServiceAoAdicionarItem() {
        // Arrange
        Long userId = 1L;
        Map<String, Object> body = Map.of("itemName", "Item Inexistente", "quantity", 1);
        
        User user = new User();
        user.setId(userId);
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(inventoryService.addItem(user, "Item Inexistente", 1))
            .thenThrow(new ResourceNotFoundException("Item with name 'Item Inexistente' not found"));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryController.addItem(userId, body)
        );
        
        assertEquals("Item with name 'Item Inexistente' not found", exception.getMessage());
        verify(userService).findById(userId);
        verify(inventoryService).addItem(user, "Item Inexistente", 1);
    }    @Test
    @DisplayName("Deve propagar exceção do service ao remover item")
    void devePropagarExcecaoDoServiceAoRemoverItem() {
        // Arrange
        Long userId = 1L;
        Map<String, Object> body = Map.of("itemName", "Item Inexistente", "quantity", 1);
        
        User user = new User();
        user.setId(userId);
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        doThrow(new ResourceNotFoundException("Item 'Item Inexistente' is not in the user's inventory"))
            .when(inventoryService).removeItem(user, "Item Inexistente", 1);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryController.removeItem(userId, body)
        );
        
        assertEquals("Item 'Item Inexistente' is not in the user's inventory", exception.getMessage());
        verify(userService).findById(userId);
        verify(inventoryService).removeItem(user, "Item Inexistente", 1);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando usuário não tem itens paginados")
    void deveRetornarPaginaVaziaQuandoUsuarioNaoTemItensPaginados() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        
        User user = new User();
        user.setId(userId);
        
        Page<InventoryItem> paginaVazia = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(inventoryService.listInventory(user, pageable)).thenReturn(paginaVazia);        // Act
        Page<InventoryItem> response = 
            inventoryController.getInventoryPaged(userId, pageable);

        // Assert
        assertAll("Verificações da página vazia",
            () -> assertNotNull(response),
            () -> assertTrue(response.getContent().isEmpty()),
            () -> assertEquals(0, response.getTotalElements())
        );
        
        verify(userService).findById(userId);
        verify(inventoryService).listInventory(user, pageable);
    }

    @Test
    @DisplayName("Deve retornar inventário vazio quando usuário não tem itens")
    void deveRetornarInventarioVazioQuandoUsuarioNaoTemItens() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(inventoryService.getInventory(user)).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<InventoryItem>> response = 
            inventoryController.getInventory(userId);

        // Assert
        assertAll("Verificações do inventário vazio",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertTrue(response.getBody().isEmpty())
        );
        
        verify(userService).findById(userId);
        verify(inventoryService).getInventory(user);
    }
    private InventoryItem createInventoryItem(String itemName, int quantity) {
        Item item = new Item();
        item.setName(itemName);
        
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setItem(item);
        inventoryItem.setQuantity(quantity);
        return inventoryItem;
    }
}

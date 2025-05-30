package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.exception.BadRequestException;
import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.InventoryItem;
import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.repository.InventoryItemRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.ItemRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("Deve retornar inventário do usuário")
    void shouldReturnUserInventory() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("player1");

        Item item1 = new Item();
        item1.setName("Espada de Ferro");

        InventoryItem inventoryItem1 = new InventoryItem();
        inventoryItem1.setUser(user);
        inventoryItem1.setItem(item1);
        inventoryItem1.setQuantity(1);

        List<InventoryItem> expectedInventory = Arrays.asList(inventoryItem1);
        when(inventoryRepository.findByUser(user)).thenReturn(expectedInventory);

        // Act
        List<InventoryItem> result = inventoryService.getInventory(user);

        // Assert
        assertEquals(expectedInventory.size(), result.size());
        assertEquals(expectedInventory, result);
        verify(inventoryRepository).findByUser(user);
    }

    @Test
    @DisplayName("Deve listar inventário com paginação")
    void shouldListInventoryWithPagination() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("player1");

        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setUser(user);
        inventoryItem.setQuantity(5);

        List<InventoryItem> items = Arrays.asList(inventoryItem);
        Page<InventoryItem> inventoryPage = new PageImpl<>(items);
        Pageable pageable = PageRequest.of(0, 10);

        when(inventoryRepository.findByUser(user, pageable)).thenReturn(inventoryPage);

        // Act
        Page<InventoryItem> result = inventoryService.listInventory(user, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(5, result.getContent().get(0).getQuantity());
        verify(inventoryRepository).findByUser(user, pageable);
    }

    @Test
    @DisplayName("Deve adicionar novo item ao inventário")
    void shouldAddNewItemToInventory() {
        // Arrange
        User user = new User();
        user.setId(1L);
        
        String itemName = "Poção de Cura";
        int quantity = 3;
        
        Item item = new Item();
        item.setId(1L);
        item.setName(itemName);
        
        InventoryItem savedInventoryItem = new InventoryItem();
        savedInventoryItem.setUser(user);
        savedInventoryItem.setItem(item);
        savedInventoryItem.setQuantity(quantity);

        when(itemRepository.findByName(itemName)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByUserAndItem(user, item)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(savedInventoryItem);

        // Act
        InventoryItem result = inventoryService.addItem(user, itemName, quantity);

        // Assert
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(item, result.getItem());
        assertEquals(quantity, result.getQuantity());
        verify(itemRepository).findByName(itemName);
        verify(inventoryRepository).findByUserAndItem(user, item);
        verify(inventoryRepository).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("Deve aumentar quantidade de item existente no inventário")
    void shouldIncreaseQuantityOfExistingItem() {
        // Arrange
        User user = new User();
        user.setId(1L);
        
        String itemName = "Poção de Cura";
        int additionalQuantity = 2;
        int existingQuantity = 3;
        
        Item item = new Item();
        item.setId(1L);
        item.setName(itemName);
        
        InventoryItem existingInventoryItem = new InventoryItem();
        existingInventoryItem.setUser(user);
        existingInventoryItem.setItem(item);
        existingInventoryItem.setQuantity(existingQuantity);

        when(itemRepository.findByName(itemName)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByUserAndItem(user, item)).thenReturn(Optional.of(existingInventoryItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(existingInventoryItem);

        // Act
        InventoryItem result = inventoryService.addItem(user, itemName, additionalQuantity);

        // Assert
        assertNotNull(result);
        assertEquals(existingQuantity + additionalQuantity, result.getQuantity());
        verify(inventoryRepository).save(existingInventoryItem);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando item não existe")
    void shouldThrowResourceNotFoundExceptionWhenItemDoesNotExist() {
        // Arrange
        User user = new User();
        String itemName = "Item Inexistente";
        int quantity = 1;

        when(itemRepository.findByName(itemName)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            inventoryService.addItem(user, itemName, quantity);
        });

        assertTrue(exception.getMessage().contains("not found"));
        verify(itemRepository).findByName(itemName);
        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando quantidade é inválida")
    void shouldThrowBadRequestExceptionWhenQuantityIsInvalid() {
        // Arrange
        User user = new User();
        String itemName = "Poção de Cura";

        // Act & Assert
        assertAll(
            () -> assertThrows(BadRequestException.class, () -> {
                inventoryService.addItem(user, itemName, 0);
            }),
            () -> assertThrows(BadRequestException.class, () -> {
                inventoryService.addItem(user, itemName, -1);
            })
        );

        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("Deve remover quantidade específica do item")
    void shouldRemoveSpecificQuantityFromItem() {
        // Arrange
        User user = new User();
        String itemName = "Poção de Cura";
        int quantityToRemove = 2;
        int existingQuantity = 5;
        
        Item item = new Item();
        item.setName(itemName);
        
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setUser(user);
        inventoryItem.setItem(item);
        inventoryItem.setQuantity(existingQuantity);

        when(itemRepository.findByName(itemName)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByUserAndItem(user, item)).thenReturn(Optional.of(inventoryItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(inventoryItem);

        // Act
        inventoryService.removeItem(user, itemName, quantityToRemove);

        // Assert
        assertEquals(existingQuantity - quantityToRemove, inventoryItem.getQuantity());
        verify(inventoryRepository).save(inventoryItem);
        verify(inventoryRepository, never()).delete(any(InventoryItem.class));
    }

    @Test
    @DisplayName("Deve remover item completamente quando quantidade é maior ou igual ao estoque")
    void shouldRemoveItemCompletelyWhenQuantityIsGreaterOrEqualToStock() {
        // Arrange
        User user = new User();
        String itemName = "Poção de Cura";
        int quantityToRemove = 5;
        int existingQuantity = 3;
        
        Item item = new Item();
        item.setName(itemName);
        
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setUser(user);
        inventoryItem.setItem(item);
        inventoryItem.setQuantity(existingQuantity);

        when(itemRepository.findByName(itemName)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByUserAndItem(user, item)).thenReturn(Optional.of(inventoryItem));
        doNothing().when(inventoryRepository).delete(inventoryItem);

        // Act
        inventoryService.removeItem(user, itemName, quantityToRemove);

        // Assert
        verify(inventoryRepository).delete(inventoryItem);
        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando tentar remover item inexistente do inventário")
    void shouldThrowResourceNotFoundExceptionWhenRemovingNonExistentInventoryItem() {
        // Arrange
        User user = new User();
        String itemName = "Poção de Cura";
        int quantity = 1;
        
        Item item = new Item();
        item.setName(itemName);

        when(itemRepository.findByName(itemName)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByUserAndItem(user, item)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            inventoryService.removeItem(user, itemName, quantity);
        });

        assertTrue(exception.getMessage().contains("not in the user's inventory"));
        verify(inventoryRepository, never()).delete(any(InventoryItem.class));
        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando quantidade a remover é inválida")
    void shouldThrowBadRequestExceptionWhenRemoveQuantityIsInvalid() {
        // Arrange
        User user = new User();
        String itemName = "Poção de Cura";

        // Act & Assert
        assertAll(
            () -> assertThrows(BadRequestException.class, () -> {
                inventoryService.removeItem(user, itemName, 0);
            }),
            () -> assertThrows(BadRequestException.class, () -> {
                inventoryService.removeItem(user, itemName, -1);
            })
        );

        verify(inventoryRepository, never()).delete(any(InventoryItem.class));
        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("Deve falhar ao adicionar item inexistente")
    void deveFalharAoAdicionarItemInexistente() {
        // Arrange
        User user = new User();
        user.setId(1L);
        String itemName = "Item Inexistente";
        int quantity = 1;
        
        when(itemRepository.findByName(itemName)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryService.addItem(user, itemName, quantity)
        );
        
        assertEquals("Item with name 'Item Inexistente' not found", exception.getMessage());
        verify(itemRepository).findByName(itemName);
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Deve falhar ao remover item inexistente")
    void deveFalharAoRemoverItemInexistente() {
        // Arrange
        User user = new User();
        user.setId(1L);
        String itemName = "Item Inexistente";
        int quantity = 1;
        
        when(itemRepository.findByName(itemName)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryService.removeItem(user, itemName, quantity)
        );
        
        assertEquals("Item with name 'Item Inexistente' not found", exception.getMessage());
        verify(itemRepository).findByName(itemName);
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Deve falhar ao remover item que usuário não possui")
    void deveFalharAoRemoverItemQueUsuarioNaoPossui() {
        // Arrange
        User user = new User();
        user.setId(1L);
        String itemName = "Espada de Ferro";
        int quantity = 1;
        
        Item item = new Item();
        item.setName(itemName);
        
        when(itemRepository.findByName(itemName)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByUserAndItem(user, item)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryService.removeItem(user, itemName, quantity)
        );
        
        assertEquals("Item 'Espada de Ferro' is not in the user's inventory", exception.getMessage());
        verify(itemRepository).findByName(itemName);
        verify(inventoryRepository).findByUserAndItem(user, item);
        verify(inventoryRepository, never()).delete(any());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve adicionar novo item ao inventário quando usuário não o possui")
    void deveAdicionarNovoItemAoInventarioQuandoUsuarioNaoOPossui() {
        // Arrange
        User user = new User();
        user.setId(1L);
        String itemName = "Nova Poção";
        int quantity = 5;
        
        Item item = new Item();
        item.setName(itemName);
        
        InventoryItem newInventoryItem = new InventoryItem();
        newInventoryItem.setUser(user);
        newInventoryItem.setItem(item);
        newInventoryItem.setQuantity(quantity);
        
        when(itemRepository.findByName(itemName)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByUserAndItem(user, item)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(newInventoryItem);

        // Act
        InventoryItem result = inventoryService.addItem(user, itemName, quantity);

        // Assert
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(item, result.getItem());
        assertEquals(quantity, result.getQuantity());
        
        verify(itemRepository).findByName(itemName);
        verify(inventoryRepository).findByUserAndItem(user, item);
        verify(inventoryRepository).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("Deve remover todo o item do inventário quando quantidade for igual")
    void deveRemoverTodoOItemDoInventarioQuandoQuantidadeForIgual() {
        // Arrange
        User user = new User();
        user.setId(1L);
        String itemName = "Poção";
        int currentQuantity = 3;
        int removeQuantity = 3;
        
        Item item = new Item();
        item.setName(itemName);
        
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setUser(user);
        inventoryItem.setItem(item);
        inventoryItem.setQuantity(currentQuantity);
        
        when(itemRepository.findByName(itemName)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByUserAndItem(user, item)).thenReturn(Optional.of(inventoryItem));

        // Act
        inventoryService.removeItem(user, itemName, removeQuantity);

        // Assert
        verify(itemRepository).findByName(itemName);
        verify(inventoryRepository).findByUserAndItem(user, item);
        verify(inventoryRepository).delete(inventoryItem);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve reduzir quantidade do item quando remoção é parcial")
    void deveReduzirQuantidadeDoItemQuandoRemocaoEParcial() {
        // Arrange
        User user = new User();
        user.setId(1L);
        String itemName = "Poção";
        int currentQuantity = 5;
        int removeQuantity = 2;
        
        Item item = new Item();
        item.setName(itemName);
        
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setUser(user);
        inventoryItem.setItem(item);
        inventoryItem.setQuantity(currentQuantity);
        
        when(itemRepository.findByName(itemName)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByUserAndItem(user, item)).thenReturn(Optional.of(inventoryItem));
        when(inventoryRepository.save(inventoryItem)).thenReturn(inventoryItem);

        // Act
        inventoryService.removeItem(user, itemName, removeQuantity);

        // Assert
        assertEquals(3, inventoryItem.getQuantity()); // 5 - 2 = 3
        
        verify(itemRepository).findByName(itemName);
        verify(inventoryRepository).findByUserAndItem(user, item);
        verify(inventoryRepository).save(inventoryItem);
        verify(inventoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve retornar página vazia quando usuário não tem itens")
    void deveRetornarPaginaVaziaQuandoUsuarioNaoTemItens() {
        // Arrange
        User user = new User();
        user.setId(1L);
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<InventoryItem> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(inventoryRepository.findByUser(user, pageable)).thenReturn(emptyPage);

        // Act
        Page<InventoryItem> result = inventoryService.listInventory(user, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        
        verify(inventoryRepository).findByUser(user, pageable);
    }

    @Test
    @DisplayName("Deve retornar inventário vazio quando usuário não tem itens")
    void deveRetornarInventarioVazioQuandoUsuarioNaoTemItens() {
        // Arrange
        User user = new User();
        user.setId(1L);
        
        when(inventoryRepository.findByUser(user)).thenReturn(Arrays.asList());

        // Act
        List<InventoryItem> result = inventoryService.getInventory(user);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(inventoryRepository).findByUser(user);
    }
}

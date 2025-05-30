package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.exception.BadRequestException;
import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceAlreadyExistsException;
import com.projeto.levelupapi.projeto_levelupapi.model.Item;
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
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    @DisplayName("Deve criar item com sucesso")
    void shouldCreateItemSuccessfully() {
        // Arrange
        String name = "Espada de Ferro";
        String description = "Uma espada resistente feita de ferro";
        
        Item savedItem = new Item();
        savedItem.setId(1L);
        savedItem.setName(name);
        savedItem.setDescription(description);

        when(itemRepository.findByName(name)).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        // Act
        Item result = itemService.createItem(name, description);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        verify(itemRepository).findByName(name);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceAlreadyExistsException quando item já existe")
    void shouldThrowResourceAlreadyExistsExceptionWhenItemExists() {
        // Arrange
        String name = "Espada de Ferro";
        String description = "Uma espada resistente";
        
        Item existingItem = new Item();
        existingItem.setName(name);
        
        when(itemRepository.findByName(name)).thenReturn(Optional.of(existingItem));

        // Act & Assert
        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class, () -> {
            itemService.createItem(name, description);
        });

        assertTrue(exception.getMessage().contains("já existe"));
        verify(itemRepository).findByName(name);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando nome é nulo ou vazio")
    void shouldThrowBadRequestExceptionWhenNameIsNullOrEmpty() {
        // Arrange & Act & Assert
        assertAll(
            () -> assertThrows(BadRequestException.class, () -> {
                itemService.createItem(null, "descrição");
            }),
            () -> assertThrows(BadRequestException.class, () -> {
                itemService.createItem("", "descrição");
            }),
            () -> assertThrows(BadRequestException.class, () -> {
                itemService.createItem("   ", "descrição");
            })
        );

        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    @DisplayName("Deve listar todos os itens")
    void shouldListAllItems() {
        // Arrange
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Espada de Ferro");

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Poção de Cura");

        List<Item> expectedItems = Arrays.asList(item1, item2);
        when(itemRepository.findAll()).thenReturn(expectedItems);

        // Act
        List<Item> result = itemService.listAll();

        // Assert
        assertEquals(expectedItems.size(), result.size());
        assertEquals(expectedItems, result);
        verify(itemRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar itens com paginação")
    void shouldListItemsWithPagination() {
        // Arrange
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Espada de Ferro");

        List<Item> items = Arrays.asList(item1);
        Page<Item> itemPage = new PageImpl<>(items);
        Pageable pageable = PageRequest.of(0, 10);

        when(itemRepository.findAll(pageable)).thenReturn(itemPage);

        // Act
        Page<Item> result = itemService.listAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Espada de Ferro", result.getContent().get(0).getName());
        verify(itemRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar item por nome")
    void shouldFindItemByName() {
        // Arrange
        String itemName = "Espada de Ferro";
        Item item = new Item();
        item.setId(1L);
        item.setName(itemName);

        when(itemRepository.findByName(itemName)).thenReturn(Optional.of(item));

        // Act
        Optional<Item> result = itemService.findByName(itemName);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(itemName, result.get().getName());
        verify(itemRepository).findByName(itemName);
    }

    @Test
    @DisplayName("Deve retornar itens disponíveis para nível 1")
    void shouldReturnAvailableItemsForLevel1() {
        // Act
        List<String> result = itemService.getAvailableItemsForLevel(1);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("Poção de Cura"));
        assertTrue(result.contains("Pão Simples"));
        assertTrue(result.contains("Adaga de Madeira"));
    }    @Test
    @DisplayName("Deve retornar itens disponíveis para nível 5")
    void shouldReturnAvailableItemsForLevel5() {
        // Act
        List<String> result = itemService.getAvailableItemsForLevel(5);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Deve conter itens do nível 1
        assertTrue(result.contains("Poção de Cura"));
        
        // Deve conter itens do nível 2
        assertTrue(result.contains("Espada de Ferro"));
        
        // Deve conter itens do nível 5
        assertTrue(result.contains("Espada Flamejante"));
        assertTrue(result.contains("Escudo Mágico"));
    }

    @Test
    @DisplayName("Deve retornar itens épicos para nível 50")
    void shouldReturnEpicItemsForLevel50() {
        // Act
        List<String> result = itemService.getAvailableItemsForLevel(50);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("Espada do Caos"));
        assertTrue(result.contains("Armadura do Tempo"));
        assertTrue(result.contains("Anel da Realidade"));
    }

    @Test
    @DisplayName("Deve retornar lista vazia para nível 0 ou negativo")
    void shouldReturnEmptyListForZeroOrNegativeLevel() {
        // Act
        List<String> result0 = itemService.getAvailableItemsForLevel(0);
        List<String> resultNegative = itemService.getAvailableItemsForLevel(-1);

        // Assert
        assertNotNull(result0);
        assertTrue(result0.isEmpty());
        
        assertNotNull(resultNegative);
        assertTrue(resultNegative.isEmpty());
    }

    @Test
    @DisplayName("Deve falhar ao criar item com nome vazio")
    void deveFalharAoCriarItemComNomeVazio() {
        // Arrange
        String name = "";
        String description = "Descrição válida";

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> itemService.createItem(name, description)
        );
        
        assertEquals("O nome do item não pode ser nulo ou vazio", exception.getMessage());
        verifyNoInteractions(itemRepository);
    }

    @Test
    @DisplayName("Deve falhar ao criar item com nome apenas espacos")
    void deveFalharAoCriarItemComNomeApenasEspacos() {
        // Arrange
        String name = "   ";
        String description = "Descrição válida";

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> itemService.createItem(name, description)
        );
        
        assertEquals("O nome do item não pode ser nulo ou vazio", exception.getMessage());
        verifyNoInteractions(itemRepository);
    }

    @Test
    @DisplayName("Deve criar item com descrição nula")
    void deveCriarItemComDescricaoNula() {
        // Arrange
        String name = "Item Válido";
        String description = null;
        
        Item savedItem = new Item();
        savedItem.setId(1L);
        savedItem.setName(name);
        savedItem.setDescription(description);

        when(itemRepository.findByName(name)).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        // Act
        Item result = itemService.createItem(name, description);

        // Assert
        assertAll("Verificações do item criado",
            () -> assertNotNull(result),
            () -> assertEquals(1L, result.getId()),
            () -> assertEquals(name, result.getName()),
            () -> assertNull(result.getDescription())
        );
        
        verify(itemRepository).findByName(name);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando item não existe")
    void deveRetornarOptionalVazioQuandoItemNaoExiste() {
        // Arrange
        String name = "Item Inexistente";
        when(itemRepository.findByName(name)).thenReturn(Optional.empty());

        // Act
        Optional<Item> result = itemService.findByName(name);

        // Assert
        assertFalse(result.isPresent());
        verify(itemRepository).findByName(name);
    }

    @Test
    @DisplayName("Deve retornar item quando encontrado por nome")
    void deveRetornarItemQuandoEncontradoPorNome() {
        // Arrange
        String name = "Espada de Ferro";
        Item item = new Item();
        item.setId(1L);
        item.setName(name);
        
        when(itemRepository.findByName(name)).thenReturn(Optional.of(item));

        // Act
        Optional<Item> result = itemService.findByName(name);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(name, result.get().getName());
        verify(itemRepository).findByName(name);
    }

    @Test
    @DisplayName("Deve retornar itens para nível 4")
    void deveRetornarItensParaNivel4() {
        // Act
        List<String> result = itemService.getAvailableItemsForLevel(4);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("Armadura de Ferro"));
        assertTrue(result.contains("Botas de Velocidade"));
        assertTrue(result.contains("Elmo Protetor"));
    }

    @Test
    @DisplayName("Deve retornar itens para nível 5")
    void deveRetornarItensParaNivel5() {
        // Act
        List<String> result = itemService.getAvailableItemsForLevel(5);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("Espada Flamejante"));
        assertTrue(result.contains("Escudo Mágico"));
        assertTrue(result.contains("Anel de Poder"));
    }

    @Test
    @DisplayName("Deve retornar itens para nível muito alto")
    void deveRetornarItensParaNivelMuitoAlto() {
        // Act
        List<String> result = itemService.getAvailableItemsForLevel(100);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        // Para níveis muito altos, deve retornar os itens de nível 5
        assertTrue(result.contains("Espada Flamejante"));
        assertTrue(result.contains("Escudo Mágico"));
        assertTrue(result.contains("Anel de Poder"));
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há itens")
    void deveRetornarPaginaVaziaQuandoNaoHaItens() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(itemRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<Item> result = itemService.listAll(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        
        verify(itemRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve listar todas as páginas corretamente")
    void deveListarTodasAsPaginasCorretamente() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2); // Segunda página, 2 itens por página
        
        Item item1 = new Item();
        item1.setId(3L);
        item1.setName("Item 3");
        
        Item item2 = new Item();
        item2.setId(4L);
        item2.setName("Item 4");
        
        List<Item> items = Arrays.asList(item1, item2);
        Page<Item> page = new PageImpl<>(items, pageable, 10); // Total de 10 itens
        
        when(itemRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Item> result = itemService.listAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getNumber()); // Página 1 (0-indexed)
        assertEquals(2, result.getSize());
        assertEquals(10, result.getTotalElements());
        assertEquals(5, result.getTotalPages()); // 10 items / 2 per page = 5 pages
        
        verify(itemRepository).findAll(pageable);
    }
}

package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.Role;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.model.InventoryItem;
import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.repository.InventoryItemRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.UserRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.XpRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.ItemRepository;
import com.projeto.levelupapi.projeto_levelupapi.service.InventoryService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private XpRepository xpRepository;    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private UserService userService;@Test
    @DisplayName("Deve listar todos os usuários")
    void shouldListAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("player1");
        user1.setRole(Role.USER);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("admin");
        user2.setRole(Role.ADMIN);

        List<User> expectedUsers = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> result = userService.listAll();

        // Assert
        assertEquals(expectedUsers.size(), result.size());
        assertEquals(expectedUsers, result);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar usuários com paginação")
    void shouldListUsersWithPagination() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("player1");

        List<User> users = Arrays.asList(user1);
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<User> result = userService.listAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("player1", result.getContent().get(0).getUsername());
        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar usuário por ID quando existir")
    void shouldFindUserByIdWhenExists() {
        // Arrange
        Long userId = 1L;
        User user = new User();        user.setId(userId);
        user.setUsername("testplayer");
        user.setRole(Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("testplayer", result.get().getUsername());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando buscar por ID inexistente")
    void shouldReturnEmptyOptionalWhenUserIdDoesNotExist() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        // Arrange
        User newUser = new User();
        newUser.setUsername("newplayer");
        newUser.setPassword("password123");
        newUser.setRole(Role.USER);
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newplayer");
        savedUser.setPassword("hashedpassword");
        savedUser.setRole(Role.USER);
        savedUser.setInSafeZone(true);

        // Mock items para os itens iniciais
        Item mapaItem = new Item();
        mapaItem.setId(1L);
        mapaItem.setName("Mapa do Jogo");
        
        Item pocaoItem = new Item();
        pocaoItem.setId(2L);
        pocaoItem.setName("Poção de Cura");
        
        Item espadaItem = new Item();
        espadaItem.setId(3L);
        espadaItem.setName("Espada de Madeira");
        
        Item escudoItem = new Item();
        escudoItem.setId(4L);
        escudoItem.setName("Escudo de Couro");

        when(passwordEncoder.encode("password123")).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Mock dos itens que serão criados/encontrados
        when(itemRepository.findByName("Mapa do Jogo")).thenReturn(Optional.of(mapaItem));
        when(itemRepository.findByName("Poção de Cura")).thenReturn(Optional.of(pocaoItem));
        when(itemRepository.findByName("Espada de Madeira")).thenReturn(Optional.of(espadaItem));
        when(itemRepository.findByName("Escudo de Couro")).thenReturn(Optional.of(escudoItem));
        
        // Mock do InventoryService
        when(inventoryService.addItem(any(User.class), anyString(), anyInt())).thenReturn(new InventoryItem());

        // Act
        User result = userService.create(newUser);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("newplayer", result.getUsername());
        assertTrue(result.isInSafeZone());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(inventoryService, times(4)).addItem(any(User.class), anyString(), anyInt());
    }@Test
    @DisplayName("Deve excluir usuário com limpeza de dados relacionados")
    void shouldDeleteUserWithRelatedDataCleanup() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("player1");

        List<InventoryItem> inventoryItems = Arrays.asList(new InventoryItem());
        Xp xpData = new Xp();
        xpData.setUser(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(inventoryItemRepository.findByUser(user)).thenReturn(inventoryItems);
        when(xpRepository.findByUserId(userId)).thenReturn(Optional.of(xpData));
        doNothing().when(inventoryItemRepository).deleteAll(inventoryItems);
        doNothing().when(xpRepository).delete(xpData);
        doNothing().when(userRepository).delete(user);

        // Act
        assertDoesNotThrow(() -> userService.delete(userId));

        // Assert
        verify(userRepository).findById(userId);
        verify(inventoryItemRepository).findByUser(user);
        verify(inventoryItemRepository).deleteAll(inventoryItems);
        verify(xpRepository).findByUserId(userId);
        verify(xpRepository).delete(xpData);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando excluir usuário inexistente")
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentUser() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.delete(userId);
        });        assertTrue(exception.getMessage().contains("User with ID"));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }
}

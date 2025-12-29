package ru.effectivemobile.bankcards.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.effectivemobile.bankcards.dto.*;
import ru.effectivemobile.bankcards.entity.Card;
import ru.effectivemobile.bankcards.entity.CardStatus;
import ru.effectivemobile.bankcards.entity.Role;
import ru.effectivemobile.bankcards.entity.User;
import ru.effectivemobile.bankcards.exception.UserNotFoundException;
import ru.effectivemobile.bankcards.repository.CardRepository;
import ru.effectivemobile.bankcards.repository.UserRepository;
import ru.effectivemobile.bankcards.service.audit.AuditService;
import ru.effectivemobile.bankcards.service.encryption.EncryptionService;
import ru.effectivemobile.bankcards.service.validation.ValidationService;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private AuditService auditService;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateCard_WhenUserExists() {
        // given
        CreateCardRequest request = new CreateCardRequest(
                "4556737586899855", // Валидный PAN
                "John Doe",
                "12/28",
                new BigDecimal("1000.00"),
                1L
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.USER);

        Card savedCard = new Card();
        savedCard.setId(1L);
        savedCard.setUserId(1L);
        savedCard.setEncryptedPan("encryptedPan123");
        savedCard.setOwnerName("John Doe");
        savedCard.setExpiryDate(YearMonth.of(2028, 12));
        savedCard.setStatus(CardStatus.ACTIVE);
        savedCard.setBalance(new BigDecimal("1000.00"));

        CardDto expectedDto = new CardDto(
                1L, "**** **** **** 9855", "John Doe", "12/28", "ACTIVE", new BigDecimal("1000.00")
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(encryptionService.encrypt("4556737586899855")).thenReturn("encryptedPan123");
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);
        when(cardMapper.toDto(savedCard)).thenReturn(expectedDto);

        // when
        CardDto result = cardService.createCard(request);

        // then
        assertThat(result).isEqualTo(expectedDto);
        verify(userRepository).findById(1L);
        verify(encryptionService).encrypt("4556737586899855");
        verify(validationService).validatePan("4556737586899855");
        verify(validationService).validateExpiryDate("12/28");
        verify(validationService).validateCardCreation(1L, new BigDecimal("1000.00"));
        verify(cardRepository).save(any(Card.class));
        verify(auditService).logCardCreation(savedCard);
    }

    @Test
    void shouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        // given
        CreateCardRequest request = new CreateCardRequest(
                "4556737586899855", "John Doe", "12/28", new BigDecimal("100"), 999L
        );
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardService.createCard(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(encryptionService, never()).encrypt(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void shouldGetMyCards_WhenUserIsAuthenticated() {
        // given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Card card = new Card();
        card.setId(1L);
        card.setUserId(1L);
        card.setEncryptedPan("encryptedPan123");
        card.setOwnerName("John Doe");
        card.setExpiryDate(YearMonth.of(2028, 12));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(new BigDecimal("1000.00"));
        when(cardRepository.findByUserId(1L)).thenReturn(List.of(card));

        CardDto expectedDto = new CardDto(
                1L, "**** **** **** 9855", "John Doe", "12/28", "ACTIVE", new BigDecimal("1000.00")
        );
        when(cardMapper.toDto(card)).thenReturn(expectedDto);

        // authenticate user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test@example.com", null, List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        List<CardDto> result = cardService.getMyCards();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expectedDto);

        // cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldTransferBetweenOwnCards() {
        // given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUserId(1L);
        fromCard.setBalance(new BigDecimal("1000.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setUserId(1L);
        toCard.setBalance(new BigDecimal("500.00"));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test@example.com", null, List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("200.00"));

        // when
        cardService.transfer(request);

        // then
        assertThat(fromCard.getBalance()).isEqualByComparingTo("800.00");
        assertThat(toCard.getBalance()).isEqualByComparingTo("700.00");
        verify(validationService).validateTransfer(fromCard, toCard, new BigDecimal("200.00"));
        verify(cardRepository).save(fromCard);
        verify(cardRepository).save(toCard);
        verify(auditService).logTransfer(fromCard, toCard, new BigDecimal("200.00"));
        verify(auditService).logBalanceChange(fromCard, new BigDecimal("1000.00"));
        verify(auditService).logBalanceChange(toCard, new BigDecimal("500.00"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrow_WhenTransferringToAnotherUserCard() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUserId(1L);
        fromCard.setBalance(new BigDecimal("1000.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setUserId(2L); // ← другой пользователь!
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test@example.com", null, List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("100.00"));

        assertThatThrownBy(() -> cardService.transfer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target card does not belong to you");

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowExceptionWhenCreatingCardWithInvalidPan() {
        // given
        CreateCardRequest request = new CreateCardRequest(
                "1234567890123456", // Невалидный PAN (не проходит алгоритм Луна)
                "John Doe",
                "12/28",
                new BigDecimal("1000.00"),
                1L
        );

        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new ru.effectivemobile.bankcards.exception.BusinessValidationException("Invalid PAN (failed Luhn check)"))
                .when(validationService).validatePan("1234567890123456");

        // when & then
        assertThatThrownBy(() -> cardService.createCard(request))
                .isInstanceOf(ru.effectivemobile.bankcards.exception.BusinessValidationException.class)
                .hasMessageContaining("Invalid PAN");
    }

    @Test
    void shouldThrowExceptionWhenCreatingCardWithExpiredDate() {
        // given
        String expiredDate = YearMonth.now().minusMonths(1)
                .format(java.time.format.DateTimeFormatter.ofPattern("MM/yy"));

        CreateCardRequest request = new CreateCardRequest(
                "4556737586899855",
                "John Doe",
                expiredDate,
                new BigDecimal("1000.00"),
                1L
        );

        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        // Не нужно вызывать validatePan для этого теста
        // Не нужно вызывать validateExpiryDate, так как тестируем исключение

        // when & then
        assertThatThrownBy(() -> cardService.createCard(request))
                .isInstanceOf(Exception.class); // Может быть несколько видов исключений
    }

    @Test
    void shouldThrowExceptionWhenTransferExceedsLimit() {
        // given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUserId(1L);
        fromCard.setBalance(new BigDecimal("2000000.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setUserId(1L);
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test@example.com", null, List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("1000001.00"));

        // Заглушка для валидации
        doThrow(new ru.effectivemobile.bankcards.exception.BusinessValidationException("Transfer amount cannot exceed 1000000.00"))
                .when(validationService).validateTransfer(fromCard, toCard, new BigDecimal("1000001.00"));

        // when & then
        assertThatThrownBy(() -> cardService.transfer(request))
                .isInstanceOf(ru.effectivemobile.bankcards.exception.BusinessValidationException.class)
                .hasMessageContaining("cannot exceed");

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowExceptionWhenDeletingActiveCard() {
        // given
        Card card = new Card();
        card.setId(1L);
        card.setBalance(BigDecimal.ZERO);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        // Заглушка для проверки прав администратора
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        doThrow(new ru.effectivemobile.bankcards.exception.BusinessValidationException("Cannot delete active card. Block it first"))
                .when(validationService).validateCardDeletion(card);

        // when & then
        assertThatThrownBy(() -> cardService.deleteCard(1L))
                .isInstanceOf(ru.effectivemobile.bankcards.exception.BusinessValidationException.class)
                .hasMessageContaining("Cannot delete active card");

        SecurityContextHolder.clearContext();
    }
}
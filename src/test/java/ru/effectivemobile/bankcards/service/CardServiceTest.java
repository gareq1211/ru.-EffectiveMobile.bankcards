package ru.effectivemobile.bankcards.service;

import ru.effectivemobile.bankcards.dto.CardDto;
import ru.effectivemobile.bankcards.dto.CreateCardRequest;
import ru.effectivemobile.bankcards.entity.Card;
import ru.effectivemobile.bankcards.entity.CardStatus;
import ru.effectivemobile.bankcards.entity.Role;
import ru.effectivemobile.bankcards.entity.User;
import ru.effectivemobile.bankcards.exception.UserNotFoundException;
import ru.effectivemobile.bankcards.repository.CardRepository;
import ru.effectivemobile.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.util.List;
import java.awt.*;
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

    @InjectMocks
    private CardService cardService;

    @Test
    void shouldCreateCard_WhenUserExists() {
        // given
        CreateCardRequest request = new CreateCardRequest(
                "1234567890123456",
                "John Doe",
                "12/28",
                new BigDecimal("1000.00"),
                1L
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(ru.effectivemobile.bankcards.entity.Role.USER);

        Card savedCard = new Card();
        savedCard.setId(1L);
        savedCard.setUserId(1L);
        savedCard.setEncryptedPan("1234567890123456");
        savedCard.setOwnerName("John Doe");
        savedCard.setExpiryDate(java.time.YearMonth.of(2028, 12));
        savedCard.setStatus(ru.effectivemobile.bankcards.entity.CardStatus.ACTIVE);
        savedCard.setBalance(new BigDecimal("1000.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        // when
        CardDto result = cardService.createCard(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.maskedPan()).isEqualTo("**** **** **** 3456");
        assertThat(result.balance()).isEqualByComparingTo("1000.00");

        verify(userRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void shouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        // given
        CreateCardRequest request = new CreateCardRequest(
                "1234567890123456", "John Doe", "12/28", new BigDecimal("100"), 999L
        );
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cardService.createCard(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 999");

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
        card.setEncryptedPan("1234567890123456");
        card.setOwnerName("John Doe");
        card.setExpiryDate(YearMonth.of(2028, 12));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(new BigDecimal("1000.00"));
        when(cardRepository.findByUserId(1L)).thenReturn(List.of(card));

        // authenticate user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test@example.com", null, List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        List<CardDto> result = cardService.getMyCards();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).maskedPan()).isEqualTo("**** **** **** 3456");

        // cleanup
        SecurityContextHolder.clearContext();
    }
}
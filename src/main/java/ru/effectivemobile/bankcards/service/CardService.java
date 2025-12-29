package ru.effectivemobile.bankcards.service;

import ru.effectivemobile.bankcards.dto.CardDto;
import ru.effectivemobile.bankcards.dto.CreateCardRequest;
import ru.effectivemobile.bankcards.dto.CardMapper;
import ru.effectivemobile.bankcards.dto.TransferRequest;
import ru.effectivemobile.bankcards.entity.Card;
import ru.effectivemobile.bankcards.entity.CardStatus;
import ru.effectivemobile.bankcards.entity.User;
import ru.effectivemobile.bankcards.exception.UserNotFoundException;
import ru.effectivemobile.bankcards.repository.CardRepository;
import ru.effectivemobile.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.stream.Collectors;
import java.awt.*;
import java.time.YearMonth;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardDto createCard(CreateCardRequest request) {
        // 1. Проверяем, что пользователь с таким ID существует
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.userId()));

        // 2. Создаём карту
        Card card = new Card();
        card.setUserId(user.getId());
        // ⚠️ TODO: Зашифровать PAN перед сохранением
        card.setEncryptedPan(request.pan()); // временно сохраняем как есть
        card.setOwnerName(request.ownerName());
        card.setExpiryDate(YearMonth.parse(request.expiryDate(), java.time.format.DateTimeFormatter.ofPattern("MM/yy")));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(request.initialBalance());

        // 3. Сохраняем
        Card savedCard = cardRepository.save(card);

        // 4. Возвращаем DTO
        return CardMapper.toDto(savedCard);
        }
    public List<CardDto> getMyCards() {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentEmail));

        List<Card> cards = cardRepository.findByUserId(user.getId());
        return cards.stream()
                .map(CardMapper::toDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public void transfer(TransferRequest request) {
        // 1. Получаем текущего пользователя
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentEmail));

        Long userId = user.getId();

        // 2. Находим карты
        Card fromCard = cardRepository.findById(request.fromCardId())
                .orElseThrow(() -> new IllegalArgumentException("Source card not found"));

        Card toCard = cardRepository.findById(request.toCardId())
                .orElseThrow(() -> new IllegalArgumentException("Target card not found"));

        // 3. Проверяем, что обе карты принадлежат пользователю
        if (!fromCard.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Source card does not belong to you");
        }
        if (!toCard.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Target card does not belong to you");
        }

        // 4. Проверяем, что это не одна и та же карта
        if (fromCard.getId().equals(toCard.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }

        // 5. Проверяем статус и баланс
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Source card is not active");
        }
        if (fromCard.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        // 6. Выполняем перевод (в одной транзакции)
        fromCard.setBalance(fromCard.getBalance().subtract(request.amount()));
        toCard.setBalance(toCard.getBalance().add(request.amount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}

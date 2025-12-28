package ru.effectivemobile.bankcards.service;

import ru.effectivemobile.bankcards.dto.CardDto;
import ru.effectivemobile.bankcards.dto.CreateCardRequest;
import ru.effectivemobile.bankcards.dto.CardMapper;
import ru.effectivemobile.bankcards.entity.Card;
import ru.effectivemobile.bankcards.entity.CardStatus;
import ru.effectivemobile.bankcards.entity.User;
import ru.effectivemobile.bankcards.exception.UserNotFoundException;
import ru.effectivemobile.bankcards.repository.CardRepository;
import ru.effectivemobile.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    /**
     * Создаёт новую банковскую карту.
     * Доступно только администраторам (проверка прав — в контроллере или security).
     */
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
}
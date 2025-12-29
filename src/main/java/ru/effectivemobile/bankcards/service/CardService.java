package ru.effectivemobile.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.effectivemobile.bankcards.dto.*;
import ru.effectivemobile.bankcards.entity.Card;
import ru.effectivemobile.bankcards.entity.CardStatus;
import ru.effectivemobile.bankcards.entity.User;
import ru.effectivemobile.bankcards.exception.CardNotFoundException;
import ru.effectivemobile.bankcards.exception.CardNotActiveException;
import ru.effectivemobile.bankcards.exception.InsufficientFundsException;
import ru.effectivemobile.bankcards.exception.UserNotFoundException;
import ru.effectivemobile.bankcards.repository.CardRepository;
import ru.effectivemobile.bankcards.repository.UserRepository;
import ru.effectivemobile.bankcards.service.encryption.EncryptionService;
import ru.effectivemobile.bankcards.service.audit.AuditService;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import ru.effectivemobile.bankcards.service.validation.ValidationService;

@Service
@RequiredArgsConstructor
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final CardMapper cardMapper;
    private final AuditService auditService;
    private final ValidationService validationService;

    public CardDto createCard(CreateCardRequest request) {
        // ✅ Валидация бизнес-правил
        validationService.validatePan(request.pan());
        validationService.validateExpiryDate(request.expiryDate());
        validationService.validateCardCreation(request.userId(), request.initialBalance());

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.userId()));

        Card card = new Card();
        card.setUserId(user.getId());

        String encryptedPan = encryptionService.encrypt(request.pan());
        card.setEncryptedPan(encryptedPan);

        card.setOwnerName(request.ownerName());
        card.setExpiryDate(YearMonth.parse(request.expiryDate(), java.time.format.DateTimeFormatter.ofPattern("MM/yy")));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(request.initialBalance());

        Card savedCard = cardRepository.save(card);

        auditService.logCardCreation(savedCard);
        return cardMapper.toDto(savedCard);
    }

    public List<CardDto> getMyCards() {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentEmail));

        List<Card> cards = cardRepository.findByUserId(user.getId());
        return cards.stream()
                .map(cardMapper::toDto)
                .collect(Collectors.toList());
    }

    public CardDto getCardById(Long cardId) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentEmail));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!card.getUserId().equals(user.getId()) && !isAdmin()) {
            throw new CardNotFoundException(cardId);
        }

        return cardMapper.toDto(card);
    }

    public Page<CardDto> getMyCardsWithFilter(CardFilterRequest filter) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentEmail));

        Page<Card> cards;
        Pageable pageable = filter.toPageable();

        if (filter.status() != null) {
            cards = cardRepository.findByUserIdAndStatus(user.getId(), filter.status(), pageable);
        } else {
            cards = cardRepository.findByUserId(user.getId(), pageable);
        }

        return cards.map(cardMapper::toDto);
    }

    public Page<CardDto> getAllCards(CardFilterRequest filter) {
        if (!isAdmin()) {
            throw new IllegalArgumentException("Access denied");
        }

        Page<Card> cards;
        Pageable pageable = filter.toPageable();

        if (filter.userId() != null && filter.status() != null) {
            cards = cardRepository.findByUserIdAndStatus(filter.userId(), filter.status(), pageable);
        } else if (filter.userId() != null) {
            User user = userRepository.findById(filter.userId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + filter.userId()));
            cards = cardRepository.findByUserId(filter.userId(), pageable);
        } else if (filter.status() != null) {
            cards = cardRepository.findByStatus(filter.status(), pageable);
        } else {
            cards = cardRepository.findAll(pageable);
        }

        return cards.map(cardMapper::toDto);
    }

    public CardDto updateCardStatus(Long cardId, UpdateCardStatusRequest request) {
        if (!isAdmin()) {
            throw new IllegalArgumentException("Access denied");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        // ✅ Валидация изменения статуса
        validationService.validateCardStatusChange(card, request.status());

        CardStatus oldStatus = card.getStatus();
        card.setStatus(request.status());
        Card updatedCard = cardRepository.save(card);

        if (!oldStatus.equals(request.status())) {
            auditService.logStatusChange(updatedCard, oldStatus);
        }

        return cardMapper.toDto(updatedCard);
    }

    public void requestCardBlock(Long cardId) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentEmail));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!card.getUserId().equals(user.getId())) {
            throw new CardNotFoundException(cardId);
        }

        System.out.println("User " + user.getEmail() + " requested block for card " + cardId);
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        // ✅ Логируем запрос на блокировку
        auditService.logBlockRequest(card);
    }

    public void deleteCard(Long cardId) {
        if (!isAdmin()) {
            throw new IllegalArgumentException("Access denied");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        // ✅ Валидация удаления
        validationService.validateCardDeletion(card);

        cardRepository.delete(card);
    }

    @Transactional
    public void transfer(TransferRequest request) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentEmail));

        Long userId = user.getId();

        Card fromCard = cardRepository.findById(request.fromCardId())
                .orElseThrow(() -> new CardNotFoundException(request.fromCardId()));

        Card toCard = cardRepository.findById(request.toCardId())
                .orElseThrow(() -> new CardNotFoundException(request.toCardId()));

        // Проверяем принадлежность карт
        if (!fromCard.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Source card does not belong to you");
        }
        if (!toCard.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Target card does not belong to you");
        }
        // ✅ Используем ValidationService для проверки
        validationService.validateTransfer(fromCard, toCard, request.amount());

        BigDecimal fromOldBalance = fromCard.getBalance();
        BigDecimal toOldBalance = toCard.getBalance();

        fromCard.setBalance(fromCard.getBalance().subtract(request.amount()));
        toCard.setBalance(toCard.getBalance().add(request.amount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        auditService.logTransfer(fromCard, toCard, request.amount());
        auditService.logBalanceChange(fromCard, fromOldBalance);
        auditService.logBalanceChange(toCard, toOldBalance);
    }

        // ✅ ОБНОВЛЕННЫЙ метод проверки просроченных карт
    @Transactional
    public void checkAndUpdateExpiredCards() {
        YearMonth currentDate = YearMonth.now();
        List<Card> expiredCards = cardRepository.findExpiredCards(currentDate);

        for (Card card : expiredCards) {
            card.setStatus(CardStatus.EXPIRED);
        }

        if (!expiredCards.isEmpty()) {
            cardRepository.saveAll(expiredCards);
            System.out.println("Updated " + expiredCards.size() + " expired cards");
        }
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
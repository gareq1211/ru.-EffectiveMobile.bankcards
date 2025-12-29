package ru.effectivemobile.bankcards.service.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.effectivemobile.bankcards.config.BusinessRulesConfig;
import ru.effectivemobile.bankcards.entity.Card;
import ru.effectivemobile.bankcards.entity.CardStatus;
import ru.effectivemobile.bankcards.exception.*;
import ru.effectivemobile.bankcards.repository.CardRepository;
import ru.effectivemobile.bankcards.repository.UserRepository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final BusinessRulesConfig businessRules;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    // ✅ Валидация создания карты
    public void validateCardCreation(Long userId, BigDecimal initialBalance) {
        // Проверка максимального количества карт
        validateMaxCardsPerUser(userId);

        // Проверка минимального начального баланса
        if (initialBalance.compareTo(businessRules.getMinInitialBalance()) < 0) {
            throw new BusinessValidationException(
                    String.format("Initial balance must be at least %s",
                            businessRules.getMinInitialBalance())
            );
        }
    }

    // ✅ Валидация перевода
    public void validateTransfer(Card fromCard, Card toCard, BigDecimal amount) {
        // Проверка что карты активны
        validateCardActive(fromCard);
        validateCardActive(toCard);

        // Проверка срока действия
        validateCardNotExpired(fromCard);
        validateCardNotExpired(toCard);

        // Проверка минимальной суммы перевода
        if (amount.compareTo(businessRules.getMinTransferAmount()) < 0) {
            throw new BusinessValidationException(
                    String.format("Transfer amount must be at least %s",
                            businessRules.getMinTransferAmount())
            );
        }

        // Проверка максимальной суммы перевода
        if (amount.compareTo(businessRules.getMaxTransferAmount()) > 0) {
            throw new BusinessValidationException(
                    String.format("Transfer amount cannot exceed %s",
                            businessRules.getMaxTransferAmount())
            );
        }

        // Проверка достаточности средств
        if (fromCard.getBalance().subtract(amount)
                .compareTo(businessRules.getMinCardBalance()) < 0) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds. Minimum balance must be %s",
                            businessRules.getMinCardBalance())
            );
        }

        // Проверка что перевод не на ту же карту
        if (fromCard.getId().equals(toCard.getId())) {
            throw new BusinessValidationException("Cannot transfer to the same card");
        }
    }

    // ✅ Валидация изменения баланса
    public void validateBalanceChange(Card card, BigDecimal newBalance) {
        // Проверка минимального баланса
        if (newBalance.compareTo(businessRules.getMinCardBalance()) < 0) {
            throw new BusinessValidationException(
                    String.format("Card balance cannot be less than %s",
                            businessRules.getMinCardBalance())
            );
        }

        validateCardActive(card);
        validateCardNotExpired(card);
    }

    // ✅ Валидация статуса карты
    public void validateCardStatusChange(Card card, CardStatus newStatus) {
        // Нельзя активировать просроченную карту
        if (card.getStatus() == CardStatus.EXPIRED && newStatus == CardStatus.ACTIVE) {
            throw new BusinessValidationException("Cannot activate expired card");
        }

        // Нельзя блокировать уже заблокированную карту
        if (card.getStatus() == CardStatus.BLOCKED && newStatus == CardStatus.BLOCKED) {
            throw new BusinessValidationException("Card is already blocked");
        }

        // Нельзя активировать уже активную карту
        if (card.getStatus() == CardStatus.ACTIVE && newStatus == CardStatus.ACTIVE) {
            throw new BusinessValidationException("Card is already active");
        }
    }

    // ✅ Валидация удаления карты
    public void validateCardDeletion(Card card) {
        if (card.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessValidationException("Cannot delete card with positive balance");
        }

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new BusinessValidationException("Cannot delete active card. Block it first");
        }
    }

    // ✅ Валидация PAN (номера карты)
    public void validatePan(String pan) {
        if (pan == null || !pan.matches("\\d{16}")) {
            throw new BusinessValidationException("PAN must be exactly 16 digits");
        }

        // Проверка по алгоритму Луна
        if (!isValidLuhn(pan)) {
            throw new BusinessValidationException("Invalid PAN (failed Luhn check)");
        }
    }

    // ✅ Валидация срока действия
    public void validateExpiryDate(String expiryDate) {
        try {
            YearMonth expiry = YearMonth.parse(expiryDate,
                    java.time.format.DateTimeFormatter.ofPattern("MM/yy"));
            YearMonth current = YearMonth.now();

            if (expiry.isBefore(current)) {
                throw new BusinessValidationException("Card expiry date is in the past");
            }

            // Максимальный срок - 5 лет
            if (expiry.isAfter(current.plusYears(5))) {
                throw new BusinessValidationException("Card expiry date cannot be more than 5 years in the future");
            }

        } catch (Exception e) {
            throw new BusinessValidationException("Invalid expiry date format. Use MM/yy");
        }
    }

    // ✅ Вспомогательные методы
    private void validateMaxCardsPerUser(Long userId) {
        List<Card> userCards = cardRepository.findByUserId(userId);
        if (userCards.size() >= businessRules.getMaxCardsPerUser()) {
            throw new BusinessValidationException(
                    String.format("User cannot have more than %d cards",
                            businessRules.getMaxCardsPerUser())
            );
        }
    }

    private void validateCardActive(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException(
                    String.format("Card is not active. Current status: %s", card.getStatus())
            );
        }
    }

    private void validateCardNotExpired(Card card) {
        if (card.getExpiryDate().isBefore(YearMonth.now())) {
            card.setStatus(CardStatus.EXPIRED);
            throw new CardNotActiveException("Card has expired");
        }
    }

    // Алгоритм Луна для проверки валидности номера карты
    private boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }

            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }
}
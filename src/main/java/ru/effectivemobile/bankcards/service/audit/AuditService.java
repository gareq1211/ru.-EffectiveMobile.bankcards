package ru.effectivemobile.bankcards.service.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.effectivemobile.bankcards.entity.AuditAction;
import ru.effectivemobile.bankcards.entity.Card;
import ru.effectivemobile.bankcards.entity.CardAudit;
import ru.effectivemobile.bankcards.entity.CardStatus;
import ru.effectivemobile.bankcards.repository.CardAuditRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final CardAuditRepository cardAuditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCardCreation(Card card) {
        CardAudit audit = new CardAudit();
        audit.setCardId(card.getId());
        audit.setUserId(card.getUserId());
        audit.setAction(AuditAction.CREATE);
        audit.setDescription("Card created with initial balance: " + card.getBalance());
        audit.setNewBalance(card.getBalance());
        audit.setNewStatus(card.getStatus());
        audit.setPerformedBy(getCurrentUserEmail());

        cardAuditRepository.save(audit);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logStatusChange(Card card, CardStatus oldStatus) {
        CardAudit audit = new CardAudit();
        audit.setCardId(card.getId());
        audit.setUserId(card.getUserId());
        audit.setAction(AuditAction.UPDATE_STATUS);
        audit.setDescription(String.format("Status changed from %s to %s", oldStatus, card.getStatus()));
        audit.setOldStatus(oldStatus);
        audit.setNewStatus(card.getStatus());
        audit.setPerformedBy(getCurrentUserEmail());

        cardAuditRepository.save(audit);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBalanceChange(Card card, BigDecimal oldBalance) {
        CardAudit audit = new CardAudit();
        audit.setCardId(card.getId());
        audit.setUserId(card.getUserId());
        audit.setAction(AuditAction.UPDATE_BALANCE);
        audit.setDescription(String.format("Balance changed from %s to %s", oldBalance, card.getBalance()));
        audit.setOldBalance(oldBalance);
        audit.setNewBalance(card.getBalance());
        audit.setPerformedBy(getCurrentUserEmail());

        cardAuditRepository.save(audit);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTransfer(Card fromCard, Card toCard, BigDecimal amount) {
        // Логируем для карты-отправителя
        CardAudit auditFrom = new CardAudit();
        auditFrom.setCardId(fromCard.getId());
        auditFrom.setUserId(fromCard.getUserId());
        auditFrom.setAction(AuditAction.TRANSFER);
        auditFrom.setDescription(String.format("Transfer to card %s: -%s", toCard.getId(), amount));
        auditFrom.setPerformedBy(getCurrentUserEmail());

        // Логируем для карты-получателя
        CardAudit auditTo = new CardAudit();
        auditTo.setCardId(toCard.getId());
        auditTo.setUserId(toCard.getUserId());
        auditTo.setAction(AuditAction.TRANSFER);
        auditTo.setDescription(String.format("Transfer from card %s: +%s", fromCard.getId(), amount));
        auditTo.setPerformedBy(getCurrentUserEmail());

        cardAuditRepository.save(auditFrom);
        cardAuditRepository.save(auditTo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBlockRequest(Card card) {
        CardAudit audit = new CardAudit();
        audit.setCardId(card.getId());
        audit.setUserId(card.getUserId());
        audit.setAction(AuditAction.BLOCK_REQUEST);
        audit.setDescription("User requested card block");
        audit.setPerformedBy(getCurrentUserEmail());

        cardAuditRepository.save(audit);
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
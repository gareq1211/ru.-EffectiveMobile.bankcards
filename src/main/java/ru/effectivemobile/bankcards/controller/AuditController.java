package ru.effectivemobile.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.effectivemobile.bankcards.entity.CardAudit;
import ru.effectivemobile.bankcards.repository.CardAuditRepository;

@Tag(name = "Audit", description = "Card audit logs")
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final CardAuditRepository cardAuditRepository;

    @Operation(summary = "Get audit logs for a card (ADMIN or card owner)")
    @ApiResponse(responseCode = "200", description = "Audit logs retrieved")
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<Page<CardAudit>> getCardAudit(
            @PathVariable Long cardId,
            Pageable pageable) {
        Page<CardAudit> auditLogs = cardAuditRepository.findByCardId(cardId, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @Operation(summary = "Get audit logs for user's cards (ADMIN only for other users)")
    @ApiResponse(responseCode = "200", description = "Audit logs retrieved")
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardAudit>> getUserCardAudit(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<CardAudit> auditLogs = cardAuditRepository.findByUserId(userId, pageable);
        return ResponseEntity.ok(auditLogs);
    }
}
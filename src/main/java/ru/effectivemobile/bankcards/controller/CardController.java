package ru.effectivemobile.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ru.effectivemobile.bankcards.dto.*;
import ru.effectivemobile.bankcards.service.CardService;

import java.util.List;

@Tag(name = "Bank Cards", description = "Manage bank cards")
@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // ✅ СУЩЕСТВУЮЩИЕ МЕТОДЫ

    @Operation(summary = "Create a new card (ADMIN only)")
    @ApiResponse(responseCode = "201", description = "Card created")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequest request) {
        CardDto cardDto = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cardDto);
    }

    @Operation(summary = "Get current user's cards (simple version)")
    @ApiResponse(responseCode = "200", description = "Cards retrieved")
    @GetMapping("/my")
    public ResponseEntity<List<CardDto>> getMyCards() {
        List<CardDto> cards = cardService.getMyCards();
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Transfer money between own cards")
    @ApiResponse(responseCode = "200", description = "Transfer successful")
    @PostMapping("/transfers")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        cardService.transfer(request);
        return ResponseEntity.ok().build();
    }

    // ✅ НОВЫЕ МЕТОДЫ

    @Operation(summary = "Get card by ID")
    @ApiResponse(responseCode = "200", description = "Card retrieved")
    @ApiResponse(responseCode = "404", description = "Card not found")
    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCard(@PathVariable Long id) {
        CardDto card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "Get user's cards with pagination and filtering")
    @ApiResponse(responseCode = "200", description = "Cards retrieved")
    @GetMapping("/my/filtered")
    public ResponseEntity<Page<CardDto>> getMyCardsFiltered(@Valid CardFilterRequest filter) {
        Page<CardDto> cards = cardService.getMyCardsWithFilter(filter);
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Get all cards with filtering (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Cards retrieved")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getAllCards(@Valid CardFilterRequest filter) {
        Page<CardDto> cards = cardService.getAllCards(filter);
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Update card status (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Card status updated")
    @ApiResponse(responseCode = "404", description = "Card not found")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> updateCardStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCardStatusRequest request) {
        CardDto updatedCard = cardService.updateCardStatus(id, request);
        return ResponseEntity.ok(updatedCard);
    }

    @Operation(summary = "Request card block (USER)")
    @ApiResponse(responseCode = "200", description = "Block requested")
    @ApiResponse(responseCode = "404", description = "Card not found")
    @PostMapping("/{id}/request-block")
    public ResponseEntity<Void> requestCardBlock(@PathVariable Long id) {
        cardService.requestCardBlock(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete card (ADMIN only)")
    @ApiResponse(responseCode = "204", description = "Card deleted")
    @ApiResponse(responseCode = "404", description = "Card not found")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
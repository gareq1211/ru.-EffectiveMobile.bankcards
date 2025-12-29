package ru.effectivemobile.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.effectivemobile.bankcards.dto.CardDto;
import ru.effectivemobile.bankcards.dto.CreateCardRequest;
import ru.effectivemobile.bankcards.dto.TransferRequest;
import ru.effectivemobile.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "Bank Cards", description = "Manage bank cards")
@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Validated
public class CardController {

    private final CardService cardService;

    @Operation(summary = "Create a new card (ADMIN only)")
    @ApiResponse(responseCode = "201", description = "Card created")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequest request) {
        CardDto cardDto = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cardDto);
    }
    @Operation(summary = "Get current user's cards")
    @ApiResponse(responseCode = "200", description = "Cards retrieved")
    @GetMapping("/my")
    public ResponseEntity<List<CardDto>> getMyCards() {
        List<CardDto> cards = cardService.getMyCards();
        return ResponseEntity.ok(cards);
    }
    @Operation(summary = "Transfer money between own cards")
    @ApiResponse(responseCode = "200", description = "Transfer successful")
    @ApiResponse(responseCode = "400", description = "Invalid request or insufficient funds")
    @PostMapping("/transfers")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        cardService.transfer(request);
        return ResponseEntity.ok().build();
    }
}
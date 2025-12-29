package ru.effectivemobile.bankcards.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.effectivemobile.bankcards.dto.CardDto;
import ru.effectivemobile.bankcards.dto.CreateCardRequest;
import ru.effectivemobile.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.awt.*;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Validated
public class CardController {

    private final CardService cardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequest request) {
        CardDto cardDto = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cardDto);
    }
    @GetMapping("/my")
    public ResponseEntity<List<CardDto>> getMyCards() {
        List<CardDto> cards = cardService.getMyCards();
        return ResponseEntity.ok(cards);
    }
}
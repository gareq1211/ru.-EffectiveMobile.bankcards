package ru.effectivemobile.bankcards.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.effectivemobile.bankcards.service.CardService;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardExpiryScheduler {

    private final CardService cardService;

    @Scheduled(cron = "0 0 0 * * *")
    public void checkExpiredCards() {
        log.info("Checking for expired cards...");
        try {
            cardService.checkAndUpdateExpiredCards();
            log.info("Expired cards check completed");
        } catch (Exception e) {
            log.error("Error checking expired cards", e);
        }
    }
}
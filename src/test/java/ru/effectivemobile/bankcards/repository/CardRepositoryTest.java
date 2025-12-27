package ru.effectivemobile.bankcards.repository;

import ru.effectivemobile.bankcards.entity.Card;
import ru.effectivemobile.bankcards.entity.CardStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Test
    void shouldSaveAndFindCardByUserId() {
        // given
        Card card = new Card();
        card.setUserId(1L);
        card.setEncryptedPan("encrypted_pan_123");
        card.setOwnerName("John Doe");
        card.setExpiryDate(YearMonth.of(2028, 12));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(new BigDecimal("1000.50"));

        // when
        Card saved = cardRepository.save(card);
        List<Card> found = cardRepository.findByUserId(1L);

        // then
        assertThat(found).hasSize(1);
        Card result = found.get(0);
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getEncryptedPan()).isEqualTo("encrypted_pan_123");
        assertThat(result.getBalance()).isEqualByComparingTo("1000.50");
    }
}
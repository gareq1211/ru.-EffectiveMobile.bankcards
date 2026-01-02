package ru.effectivemobile.bankcards.repository;

import ru.effectivemobile.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.effectivemobile.bankcards.entity.CardStatus;

import java.time.YearMonth;
import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUserId(Long userId);

    Page<Card> findByUserId(Long userId, Pageable pageable);

    Page<Card> findByStatus(CardStatus status, Pageable pageable);

    Page<Card> findByUserIdAndStatus(Long userId, CardStatus status, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.expiryDate < :currentDate AND c.status = 'ACTIVE'")
    List<Card> findExpiredCards(@Param("currentDate") YearMonth currentDate);
}
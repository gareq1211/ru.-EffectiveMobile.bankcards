package ru.effectivemobile.bankcards.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.effectivemobile.bankcards.entity.CardAudit;

import java.util.List;

@Repository
public interface CardAuditRepository extends JpaRepository<CardAudit, Long> {

    List<CardAudit> findByCardId(Long cardId);

    Page<CardAudit> findByCardId(Long cardId, Pageable pageable);

    List<CardAudit> findByUserId(Long userId);

    Page<CardAudit> findByUserId(Long userId, Pageable pageable);
}
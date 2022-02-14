package com.demo.repository;

import com.demo.domain.CardStateEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
@Repository
@Transactional(value= Transactional.TxType.MANDATORY)
public interface CardStateEventRepository extends JpaRepository<CardStateEventEntity, UUID> {
    List<CardStateEventEntity> findAllByCardId(UUID cardId);
}

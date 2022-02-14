package com.demo.repository;

import com.demo.domain.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional(value= Transactional.TxType.MANDATORY)
public interface CardEntityRepository extends JpaRepository<CardEntity, UUID> {

    List<CardEntity> getByUserId(UUID userId);
}

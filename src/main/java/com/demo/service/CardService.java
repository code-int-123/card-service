package com.demo.service;

import com.demo.domain.CardEntity;
import com.demo.domain.CardState;
import com.demo.domain.CardStateEventEntity;
import com.demo.repository.CardEntityRepository;
import com.demo.repository.CardStateEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(value= Transactional.TxType.REQUIRES_NEW)
@Slf4j
public class CardService {

    private final CardEntityRepository cardEntityRepository;

    private final CardStateEventRepository cardStateEventRepository;


    private static final Map<CardState,Set<CardState>> CARD_STATE_STATE_MACHINE = Map.of(CardState.ACTIVE,Set.of(CardState.ACTIVE,CardState.LOCKED,CardState.CLOSED),
            CardState.LOCKED,Set.of(CardState.ACTIVE,CardState.LOCKED,CardState.CLOSED),CardState.CLOSED,Set.of(CardState.CLOSED));

    public UUID createCard(CardEntity cardEntity){
        List<CardEntity> userEntities = cardEntityRepository.getByUserId(cardEntity.getUserId());

               return userEntities
               .stream().max(Comparator.comparing(CardEntity::getCreatedTimestamp))
               .filter(savedCardEntity->savedCardEntity.getPan().equalsIgnoreCase(cardEntity.getPan()))
                .map(CardEntity::getId)
                .orElseGet(()-> {
                    if(!userEntities.isEmpty()) {
                        throw new IllegalStateException("Already existing card for user="+cardEntity.getUserId());
                    }
                    return doCreateCard(cardEntity);
                }

        );
    }

    public Optional<CardEntity> getCardDetails(UUID cardId){
        return cardEntityRepository.findById(cardId);
    }

    public Optional<CardState> changeCardState(UUID cardId, CardState stateToUpdate){
        Optional<CardState> currentCardState = cardStateEventRepository.findAllByCardId(cardId).stream().max(Comparator.comparing(CardStateEventEntity::getCreatedTimestamp))
                .map(CardStateEventEntity::getCardState);
        if(currentCardState.isEmpty()){
            throw new IllegalStateException("Card does not exist. CardId="+cardId);
        }

        Optional<CardState> cardState = currentCardState.map(CARD_STATE_STATE_MACHINE::get)
                .filter(set -> set.contains(stateToUpdate))
                .map(unused ->
                        cardStateEventRepository.save(CardStateEventEntity.builder().
                                id(UUID.randomUUID())
                                .cardId(cardId)
                                .cardState(stateToUpdate)
                                .createdTimestamp(OffsetDateTime.now(ZoneOffset.UTC))
                                .build()).getCardState()
                );

        return cardState.isEmpty()? currentCardState: cardState;

    }

    public Optional<UUID> reissueCard(CardEntity cardEntity){

        return cardEntityRepository.getByUserId(cardEntity.getUserId())
                .stream().max(Comparator.comparing(CardEntity::getCreatedTimestamp))
                .filter(savedCardEntity->!savedCardEntity.getPan().equalsIgnoreCase(cardEntity.getPan()))
                .map(CardEntity::getId)
                .map(id ->
                    cardStateEventRepository.save(CardStateEventEntity.builder().
                            id(UUID.randomUUID())
                            .cardId(id)
                            .cardState(CardState.CLOSED)
                            .createdTimestamp(OffsetDateTime.now(ZoneOffset.UTC))
                            .build())
                )
                .map(unused->doCreateCard(cardEntity))
        ;
    }

    private UUID doCreateCard(CardEntity cardEntity) {
        cardEntity.setCardStateEventEntities(List.of(CardStateEventEntity.builder().cardId(cardEntity.getId()).cardState(CardState.ACTIVE).id(UUID.randomUUID())
                .createdTimestamp(OffsetDateTime.now(ZoneOffset.UTC)).build()));
        return cardEntityRepository.save(cardEntity).getId();
    }
}

package com.demo;

import com.demo.domain.CardEntity;
import com.demo.domain.CardState;
import com.demo.domain.CardStateEventEntity;
import com.demo.generated.api.model.NewCardRequest;
import com.demo.generated.api.model.ReissueCardRequest;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class TestUtil {
    public static final UUID NEW_CARD_ID = UUID.randomUUID();

    public static List<CardEntity> getCardEntities(){
        return List.of(getCardEntity(OffsetDateTime.now(),NEW_CARD_ID),getCardEntity(OffsetDateTime.now().minusDays(2),UUID.randomUUID()));
    }

    public static ReissueCardRequest getReissueCardRequest(UUID userId){
        return new ReissueCardRequest()
                .userId(userId)
                .firstName("TestFirstName")
                .lastName("TestLastName")
                .pan("1234567")
                .expiryDate(LocalDate.now())
                .cvc2("124");
    }

    public static ReissueCardRequest getReissueCardRequest(){
        return new ReissueCardRequest()
                .userId(UUID.randomUUID())
                .firstName("TestFirstName")
                .lastName("TestLastName")
                .pan("1234567")
                .expiryDate(LocalDate.now())
                .cvc2("124");
    }

    public static CardEntity getCardEntity(OffsetDateTime createTime,UUID cardId){
        return CardEntity.builder().
                id(cardId)
                .userId(UUID.randomUUID())
                .firstName("TestFirstName")
                .lastName("TestLastName")
                .pan("12345")
                .expiryDate(LocalDate.now())
                .cvc2("124").
                createdTimestamp(createTime)
                .build();
    }

    public static NewCardRequest getNewCardRequest(){
        return new NewCardRequest().cvc2("124")
                .expiryDate(LocalDate.now())
                .firstName("TestFirstName")
                .lastName("TestLastName")
                .pan("12345")
                .userId(UUID.randomUUID());
    }

    public static CardStateEventEntity getCardStateEventEntity(CardState state, OffsetDateTime createdTimestamp){
        return CardStateEventEntity.builder()
                .id(UUID.randomUUID())
                .cardState(state)
                .createdTimestamp(createdTimestamp)
                .build();
    }
}

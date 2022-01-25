package com.zilch.domain;

import com.zilch.generated.api.model.CardResponse;
import com.zilch.generated.api.model.NewCardRequest;
import com.zilch.generated.api.model.ReissueCardRequest;
import lombok.*;
import lombok.experimental.Wither;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@With
@Builder
@Entity
@Table(name="card")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CardEntity {

    @Id
    private UUID id;

    @Column(name="USER_ID")
    private UUID userId;

    @Column(name="FIRST_NAME")
    private String firstName;

    @Column(name="LAST_NAME")
    private String lastName;

    @Column(name="PAN")
    private String pan;

    @Column(name="EXPIRY_DATE")
    private LocalDate expiryDate;

    @Column(name="cvc2")
    private String cvc2;


    @Column(name="created_timestamp")
    private OffsetDateTime createdTimestamp;

    @OneToMany(mappedBy = "cardEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CardStateEventEntity> cardStateEventEntities;

    public static CardEntity from(NewCardRequest request){
        return CardEntity.builder().
                id(UUID.randomUUID())
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .pan(request.getPan())
                .expiryDate(request.getExpiryDate())
                .cvc2(request.getCvc2()).
                createdTimestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    public static CardEntity from(ReissueCardRequest request){
        return CardEntity.builder().
                id(UUID.randomUUID())
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .pan(request.getPan())
                .expiryDate(request.getExpiryDate())
                .cvc2(request.getCvc2()).
                        createdTimestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

}

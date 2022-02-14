package com.demo.domain;

import com.demo.generated.api.model.NewCardRequest;
import com.demo.generated.api.model.ReissueCardRequest;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
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

    @Column(name="USER_ID",updatable = false,nullable = false)
    private UUID userId;

    @Column(name="FIRST_NAME",updatable = false,nullable = false)
    private String firstName;

    @Column(name="LAST_NAME",updatable = false,nullable = false)
    private String lastName;

    @Column(name="PAN",updatable = false,nullable = false)
    private String pan;

    @Column(name="EXPIRY_DATE",updatable = false,nullable = false)
    private LocalDate expiryDate;

    @Column(name="cvc2",updatable = false,nullable = false)
    private String cvc2;


    @Column(name="created_timestamp",updatable = false,nullable = false)
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

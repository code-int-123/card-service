package com.zilch.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
@Entity
@Table(name="card_state_event")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CardStateEventEntity {
    @Id
    private UUID id;

    @Column(name="CARD_ID",updatable = false,nullable = false)
    private UUID cardId;

    @Enumerated(EnumType.STRING)
    private CardState cardState;

    @Column(name="created_timestamp",updatable = false,nullable = false)
    private OffsetDateTime createdTimestamp;

    @ManyToOne()
    @JoinColumn(name="CARD_ID", referencedColumnName = "ID",insertable = false,updatable = false)
    private CardEntity cardEntity;

}

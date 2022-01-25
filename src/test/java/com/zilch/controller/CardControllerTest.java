package com.zilch.controller;

import com.zilch.domain.CardEntity;
import com.zilch.domain.CardState;
import com.zilch.generated.api.model.*;
import com.zilch.generated.api.model.Error;
import com.zilch.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.zilch.TestUtil.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.liquibase.enabled=false"})
@EnableAutoConfiguration
class CardControllerTest {
    @MockBean
    private  CardService cardService;

    @LocalServerPort
    private int randomServerPort;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void init() {

        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + randomServerPort).build();
    }

    @Test
    void createCardSuccess(){
        when(cardService.createCard(any())).thenReturn(NEW_CARD_ID);
        webTestClient.post()
                .uri("/api/v1/cards")
                .bodyValue(getNewCardRequest())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(NewCardResponse.class)
                .value(response->assertThat(response.getCardId()).isEqualTo(NEW_CARD_ID));

    }

    @Test
    void createCardFailedWithIlleagalStateException(){
        when(cardService.createCard(any())).thenThrow(new IllegalStateException("Error"));
        webTestClient.post()
                .uri("/api/v1/cards")
                .bodyValue(getNewCardRequest())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody(Error.class)
                .value(response->assertThat(response.getMessage()).isEqualTo("Error"));
    }

    @Test
    void getCardDetailSuccess(){
        CardEntity cardEntity = getCardEntity(OffsetDateTime.now(), NEW_CARD_ID);
        cardEntity.setCardStateEventEntities(List.of(getCardStateEventEntity(CardState.ACTIVE, OffsetDateTime.now())));
        when(cardService.getCardDetails(any())).thenReturn(Optional.of(cardEntity));
        webTestClient.get()
                .uri("/api/v1/cards/{cardId}".replace("{cardId}",NEW_CARD_ID.toString()))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(CardResponse.class)
                .value(response->{
                    assertThat(response.getCardId()).isEqualTo(NEW_CARD_ID);
                    assertThat(response.getCvc2()).isEqualTo(cardEntity.getCvc2());
                    assertThat(response.getFirstName()).isEqualTo(cardEntity.getFirstName());
                    assertThat(response.getLastName()).isEqualTo(cardEntity.getLastName());
                    assertThat(response.getCardState().name()).isEqualTo(CardState.ACTIVE.name());
                });
    }

   @Test
   void getCardDetailWithNonExistCardId(){
       when(cardService.getCardDetails(any())).thenReturn(Optional.empty());
       webTestClient.get()
               .uri("/api/v1/cards/{cardId}".replace("{cardId}",NEW_CARD_ID.toString()))
               .exchange()
               .expectStatus()
               .isEqualTo(HttpStatus.OK)
               .expectBody(CardResponse.class)
               .value(response-> assertThat(response).isEqualTo(new CardResponse()));
   }

   @Test
    void changeCardStateSuccess(){
        when(cardService.changeCardState(any(),any())).thenReturn(Optional.of(CardState.ACTIVE));
       webTestClient.put()
               .uri("/api/v1/cards/{cardId}/states".replace("{cardId}",NEW_CARD_ID.toString()))
               .bodyValue(new CardStateRequest().cardState(com.zilch.generated.api.model.CardState.ACTIVE))
               .exchange()
               .expectStatus()
               .isEqualTo(HttpStatus.OK)
               .expectBody(CardStateResponse.class)
               .value(response->assertThat(response.getCardState().name()).isEqualTo(CardState.ACTIVE.name()));

   }
    @Test
    void changeCardStateFailed(){
        when(cardService.changeCardState(any(),any())).thenThrow(new IllegalStateException("Error"));
        webTestClient.put()
                .uri("/api/v1/cards/{cardId}/states".replace("{cardId}",NEW_CARD_ID.toString()))
                .bodyValue(new CardStateRequest().cardState(com.zilch.generated.api.model.CardState.ACTIVE))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody(Error.class)
                .value(response->assertThat(response.getMessage()).isEqualTo("Error"));

    }

    @Test
    void reissueCardSuccess(){
        when(cardService.reissueCard(any())).thenReturn(Optional.of(NEW_CARD_ID));
        webTestClient.post()
                .uri("/api/v1/cards/{cardId}/reissue".replace("{cardId}",NEW_CARD_ID.toString()))
                .bodyValue(getReissueCardRequest())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(ReissueCardResponse.class)
                .value(response->assertThat(response.getCardId()).isEqualTo(NEW_CARD_ID));

    }

    @Test
    void reissueCardFailed(){
        when(cardService.reissueCard(any())).thenThrow(new IllegalStateException("Error"));
        webTestClient.post()
                .uri("/api/v1/cards/{cardId}/reissue".replace("{cardId}",NEW_CARD_ID.toString()))
                .bodyValue(getReissueCardRequest())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody(Error.class)
                .value(response->assertThat(response.getMessage()).isEqualTo("Error"));

    }

}
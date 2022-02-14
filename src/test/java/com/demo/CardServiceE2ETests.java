package com.demo;

import com.demo.generated.api.model.*;
import com.demo.repository.CardEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static com.demo.TestUtil.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CardServiceE2ETests {

	@LocalServerPort
	private int randomServerPort;

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private CardEntityRepository cardEntityRepository;

	@BeforeEach
	public void init() {
		cardEntityRepository.deleteAllInBatch();
		webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + randomServerPort).build();
	}

	@Test
	@DisplayName("Test create new card success")
	void testCreateNewCardSuccess() {
		webTestClient.post()
				.uri("/api/v1/cards")
				.bodyValue(getNewCardRequest())
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(NewCardResponse.class)
				.value(response->assertThat(response.getCardId()).isNotNull());
	}

	@Test
	@DisplayName("Test create same new cards twice success with same cardId return")
	void testCreateNewCardTwiceSuccess() {
		NewCardRequest newCardRequest = getNewCardRequest();
		UUID cardId = webTestClient.post()
				.uri("/api/v1/cards")
				.bodyValue(newCardRequest)
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(NewCardResponse.class)
				.returnResult().getResponseBody().getCardId();

		webTestClient.post()
				.uri("/api/v1/cards")
				.bodyValue(newCardRequest)
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(NewCardResponse.class)
				.value(response->assertThat(response.getCardId()).isEqualTo(cardId));
	}
	@Test
	@DisplayName("Test change card state multiple times")
	void testChangeCardStateMultipleTimes(){
		NewCardRequest newCardRequest = getNewCardRequest();
		UUID cardId = webTestClient.post()
				.uri("/api/v1/cards")
				.bodyValue(newCardRequest)
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(NewCardResponse.class)
				.returnResult().getResponseBody().getCardId();

		changeCardStatus(CardState.LOCKED,cardId);
		changeCardStatus(CardState.CLOSED,cardId);
		assertThat(getCardDetail(cardId).getCardState().name()).isEqualTo(CardState.CLOSED.name());

	}

	@Test
	@DisplayName("Reissue Card once success")
	void testReissueCardOnceSuccess(){
		NewCardRequest newCardRequest = getNewCardRequest();
		UUID cardId = webTestClient.post()
				.uri("/api/v1/cards")
				.bodyValue(newCardRequest)
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(NewCardResponse.class)
				.returnResult().getResponseBody().getCardId();
		ReissueCardResponse reissueCardResponse = reissueCard(cardId,newCardRequest.getUserId());
		CardResponse cardDetail = getCardDetail(reissueCardResponse.getCardId());
		assertThat(cardDetail.getCardState().name()).isEqualTo(CardState.ACTIVE.name());
		CardResponse cardDetailOld = getCardDetail(cardId);
		assertThat(cardDetailOld.getCardState().name()).isEqualTo(CardState.CLOSED.name());
	}

	private void changeCardStatus(com.demo.generated.api.model.CardState cardState, UUID cardId){
		webTestClient.patch()
				.uri("/api/v1/cards/{cardId}/states".replace("{cardId}",cardId.toString()))
				.bodyValue(new CardStateRequest().cardState(cardState))
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(CardStateResponse.class)
				.value(response->assertThat(response.getCardState().name()).isEqualTo(cardState.name()));
	}

	private CardResponse getCardDetail(UUID cardId){
		return webTestClient.get()
				.uri("/api/v1/cards/{cardId}".replace("{cardId}",cardId.toString()))
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(CardResponse.class)
				.returnResult().getResponseBody();
	}

	private ReissueCardResponse reissueCard(UUID cardId,UUID userId){
		return webTestClient.post()
				.uri("/api/v1/cards/{cardId}/reissue".replace("{cardId}",cardId.toString()))
				.bodyValue(getReissueCardRequest(userId))
				.exchange()
				.expectStatus()
				.isEqualTo(HttpStatus.OK)
				.expectBody(ReissueCardResponse.class)
				.returnResult().getResponseBody();
	}




}

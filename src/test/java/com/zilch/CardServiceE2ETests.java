package com.zilch;

import com.zilch.generated.api.model.NewCardRequest;
import com.zilch.generated.api.model.NewCardResponse;
import com.zilch.repository.CardEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static com.zilch.TestUtil.NEW_CARD_ID;
import static com.zilch.TestUtil.getNewCardRequest;
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

}

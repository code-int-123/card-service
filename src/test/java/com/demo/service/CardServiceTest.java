package com.demo.service;

import com.demo.domain.CardEntity;
import com.demo.domain.CardState;
import com.demo.repository.CardEntityRepository;
import com.demo.repository.CardStateEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.demo.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class CardServiceTest {
    @Mock
    private CardEntityRepository cardEntityRepository;

    @Mock
    private CardStateEventRepository cardStateEventRepository;

    private CardService cardService;

    @BeforeEach
    void setUp(){
        openMocks(this);
        cardService = new CardService(cardEntityRepository,cardStateEventRepository);
    }

    @Test
    void createCardSuccess() {
        CardEntity cardEntity = getCardEntity(OffsetDateTime.now(), NEW_CARD_ID);
        when(cardEntityRepository.getByUserId(any(UUID.class))).thenReturn(new ArrayList<>());
        when(cardEntityRepository.save(any(CardEntity.class))).thenReturn(cardEntity);
        assertThat(cardService.createCard(cardEntity)).isEqualTo(cardEntity.getId());
        verify(cardEntityRepository).getByUserId(cardEntity.getUserId());
        verify(cardEntityRepository).save(any());
    }

    @Test
    void createCardSuccessWithSameCardExists(){
        CardEntity cardEntity = getCardEntity(OffsetDateTime.now(), NEW_CARD_ID);
        when(cardEntityRepository.getByUserId(any(UUID.class))).thenReturn(getCardEntities());
        when(cardEntityRepository.save(any(CardEntity.class))).thenReturn(getCardEntities().get(0));
        assertThat(cardService.createCard(cardEntity)).isEqualTo(cardEntity.getId());
        verify(cardEntityRepository).getByUserId(cardEntity.getUserId());
        verify(cardEntityRepository,times(0)).save(any());
    }

    @Test
    void createCardFailedWithDifferentCardExists(){
        CardEntity cardEntity = getCardEntity(OffsetDateTime.now(), NEW_CARD_ID);
        List<CardEntity> cardEntities = getCardEntities();
        cardEntities.get(0).setPan("123");
        when(cardEntityRepository.getByUserId(any(UUID.class))).thenReturn(cardEntities);
        when(cardEntityRepository.save(any(CardEntity.class))).thenReturn(getCardEntities().get(0));
        assertThatThrownBy(()->cardService.createCard(cardEntity)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getCardDetailsSuccess() {
        CardEntity cardEntity = getCardEntity(OffsetDateTime.now(), NEW_CARD_ID);
        when(cardEntityRepository.findById(any(UUID.class))).thenReturn(Optional.of(cardEntity));
        assertThat(cardService.getCardDetails(UUID.randomUUID())).isEqualTo(Optional.of(cardEntity));
    }

    @Test
    void changeCardStateFromActiveToLockReturnLock() {
        changeStateTest(CardState.ACTIVE,CardState.LOCKED,CardState.LOCKED);
    }
    @Test
    void changeCardStateFromActiveToClosedReturnClosed() {
        changeStateTest(CardState.ACTIVE,CardState.CLOSED,CardState.CLOSED);
    }
    @Test
    void changeCardStateFromActiveToActiveReturnActive() {
        changeStateTest(CardState.ACTIVE,CardState.ACTIVE,CardState.ACTIVE);
    }

    @Test
    void changeCardStateFromLockedToActiveReturnActive() {
        changeStateTest(CardState.LOCKED,CardState.ACTIVE,CardState.ACTIVE);
    }

    @Test
    void changeCardStateFromLockedToLockedReturnLocked() {
        changeStateTest(CardState.LOCKED,CardState.LOCKED,CardState.LOCKED);
    }

    @Test
    void changeCardStateFromLockedToClosedReturnClosed() {
        changeStateTest(CardState.LOCKED,CardState.CLOSED,CardState.CLOSED);
    }

    @Test
    void changeCardStateFromClosedToActiveReturnClosed() {
        changeStateTest(CardState.CLOSED,CardState.ACTIVE,CardState.CLOSED);
    }

    @Test
    void changeCardStateFromClosedToLockedReturnClosed() {
        changeStateTest(CardState.CLOSED,CardState.LOCKED,CardState.CLOSED);
    }

    @Test
    void changeCardStateFromClosedToClosedReturnClosed() {
        changeStateTest(CardState.CLOSED,CardState.CLOSED,CardState.CLOSED);
    }

    @Test
    void changeCardStateWithNonExistsCardId() {
        when(cardStateEventRepository.findAllByCardId(any(UUID.class))).thenReturn(new ArrayList<>());
        assertThatThrownBy(()->cardService.changeCardState(UUID.randomUUID(),CardState.ACTIVE)).isInstanceOf(IllegalStateException.class);
        verify(cardStateEventRepository,times(0)).save(any());
    }

    @Test
    void reissueCardSuccess() {
        CardEntity cardEntity = getCardEntity(OffsetDateTime.now(), NEW_CARD_ID);
        CardEntity cardEntity2 = getCardEntity(OffsetDateTime.now(), NEW_CARD_ID);
        cardEntity2.setPan("234");
        when(cardEntityRepository.getByUserId(any())).thenReturn(List.of(cardEntity));
        when(cardStateEventRepository.save(any())).thenReturn(getCardStateEventEntity(CardState.ACTIVE,OffsetDateTime.now()));
        when(cardEntityRepository.save(any())).thenReturn(cardEntity);
        assertThat(cardService.reissueCard(cardEntity2)).isEqualTo(Optional.of(cardEntity.getId()));
    }

    private void changeStateTest(CardState currentState, CardState targetSate, CardState returnState){
        when(cardStateEventRepository.findAllByCardId(any(UUID.class))).thenReturn(List.of(getCardStateEventEntity(currentState,OffsetDateTime.now())));
        when(cardStateEventRepository.save(any())).thenReturn(getCardStateEventEntity(targetSate,OffsetDateTime.now()));
        assertThat(cardService.changeCardState(UUID.randomUUID(),targetSate)).isEqualTo(Optional.of(returnState));
    }
}
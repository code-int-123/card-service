package com.zilch.controller;

import com.zilch.domain.CardEntity;
import com.zilch.domain.CardState;
import com.zilch.domain.CardStateEventEntity;
import com.zilch.generated.api.model.*;
import com.zilch.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController()
@RequestMapping(value = "/api/v1")
@RequiredArgsConstructor
@Slf4j
public class CardController {

    private final CardService cardService;

    @PostMapping(value = "/cards",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NewCardResponse> createCard(@Valid @NotNull @RequestBody NewCardRequest request){
        log.info("Received request to create new card  userId={}",request.getUserId());

        UUID cardId = cardService.createCard(CardEntity.from(request));

        return ResponseEntity.ok(new NewCardResponse().cardId(cardId));
    }

    @GetMapping(value = "/cards/{cardId}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CardResponse> getCardDetail(@PathVariable("cardId") final UUID cardId){
        log.info("Received request to get card detail  cardId={}",cardId);

        CardResponse cardResponse = cardService.getCardDetails(cardId).map(this::from).orElseGet(CardResponse::new);

        return ResponseEntity.ok(cardResponse);
    }

    @PutMapping(value="/cards/{cardId}/states",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CardStateResponse> updateCardState(@PathVariable("cardId") final UUID cardId, @Valid @NotNull @RequestBody CardStateRequest request){

        log.info("Received request to change card state cardId={}, CardStateRequest={}",cardId,request);

        CardStateResponse cardStateResponse = cardService.changeCardState(cardId, CardState.valueOf(request.getCardState().name()))
                .map(state -> com.zilch.generated.api.model.CardState.fromValue(state.name()))
                .map(state -> new CardStateResponse().cardState(state))
                .orElseGet(CardStateResponse::new);
        return ResponseEntity.ok(cardStateResponse);

    }
    @PostMapping(value="/cards/{cardId}/reissue",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReissueCardResponse> reissueCard(@PathVariable("cardId") final UUID cardId,@Valid @NotNull @RequestBody ReissueCardRequest request){

        log.info("Received request to reissue card cardId={}, ReissueCardRequest={}",cardId,request);

        ReissueCardResponse reissueCardResponse = cardService.reissueCard(CardEntity.from(request)).map(id -> new ReissueCardResponse().cardId(id))
                .orElseGet(ReissueCardResponse::new);

        return ResponseEntity.ok(reissueCardResponse);
    }

    private CardResponse from(CardEntity cardEntity){
        return new CardResponse()
                .cardId(cardEntity.getId())
                .cvc2(cardEntity.getCvc2())
                .expiryDate(cardEntity.getExpiryDate())
                .firstName(cardEntity.getFirstName())
                .lastName(cardEntity.getLastName())
                .cardState(com.zilch.generated.api.model.CardState.fromValue(getLatestState(cardEntity.getCardStateEventEntities()).toString()))
                .createdDateTime(cardEntity.getCreatedTimestamp())
                .pan(cardEntity.getPan())
               .userId(cardEntity.getUserId());
    }

    private CardState getLatestState(List<CardStateEventEntity> eventEntities){
        return eventEntities.stream().max(Comparator.comparing(CardStateEventEntity::getCreatedTimestamp))
                .orElseThrow()
                .getCardState();
    }
}

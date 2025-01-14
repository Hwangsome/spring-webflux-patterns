package com.bhuang.webflux.pattern.sec05.client;

import com.bhuang.webflux.pattern.sec05.dto.RoomReservationRequest;
import com.bhuang.webflux.pattern.sec05.dto.RoomReservationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class RoomClient {

    private final WebClient client;

    public RoomClient(@Value("${sec05.room.service}") String baseUrl){
        this.client = WebClient.builder()
                               .baseUrl(baseUrl)
                               .build();
    }

    public Flux<RoomReservationResponse> reserve(Flux<RoomReservationRequest> flux){
        return this.client
                .post()
                .body(flux, RoomReservationRequest.class)
                .retrieve()
                .bodyToFlux(RoomReservationResponse.class)
                .onErrorResume(ex -> Mono.empty());
    }

}

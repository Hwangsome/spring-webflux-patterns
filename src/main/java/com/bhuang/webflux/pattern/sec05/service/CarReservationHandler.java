package com.bhuang.webflux.pattern.sec05.service;

import com.bhuang.webflux.pattern.sec05.client.CarClient;
import com.bhuang.webflux.pattern.sec05.dto.CarReservationRequest;
import com.bhuang.webflux.pattern.sec05.dto.CarReservationResponse;
import com.bhuang.webflux.pattern.sec05.dto.ReservationItemRequest;
import com.bhuang.webflux.pattern.sec05.dto.ReservationItemResponse;
import com.bhuang.webflux.pattern.sec05.dto.ReservationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class CarReservationHandler extends ReservationHandler{

    private final CarClient client;

    public CarReservationHandler(CarClient client) {
        this.client = client;
    }

    @Override
    protected ReservationType getType() {
        return ReservationType.CAR;
    }

    @Override
    protected Flux<ReservationItemResponse> reserve(Flux<ReservationItemRequest> flux) {
        return flux.map(this::toCarRequest)
                .transform(this.client::reserve)
                .map(this::toReservationItemResponse);
    }

    private CarReservationRequest toCarRequest(ReservationItemRequest request){
        return CarReservationRequest.create(
                request.getCity(),
                request.getFrom(),
                request.getTo(),
                request.getCategory()
        );
    }

    private ReservationItemResponse toReservationItemResponse(CarReservationResponse response){
        return ReservationItemResponse.create(
                response.getReservationId(),
                this.getType(),
                response.getCategory(),
                response.getCity(),
                response.getPickup(),
                response.getDrop(),
                response.getPrice()
        );
    }
}

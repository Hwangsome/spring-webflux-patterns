package com.bhuang.webflux.pattern.sec05.service;

import com.bhuang.webflux.pattern.sec05.client.RoomClient;
import com.bhuang.webflux.pattern.sec05.dto.ReservationItemRequest;
import com.bhuang.webflux.pattern.sec05.dto.ReservationItemResponse;
import com.bhuang.webflux.pattern.sec05.dto.ReservationType;
import com.bhuang.webflux.pattern.sec05.dto.RoomReservationRequest;
import com.bhuang.webflux.pattern.sec05.dto.RoomReservationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class RoomReservationHandler extends ReservationHandler{

    private final RoomClient client;

    public RoomReservationHandler(RoomClient client) {
        this.client = client;
    }

    @Override
    protected ReservationType getType() {
        return ReservationType.ROOM;
    }

    @Override
    protected Flux<ReservationItemResponse> reserve(Flux<ReservationItemRequest> flux) {
        return flux.map(this::toRoomRequest)
              .transform(this.client::reserve)
              .map(this::toReservationItemResponse);
    }

    private RoomReservationRequest toRoomRequest(ReservationItemRequest request){
        return RoomReservationRequest.create(
                request.getCity(),
                request.getFrom(),
                request.getTo(),
                request.getCategory()
        );
    }

    private ReservationItemResponse toReservationItemResponse(RoomReservationResponse response){
        return ReservationItemResponse.create(
                response.getReservationId(),
                this.getType(),
                response.getCategory(),
                response.getCity(),
                response.getCheckIn(),
                response.getCheckOut(),
                response.getPrice()
        );
    }
}

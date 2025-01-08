package com.bhuang.webflux.pattern.sec03.service;

import com.bhuang.webflux.pattern.sec03.client.ShippingClient;
import com.bhuang.webflux.pattern.sec03.dto.OrchestrationRequestContext;
import com.bhuang.webflux.pattern.sec03.dto.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class ShippingOrchestrator extends Orchestrator{
    private final ShippingClient shippingClient;

    public ShippingOrchestrator(ShippingClient shippingClient) {
        this.shippingClient = shippingClient;
    }

    @Override
    public Mono<OrchestrationRequestContext> create(OrchestrationRequestContext ctx) {
        return this.shippingClient
                .schedule(ctx.getShippingRequest())
                .doOnNext(ctx::setShippingResponse)
                .thenReturn(ctx)
                ;
    }

    @Override
    public Predicate<OrchestrationRequestContext> isSuccess() {
        return orchestrationRequestContext -> Status.SUCCESS.equals(orchestrationRequestContext.getStatus());
    }

    @Override
    public Consumer<OrchestrationRequestContext> cancel() {
        return orchestrationRequestContext -> Mono.just(orchestrationRequestContext)
                .filter(isSuccess())
                // flatMap(ctx -> this.shippingClient.cancel(ctx.getShippingRequest()))  <==>  .map(OrchestrationRequestContext::getShippingRequest) .flatMap(this.shippingClient::cancel)
                //.flatMap(ctx -> this.shippingClient.cancel(ctx.getShippingRequest()))
                .map(OrchestrationRequestContext::getShippingRequest)
                .flatMap(this.shippingClient::cancel)
                .subscribe()
                ;
    }
}

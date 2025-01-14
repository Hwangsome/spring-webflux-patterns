package com.bhuang.webflux.pattern.sec03.service;

import com.bhuang.webflux.pattern.sec03.client.ShippingClient;
import com.bhuang.webflux.pattern.sec03.dto.OrchestrationRequestContext;
import com.bhuang.webflux.pattern.sec03.dto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class ShippingOrchestrator extends Orchestrator{
    private Logger logger = LoggerFactory.getLogger(ShippingOrchestrator.class.getName());
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

        return orchestrationRequestContext -> {
            logger.info(" ShippingOrchestrator orchestrationRequestContext: {}", orchestrationRequestContext);
            return Status.SUCCESS.equals(orchestrationRequestContext.getShippingResponse().getStatus());
        };

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

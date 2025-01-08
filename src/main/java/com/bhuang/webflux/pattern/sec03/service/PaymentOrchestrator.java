package com.bhuang.webflux.pattern.sec03.service;

import com.bhuang.webflux.pattern.sec03.client.UserClient;
import com.bhuang.webflux.pattern.sec03.dto.OrchestrationRequestContext;
import com.bhuang.webflux.pattern.sec03.dto.Status;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class PaymentOrchestrator extends Orchestrator{
    private final UserClient userClient;

    public PaymentOrchestrator(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public Mono<OrchestrationRequestContext> create(OrchestrationRequestContext ctx) {
        return this.userClient
                .deduct(ctx.getPaymentRequest()) // Mono<PaymentResponse>
                .doOnNext(ctx::setPaymentResponse)
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
                .map(OrchestrationRequestContext::getPaymentRequest)
                .flatMap(this.userClient::deduct)
                .subscribe();
    }
}

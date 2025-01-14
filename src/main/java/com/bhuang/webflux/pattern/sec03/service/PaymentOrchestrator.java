package com.bhuang.webflux.pattern.sec03.service;

import com.bhuang.webflux.pattern.sec03.client.UserClient;
import com.bhuang.webflux.pattern.sec03.dto.OrchestrationRequestContext;
import com.bhuang.webflux.pattern.sec03.dto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class PaymentOrchestrator extends Orchestrator{
    private final UserClient userClient;
    private Logger logger = LoggerFactory.getLogger(PaymentOrchestrator.class.getName());


    public PaymentOrchestrator(UserClient userClient) {
        this.userClient = userClient;
    }

    /*
   当用户下订单的时候，需要扣款
    */
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
        return orchestrationRequestContext -> {
            logger.info(" PaymentOrchestrator orchestrationRequestContext: {}", orchestrationRequestContext);
            return Status.SUCCESS.equals(orchestrationRequestContext.getPaymentResponse().getStatus());
        };
    }

    /*
    当用户取消订单的时候 需要退款
     */
    @Override
    public Consumer<OrchestrationRequestContext> cancel() {
        return orchestrationRequestContext -> Mono.just(orchestrationRequestContext)
                .filter(isSuccess())
                .map(OrchestrationRequestContext::getPaymentRequest)
                .flatMap(this.userClient::deduct)
                .subscribe();
    }
}

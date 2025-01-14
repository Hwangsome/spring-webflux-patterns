package com.bhuang.webflux.pattern.sec03.service;

import com.bhuang.webflux.pattern.sec03.client.InventoryClient;
import com.bhuang.webflux.pattern.sec03.dto.OrchestrationRequestContext;
import com.bhuang.webflux.pattern.sec03.dto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class InventoryOrchestrator extends Orchestrator {
    private Logger logger = LoggerFactory.getLogger(InventoryOrchestrator.class.getName());

    private final InventoryClient inventoryClient;

    public InventoryOrchestrator(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    /*
    当用户下单的时候，需要扣减库存
     */
    @Override
    public Mono<OrchestrationRequestContext> create(OrchestrationRequestContext ctx) {
        return this.inventoryClient
                .deduct(ctx.getInventoryRequest()) // Mono<InventoryResponse>
                // doOnNext 副作用：给ctx 设置值
                .doOnNext(ctx::setInventoryResponse) // Mono<InventoryResponse>
                .thenReturn(ctx);
    }

    @Override
    public Predicate<OrchestrationRequestContext> isSuccess() {
        return orchestrationRequestContext ->
        {
            logger.info(" InventoryOrchestrator orchestrationRequestContext: {}", orchestrationRequestContext);
            return Status.SUCCESS.equals(orchestrationRequestContext.getInventoryResponse().getStatus());
        };
    }

    /*
    当用户 取消订单的时候， 需要恢复库存
     */
    @Override
    public Consumer<OrchestrationRequestContext> cancel() {
        return orchestrationRequestContext -> Mono.just(orchestrationRequestContext)
                .filter(isSuccess())
                .map(OrchestrationRequestContext::getInventoryRequest)
                .flatMap(this.inventoryClient::restore)
                .subscribe();
    }
}

package com.bhuang.webflux.pattern.sec03.service;

import com.bhuang.webflux.pattern.sec03.dto.OrchestrationRequestContext;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/*
流量管理: 使用多播和反压机制，能够有效管理和处理多个并发请求。
异步处理: 通过调度器在适当的线程上异步处理订单取消逻辑，避免阻塞主线程。
 */
@Service
public class OrderCancellationService {

    /*
    使用 Sinks.Many 表示一个多生产者的 sink，它可以接收多个 OrchestrationRequestContext 对象。
    这里的 OrchestrationRequestContext 可能是表示订单取消请求的上下文。
     */
    private Sinks.Many<OrchestrationRequestContext> sink;
    /*
    这是一个 Flux<OrchestrationRequestContext> 对象，它是用来传递和处理多个 OrchestrationRequestContext 对象的异步流。
     */
    private Flux<OrchestrationRequestContext> flux;

    @Autowired
    private List<Orchestrator> orchestrators;

    @PostConstruct
    public void  init(){
        /*
            Sinks.many(): 创建一个多生产者的 sink，允许多个线程/生产者发送数据。
            multicast(): 使得传入的数据可以被多个订阅者接收。
            onBackpressureBuffer(): 在流量过大且消费者处理不过来的时候，打开一个缓冲区以防止数据丢失。
         */
        this.sink = Sinks.many().multicast().onBackpressureBuffer();

        /*
            this.sink.asFlux(): 通过 sink 创建一个 Flux，这个 Flux 会接收 sink 中的所有数据。
            publishOn(Schedulers.boundedElastic()): 这表示后续的处理将会在一个弹性调度器上执行，允许处理阻塞操作而不阻塞主线程。
         */
        this.flux = this.sink.asFlux().publishOn(Schedulers.boundedElastic());
        orchestrators.forEach(o -> this.flux.subscribe(o.cancel()));
    }

    public void cancelOrder(OrchestrationRequestContext ctx){
        /*
        这个方法尝试向 sink 发送一个新的值（即订单取消的上下文）。如果 sink 的状态允许，则会成功发送。如果 sink 被关闭或达到容量限制，可能会失败。
         */
        this.sink.tryEmitNext(ctx);
    }

}

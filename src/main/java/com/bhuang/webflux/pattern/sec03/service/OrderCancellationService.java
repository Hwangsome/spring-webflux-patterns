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

    /*
`OrderCancellationService` 详细解析

`OrderCancellationService` 是基于 Spring WebFlux 框架的一个服务类，专门用于处理订单取消的逻辑。
它的设计旨在利用反应式编程的优势，通过异步和非阻塞的方式自动管理多个并发请求。


1. **注解 `@Service`**:
   - 将该类标记为一个服务组件，允许 Spring 将其作为 Spring 上下文的一个 Bean 来管理。

2. **成员变量**:
   - `private Sinks.Many<OrchestrationRequestContext> sink;`：
     - 使用 `Sinks.Many` 表示一个多生产者的 Sink，允许多个生产者（例如多个取消请求）向其中发送 `OrchestrationRequestContext` 对象。
   - `private Flux<OrchestrationRequestContext> flux;`：
     - 使用 `Flux` 来处理通过 Sink 发送的 `OrchestrationRequestContext` 对象的异步流。

3. **依赖注入**:
   - `@Autowired` 注解用于自动注入一组实现了 `Orchestrator` 接口的对象，通常会包含多个具体的协调器（如 `InventoryOrchestrator`, `ShippingOrchestrator` 等）。

4. **`@PostConstruct` 初始化方法**:
   - `init()` 方法在 Bean 被创建之后执行，完成服务的初始化工作。
   - **创建 Sink**：
     - `this.sink = Sinks.many().multicast().onBackpressureBuffer();`：创建一个多生产者的 Sink，允许数据的多播，并在背压发生时使用缓冲区，避免数据丢失。
   - **创建 Flux**：
     - `this.flux = this.sink.asFlux().publishOn(Schedulers.boundedElastic());`：将 Sink 转换为 Flux，并使用弹性调度器处理后续的操作，从而避免主线程阻塞。
   - **订阅取消逻辑**：
     - `orchestrators.forEach(o -> this.flux.subscribe(o.cancel()));`：为每个协调器的取消逻辑创建一个订阅，当有取消请求时，这些协调器将会被触发。

方法

1. **`cancelOrder(OrchestrationRequestContext ctx)`**：
   - 该方法用于接收订单取消请求。
   - `this.sink.tryEmitNext(ctx);`：尝试向 Sink 发送一个新的取消请求的上下文对象。
     - 如果 Sink 处于开放状态并且没有达到容量限制，则请求会被成功发送。
     - 如果 Sink 已经关闭或者达到容量限制，则请求可能会失败，方法返回 `false`。

 调用流程

1. **接收取消请求**：
   - 客户端调用 `cancelOrder` 方法并传递一个 `OrchestrationRequestContext` 对象，表示一个订单的取消请求。

2. **数据发射**：
   - 通过 Sink 的 `tryEmitNext` 方法，服务将取消请求的上下文对象发送到 Sink 中。

3. **流的处理**：
   - 由于在 `init` 方法中，已将 Sink 转换为 Flux 并为所有协调器注册了取消订阅，因此一旦有新数据流入，所有订阅的协调器的 `cancel` 方法都会被调用。

4. **协调器的取消逻辑**：
   - 每个协调器的 `cancel` 方法会处理收到的取消请求，通常会检查请求的状态并进一步调用相应的客户端（如库存或运输客户端）来取消具体的业务逻辑。

背压与多播

- **背压处理**：通过 `onBackpressureBuffer()`，在高并发请求的情况下，系统能够有效管理请求，不会导致数据丢失。这是反应式编程的一个重要特点。
- **多播**：使用多播机制，从而能够让多个消费者（协调器）同时处理同一个请求，这样提高了系统的响应性和处理性能。

`OrderCancellationService` 是一个典型的反应式服务，遵循反应式编程的原则，
通过使用 Sink 和 Flux 构建流来异步处理订单取消请求。它的设计允许系统在处理高并发请求时保持高效，并能优雅地应对流量调节，确保数据的一致性和完整性。
     */
    public void cancelOrder(OrchestrationRequestContext ctx){
        /*
        这个方法尝试向 sink 发送一个新的值（即订单取消的上下文）。如果 sink 的状态允许，则会成功发送。如果 sink 被关闭或达到容量限制，可能会失败。
         */
        this.sink.tryEmitNext(ctx);
    }

}

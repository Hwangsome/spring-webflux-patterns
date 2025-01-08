package com.bhuang.webflux.pattern.sec03.service;

import com.bhuang.webflux.pattern.sec03.dto.OrchestrationRequestContext;
import com.bhuang.webflux.pattern.sec03.dto.Status;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderFulfillmentService {

    /*
    上游的服务列表
     */
    private final List<Orchestrator> orchestrators;

    public OrderFulfillmentService(List<Orchestrator> orchestrators) {
        this.orchestrators = orchestrators;
    }

    /*
     下订单
      需要调用所有的orchestrators（上游的服务列表），收集响应
     */
    public Mono<OrchestrationRequestContext> placeOrder(OrchestrationRequestContext orchestrationRequestContext) {
        List<Mono<OrchestrationRequestContext>> orchestrationRequestContextListMono = orchestrators.stream()
                /*
                在这一行中，调用 o.create(ctx) 依然是一个惰性执行的过程。此时，create(ctx) 方法的执行并不会立即进行任何操作，也不会调用内部的逻辑。此时只是生成包含异步操作的 Mono 对象。
                惰性执行: 反应式流（如 Mono 和 Flux）处于惰性状态，只有在有订阅者（subscriber）对其进行订阅（通过 subscribe 方法）时，
                才会开始执行。这种特性允许开发者构建非常灵活和高效的异步处理流程。

                调用上游的创建 操作。比如创建库存，物流。。。
                 */
                .map(o -> o.create(orchestrationRequestContext))
                .collect(Collectors.toList());
        /*
         zip 你最多可以填8个publisher （p1 ~ p8）
         超过8个的时候，你需要使用List 包装好mono


         合并操作: 当执行 Mono.zip 时，它将 list 中的所有 Mono 合并。由于这些 Mono 对象仍处于惰性状态，
         因此不执行任何操作，直到有实际的订阅。

         触发执行: 当最终的 Mono 被订阅（通常是在调用 subscribe() 时）时，所有参与的 Mono 才开始执行。
         这个时候，各自的 create(ctx) 方法才真正开始工作，处理请求所需的逻辑。

         Mono.zip 是 Reactor 框架中用于合并多个 Mono 对象的静态方法。该方法广泛用于将多个异步操作的结果组合成一个新的 Mono。使用 zip 方法，你可以同时处理多个异步数据流，
         并在所有流都产生结果后的某个时间点，返回这些结果的组合。

         该参数是一个 List<Mono<OrchestrationRequestContext>>，即包含多个 Mono 对象的列表。每个 Mono 代表一个异步操作，
         成功时将返回一个 OrchestrationRequestContext 对象。

         a -> a[0]: 这是一个函数式接口 (Function)，它接受一个数组（通常是 Object[]），该数组包含了所有被 zipping 的 Mono 完成时返回的结果。
         在此示例中，a[0] 表示我们只关注结果数组中的第一个元素。这个函数的返回值将成为最终的 Mono 项目。

         为什么只关注a[0]? 因为我们call 上游的服务都反回OrchestrationRequestContext，每次call完的时候都有： ctx::setXXXXResponse

        所有相关的 Mono 都完成，zip 将会将这些结果（通常是以数组的形式）传递给你所提供的函数
        收集操作: zip 方法会等待 list 中的每一个 Mono 完成。这意味着所有的 Mono 对象都必须成功返回其结果，或者有一个 Mono 返回错误。
        异步执行: 该过程是异步的，只有当所有参与的 Mono 都完成后，zip 才会被触发并合并结果。因此，zip 在操作开始时不会返回一个结果；它返回的是一个新的 Mono 对象，表示组合结果的 Mono。
        组合结果: 一旦所有相关的 Mono 都完成，zip 将会将这些结果（通常是以数组的形式）传递给你所提供的函数（在本例中即 a -> a[0]），然后生成最终结果。
         */

       return Mono.zip(orchestrationRequestContextListMono, a -> a[0])
                .cast(OrchestrationRequestContext.class)
               .doOnNext(this::updateStatus);
    }

    private void updateStatus(OrchestrationRequestContext ctx){
        var allSuccess = this.orchestrators.stream().allMatch(o -> o.isSuccess().test(ctx));
        var status = allSuccess ? Status.SUCCESS : Status.FAILED;
        ctx.setStatus(status);
    }
}

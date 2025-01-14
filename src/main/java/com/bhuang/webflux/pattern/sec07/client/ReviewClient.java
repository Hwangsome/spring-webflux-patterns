package com.bhuang.webflux.pattern.sec07.client;

import com.bhuang.webflux.pattern.sec07.dto.Review;
import com.bhuang.webflux.pattern.sec07.exceptions.RetryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class ReviewClient {

    private final WebClient client;

    private final Random random = new Random();

    public ReviewClient(@Value("${sec07.review.service}") String baseUrl){
        this.client = WebClient.builder()
                               .baseUrl(baseUrl)
                               .build();
    }

    /*

    使用 backoff 策略来代替固定间隔重试:
    逐渐增加了两次重试之间的延迟

     处理重试次数耗尽的情况
    最后，需要考虑所有重试都不成功的情况。
    在这种情况下，策略的默认行为是传播一个 RetryExhaustedException，封装了最后一个错误。
    可以通过使用 onRetryExhaustedThrow 方法并提供一个 ServiceException 的Generator（生成器）来覆盖这种行为：
     */
    public Mono<List<Review>> getReviews(Integer id){
        return this.client
                .get()
                .uri("{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToFlux(Review.class)
                .log()
                .collectList()
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .filter(throwable -> throwable instanceof ResponseStatusException)
                        .jitter(0.75)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {throw new RetryException("External Service failed to process after max retries", HttpStatus.SERVICE_UNAVAILABLE.value());})
                )


//                .retry(5)
                .timeout(Duration.ofMillis(300))
                .onErrorReturn(Collections.emptyList());
    }

}

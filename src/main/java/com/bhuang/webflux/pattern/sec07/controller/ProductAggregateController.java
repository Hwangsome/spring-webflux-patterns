package com.bhuang.webflux.pattern.sec07.controller;

import com.bhuang.webflux.pattern.sec07.dto.ProductAggregate;
import com.bhuang.webflux.pattern.sec07.service.ProductAggregatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("sec07")
public class ProductAggregateController {

    private final ProductAggregatorService service;

    public ProductAggregateController(ProductAggregatorService service) {
        this.service = service;
    }

    @GetMapping("product/{id}")
    public Mono<ResponseEntity<ProductAggregate>> getProductAggregate(@PathVariable Integer id){
        return this.service.aggregate(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}

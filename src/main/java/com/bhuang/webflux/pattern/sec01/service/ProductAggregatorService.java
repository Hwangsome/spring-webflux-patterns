package com.bhuang.webflux.pattern.sec01.service;


import com.bhuang.webflux.pattern.sec01.client.ProductClient;
import com.bhuang.webflux.pattern.sec01.client.PromotionClient;
import com.bhuang.webflux.pattern.sec01.client.ReviewClient;
import com.bhuang.webflux.pattern.sec01.dto.Price;
import com.bhuang.webflux.pattern.sec01.dto.ProductAggregate;
import com.bhuang.webflux.pattern.sec01.dto.ProductResponse;
import com.bhuang.webflux.pattern.sec01.dto.PromotionResponse;
import com.bhuang.webflux.pattern.sec01.dto.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ProductAggregatorService {

    @Autowired
    private ProductClient productClient;

    @Autowired
    private PromotionClient promotionClient;

    @Autowired
    private ReviewClient reviewClient;

    public Mono<ProductAggregate> aggregate(Integer id){
        return Mono.zip(
               this.productClient.getProduct(id),
               this.promotionClient.getPromotion(id),
               this.reviewClient.getReviews(id)
        )
        .map(t -> toDto(t.getT1(), t.getT2(), t.getT3()));
    }

    private ProductAggregate toDto(ProductResponse product, PromotionResponse promotion, List<Review> reviews){
        var price = new Price();
        var amountSaved = product.getPrice() * promotion.getDiscount() / 100;
        var discountedPrice = product.getPrice() - amountSaved;
        price.setListPrice(product.getPrice());
        price.setAmountSaved(amountSaved);
        price.setDiscountedPrice(discountedPrice);
        price.setDiscount(promotion.getDiscount());
        price.setEndDate(promotion.getEndDate());
        return ProductAggregate.create(
                product.getId(),
                product.getCategory(),
                product.getDescription(),
                price,
                reviews
        );
    }


}

package com.bhuang.webflux.pattern.sec04.util;


import com.bhuang.webflux.pattern.sec04.dto.InventoryRequest;
import com.bhuang.webflux.pattern.sec04.dto.OrchestrationRequestContext;
import com.bhuang.webflux.pattern.sec04.dto.PaymentRequest;
import com.bhuang.webflux.pattern.sec04.dto.ShippingRequest;

public class OrchestrationUtil {

    public static void buildRequestContext(OrchestrationRequestContext ctx){
        buildPaymentRequest(ctx);
        buildInventoryRequest(ctx);
        buildShippingRequest(ctx);
    }

    public static void buildPaymentRequest(OrchestrationRequestContext ctx){
        var paymentRequest = PaymentRequest.create(
                ctx.getOrderRequest().getUserId(),
                ctx.getProductPrice() * ctx.getOrderRequest().getQuantity(),
                ctx.getOrderId()
        );
        ctx.setPaymentRequest(paymentRequest);
    }

    public static void buildInventoryRequest(OrchestrationRequestContext ctx){
        var inventoryRequest = InventoryRequest.create(
            ctx.getOrderId(),
            ctx.getOrderRequest().getProductId(),
            ctx.getOrderRequest().getQuantity()
        );
        ctx.setInventoryRequest(inventoryRequest);
    }

    public static void buildShippingRequest(OrchestrationRequestContext ctx){
        var shippingRequest = ShippingRequest.create(
                ctx.getOrderRequest().getQuantity(),
                ctx.getOrderRequest().getUserId(),
                ctx.getOrderId()
        );
        ctx.setShippingRequest(shippingRequest);
    }

}
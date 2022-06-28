package com.onion.product.event;

import com.onion.domain.product.Product;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductCreatedEvent {

    private final Product product;

}

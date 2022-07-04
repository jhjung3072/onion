package com.onion.product;

import com.onion.product.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ProductRepositoryDsl {

    Page<Product> findByKeyword(String keyword, Pageable pageable);

}

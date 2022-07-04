package com.onion.product;

import com.onion.location.QLocation;
import com.onion.product.product.Product;
import com.onion.product.product.QProduct;
import com.onion.tag.QTag;
import com.onion.user.QUser;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class ProductRepositoryDslImpl extends QuerydslRepositorySupport implements ProductRepositoryDsl{

    public ProductRepositoryDslImpl() {
        super(Product.class);
    }

    @Override
    public Page<Product> findByKeyword(String keyword, Pageable pageable) {
        QProduct product = QProduct.product;
        JPQLQuery<Product> query = from(product).where(product.seller.enabled.isTrue()
                        .and(product.name.containsIgnoreCase(keyword))
                        .or(product.tags.any().title.containsIgnoreCase(keyword))
                        .or(product.location.name.containsIgnoreCase(keyword)))
                .leftJoin(product.tags, QTag.tag).fetchJoin()
                .leftJoin(product.location, QLocation.location).fetchJoin()
                .leftJoin(product.seller, QUser.user).fetchJoin()
                .distinct();
        JPQLQuery<Product> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Product> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }


}

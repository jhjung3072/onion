package com.onion.product;

import com.onion.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface ProductRepository extends PagingAndSortingRepository<Product, Integer> {
	
	Product findByName(String name);

	Long countById(Integer id);

	@Query("SELECT p FROM Product p WHERE p.seller.enabled=true")
	Page<Product>findAll(Pageable pageable);

	// 물건 키워드 검색 (이름, 설명, 유저 닉네임, 지역 이름)
	@Query("SELECT p FROM Product p WHERE (p.name LIKE %?1% "
			+ "OR p.fullDescription LIKE %?1% "
			+ "OR p.seller.nickname LIKE %?1% "
			+ "OR p.location.name LIKE %?1%) AND p.seller.enabled=true")
	Page<Product> findAll(String keyword, Pageable pageable);

	// 해당 지역 및 부모 지역에서 물건 검색
	@Query("SELECT p FROM Product p WHERE (p.location.id = ?1 "
			+ "OR p.location.allParentIDs LIKE %?2%) AND p.seller.enabled=true")
	Page<Product> findAllInLocation(Integer locationId, String locationIdMatch,
			Pageable pageable);

	// 해당 지역 및 부모 지역에서 물건 검색
	@Query("SELECT p FROM Product p WHERE p.location.id = ?1 AND p.seller.enabled=true")
	Page<Product> findByLocation(Integer locationId,Pageable pageable);
	
	// 특정 지역에서 물건 검색
	@Query("SELECT p FROM Product p WHERE (p.location.id = ?1 "
			+ "OR p.location.allParentIDs LIKE %?2%) AND "
			+ "(p.name LIKE %?3% "
			+ "OR p.fullDescription LIKE %?3% "
			+ "OR p.seller.nickname LIKE %?3% "
			+ "OR p.location.name LIKE %?3%) AND p.seller.enabled=true")
	Page<Product> searchInLocation(Integer locationId, String locationIdMatch,
			String keyword, Pageable pageable);
	
	// 물건이름으로 물건 검색
	@Query("SELECT p FROM Product p WHERE p.name LIKE %?1% AND p.seller.enabled=true")
	Page<Product> searchProductsByName(String keyword, Pageable pageable);


	@Query("SELECT p FROM Product p WHERE p.seller.id = ?1")
    Page<Product> findBySeller(Integer id, Pageable pageable);
}

package com.onion.product;

import com.onion.location.Location;
import com.onion.product.product.Product;
import com.onion.tag.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;
import java.util.Set;


public interface ProductRepository extends PagingAndSortingRepository<Product, Integer>, ProductRepositoryDsl {
	
	Product findByName(String name);

	Long countById(Integer id);

	@EntityGraph(attributePaths = {"location", "seller"})
	@Query("SELECT p FROM Product p WHERE p.seller.enabled=true")
	Page<Product>findAll(Pageable pageable);

	// 물건 키워드 검색 (이름, 설명, 유저 닉네임, 지역 이름) by 관리자
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
	@EntityGraph(attributePaths = {"location", "seller"})
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

	// 검색 성능 개선을 위해 FULLTEXT INDEX 사용
	// JPA에서 MATCH AGAINST 사용 불가능, nativeQuery 사용
	// 참고 : https://geek-techiela.blogspot.com/2016/02/springboot-jpa-fulltextsearchnativequer.html
	@Query("SELECT p FROM Product p WHERE (p.name LIKE %?1% "
			+ "OR p.shortDescription LIKE %?1% "
			+ "OR p.seller.nickname LIKE %?1% ) AND p.seller.enabled=true")
	Page<Product> search(String keyword, Pageable pageable);

	@EntityGraph(attributePaths = "tags")
    Product findProductWithTagsById(Integer id);

	@EntityGraph(attributePaths = {"tags", "location"})
    Product findProductWithTagsAndLocationById(Integer id);

	// 기간 내에 주문목록 리스트
	// id, createdTime, price만 불러오기 위해 new 사용
	@Query("SELECT NEW com.onion.product.product.Product(p.id, p.createdTime, p.price) FROM Product p WHERE"
			+ " p.createdTime BETWEEN ?1 and ?2 ORDER BY p.createdTime ASC")
    List<Product> findByCreatedTimeBetween(Date startTime, Date endTime);

}

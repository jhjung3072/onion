package com.onion.watchlist;

import java.util.List;

import com.onion.domain.User;
import com.onion.domain.Watchlist;
import com.onion.domain.product.Product;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface WatchlistRepository extends CrudRepository<Watchlist, Integer> {
	List<Watchlist> findByUser(User user);
	
	// 상품과 회원으로 관심목록 리턴
	Watchlist findByUserAndProduct(User user, Product product);

	// 회원의 관심목록에 있는 상품 삭제
	@Modifying
	@Query("DELETE FROM Watchlist w WHERE w.user.id = ?1 AND w.product.id = ?2")
	void deleteByUserAndProduct(Integer userId, Integer productId);
	
	// 회원의 장바구니 삭제
	@Modifying
	@Query("DELETE  Watchlist w WHERE w.user.id = ?1")
	void deleteByUser(Integer userId);

}

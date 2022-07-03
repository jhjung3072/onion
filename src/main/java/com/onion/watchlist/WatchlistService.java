package com.onion.watchlist;

import java.util.List;

import com.onion.user.User;
import com.onion.product.product.Product;
import com.onion.exception.WatchlistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class WatchlistService {

	private final WatchlistRepository watchRepo;
	
	// 물건을 관심목록로 추가
	public Integer addProduct(Integer productId, User user)
			throws WatchlistException {

		Product product = new Product(productId);
		Watchlist watchlist = watchRepo.findByUserAndProduct(user, product);
		
		if (watchlist != null) { // 회원의 관심목록에 이미 있는 물건이라면,
			throw new WatchlistException("이미 관심목록에 담아놓은 물건입니다.");
		} else { // 회원의 장바구니에 없는 상품이라면
			watchlist = new Watchlist();
			watchlist.setUser(user);
			watchlist.setProduct(product);

		}
		Watchlist savedWatchlist = watchRepo.save(watchlist);
		return savedWatchlist.getId();
	}
	
	// 관심목록 목록
	public List<Watchlist> listCartItems(User user) {
		return watchRepo.findByUser(user);
	}
	

	// 관심목록에서 상품 삭제
	public void removeProduct(Integer productId, User user) {
		watchRepo.deleteByUserAndProduct(user.getId(), productId);
	}
	
	// 관심목록 삭제
	public void deleteByUser(User user) {
		watchRepo.deleteByUser(user.getId());
	}




}

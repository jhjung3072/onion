package com.onion.watchlist;

import javax.servlet.http.HttpServletRequest;

import com.onion.config.Utility;
import com.onion.domain.User;
import com.onion.exception.UserNotFoundException;
import com.onion.exception.WatchlistException;
import com.onion.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class WatchlistRestController {
	private final WatchlistService watchlistService;
	private final UserService userService;
	
	// 해당 물건 관심목록에 추가하기
	@PostMapping("/watchlist/add/{productId}")
	public String addProductToWatchlist(@PathVariable("productId") Integer productId, HttpServletRequest request) {
		try { 
			User user = getAuthenticatedUser(request);
			watchlistService.addProduct(productId,user);
			return "물건을 관심목록에 담았습니다.";
		} catch (UserNotFoundException ex) {
			return "관심목록에 담기위해서 로그인이 필요합니다.";
		} catch (WatchlistException ex) {
			return ex.getMessage();
		}
		
	}
	
	// 승인된 회원 객체 리턴
	private User getAuthenticatedUser(HttpServletRequest request) 
			throws UserNotFoundException {
		String email = Utility.getEmailOfAuthenticatedCustomer(request);
		if (email == null) {
			throw new UserNotFoundException("승인되지 않은 회원입니다.");
		}
		return userService.getUserByEmail(email);
	}

	
	// 장바구니에서 상품 삭제 DELETE
	@DeleteMapping("/watchlist/remove/{productId}")
	public String removeProductToWatchlist(@PathVariable("productId") Integer productId,
			HttpServletRequest request) {
		try {
			User user = getAuthenticatedUser(request);
			watchlistService.removeProduct(productId, user);
			
			return "해당 물건을 관심목록에서 삭제했습니다..";
			
		} catch (UserNotFoundException e) {
			return "상품을 관심목록에서 삭제하기 위해서는 로그인이 필요합니다.";
		}
	}
}

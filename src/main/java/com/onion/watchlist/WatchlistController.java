package com.onion.watchlist;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.onion.config.Utility;
import com.onion.domain.User;
import com.onion.domain.Watchlist;
import com.onion.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class WatchlistController {
	private final UserService userService;
	private final WatchlistService watchlistService;

	
	// 관심목록 목록 GET
	@GetMapping("/watchlist")
	public String viewWatchlist(Model model, HttpServletRequest request) {
		User user = getAuthenticatedUser(request);
		List<Watchlist> watchlist = watchlistService.listCartItems(user);

		model.addAttribute("watchlist", watchlist);
		
		return "watchlist/watchlist";
	}
	
	// 인증된 회원 객체 리턴
	private User getAuthenticatedUser(HttpServletRequest request) {
		String email = Utility.getEmailOfAuthenticatedCustomer(request);
		return userService.getUserByEmail(email);
	}	
}

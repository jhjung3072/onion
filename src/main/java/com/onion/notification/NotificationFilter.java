package com.onion.notification;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.onion.ControllerHelper;
import com.onion.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationFilter implements Filter {

	private final ControllerHelper controllerHelper;

	private final NotificationRepository notificationRepository;
	
	// 요청/응답 쌍이 체인을 통해 전달될 때마다 컨테이너에 의해 호출.
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		String url = servletRequest.getRequestURL().toString();
		
		// static 요청일 경우 return
		if (url.endsWith(".css") || url.endsWith(".js") || url.endsWith(".png") ||
				url.endsWith(".jpg")) {
			chain.doFilter(request, response);
			return;
		}

		User user=controllerHelper.getAuthenticatedUser(servletRequest);
		if (user!=null ) {
			long count = notificationRepository.countByUserAndChecked(user, false);
			request.setAttribute("hasNotification", count > 0);
		}


		chain.doFilter(request, response);

	}

}

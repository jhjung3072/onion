package com.onion.paging;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

//페이징 처리시 중복 코드 생략 및 조건에 맞는 파라미터가 있을 때 원하는 값을 바인딩
public class PagingAndSortingArgumentResolver implements HandlerMethodArgumentResolver {

	//파라미터가 Resolver에 의해 수행될 수 있는 타입인지 true / false를 리턴. 만약 true를 리턴한다면 resolveArgument() 메소드를 실행
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterAnnotation(PagingAndSortingParam.class) != null;
	}

	//실제로 파라미터와 바인딩을 할 객체를 리턴
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer model,
			NativeWebRequest request, WebDataBinderFactory binderFactory) throws Exception {
		PagingAndSortingParam annotation = parameter.getParameterAnnotation(PagingAndSortingParam.class);
		String sortDir = request.getParameter("sortDir");
		String sortField = request.getParameter("sortField");
		String keyword = request.getParameter("keyword");
		
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("keyword", keyword);
		model.addAttribute("moduleURL", annotation.moduleURL());	
		
		
		return new PagingAndSortingHelper(model, annotation.listName(),
				sortField, sortDir, keyword);
	}

}

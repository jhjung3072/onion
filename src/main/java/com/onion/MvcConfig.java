package com.onion;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.onion.paging.PagingAndSortingArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		exposeDirectory("../product-images", registry);
	}

	private void exposeDirectory(String pathPattern, ResourceHandlerRegistry registry) {
		Path path = Paths.get(pathPattern);
		String absolutePath = path.toFile().getAbsolutePath();

		String logicalPath = pathPattern.replace("../", "") + "/**";

		registry.addResourceHandler(logicalPath)
				.addResourceLocations("file:/" + absolutePath + "/");
	}

	//resolver등록
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new PagingAndSortingArgumentResolver());
	}

}

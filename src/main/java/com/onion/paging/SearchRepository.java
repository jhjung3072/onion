package com.onion.paging;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;


@NoRepositoryBean // 실제 빈을 등록하지 않도록 방지하는 어노테이션
public interface SearchRepository<T, ID> extends PagingAndSortingRepository<T, ID> {
	public Page<T> findAll(String keyword, Pageable pageable);
}

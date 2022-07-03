package com.onion.location;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface LocationRepository extends PagingAndSortingRepository<Location, Integer> {
	
	//상위 지역 이름 순으로 정렬
	@Query("SELECT l FROM Location l WHERE l.parent.id is NULL")
	List<Location> findRootLocations(Sort sort);

	// 지역 페이징
	@Query("SELECT l FROM Location l WHERE l.parent.id is NULL")
	Page<Location> findRootLocations(Pageable pageable);
	
	// 지역 이름 검색 with 페이징
	@Query("SELECT l FROM Location l WHERE l.name LIKE %?1%")
	Page<Location> search(String keyword, Pageable pageable);
	
	Long countById(Integer id);
	
	Location findByName(String name);

	// 지역 활성화 enable or not
	@Query("UPDATE Location l SET l.enabled = ?2 WHERE l.id = ?1")
	@Modifying
	void updateEnabledStatus(Integer id, boolean enabled);

}

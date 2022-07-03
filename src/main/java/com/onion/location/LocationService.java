package com.onion.location;

import java.util.*;
import javax.transaction.Transactional;

import com.onion.exception.LocationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@Transactional
public class LocationService {
	public static final int ROOT_LOCATIONS_PER_PAGE = 4;
	
	@Autowired
	private LocationRepository repo;
	
	// 지역 목록 페이징
	// 상위 지역 이름순 -> 하위 지역 이름순
	public List<Location> listByPage(LocationPageInfo pageInfo, int pageNum, String sortDir,
									 String keyword) {
		Sort sort = Sort.by("name");
		
		if (sortDir.equals("asc")) {
			sort = sort.ascending();
		} else if (sortDir.equals("desc")) {
			sort = sort.descending();
		}
		
		Pageable pageable = PageRequest.of(pageNum - 1, ROOT_LOCATIONS_PER_PAGE, sort);
		
		Page<Location> pageLocations = null;
		
		//검색어가 있을 때
		if (keyword != null && !keyword.isEmpty()) {
			pageLocations = repo.search(keyword, pageable);	
		} else {
			// 검색어가 없을 시 상위지역 find
			pageLocations = repo.findRootLocations(pageable);
		}
		
		List<Location> rootLocations = pageLocations.getContent();
		
		pageInfo.setTotalElements(pageLocations.getTotalElements());
		pageInfo.setTotalPages(pageLocations.getTotalPages());
		
		//검색어가 있을 때
		if (keyword != null && !keyword.isEmpty()) {
			List<Location> searchResult = pageLocations.getContent();
			for (Location location : searchResult) {
				location.setHasChildren(location.getChildren().size() > 0);
			}
			
			return searchResult;
			
		} else {
			// 검색어가 없을 시 상위지역 정렬
			return listHierarchicalLocations(rootLocations, sortDir);
		}
	}
	
	// 지역 목록
	private List<Location> listHierarchicalLocations(List<Location> rootLocations, String sortDir) {
		List<Location> hierarchicalLocations = new ArrayList<>();
		
		for (Location rootLocation : rootLocations) {
			// 데이터베이스에 영향을 주지않고 "--" 을 삽입하기 위해 copy 사용
			hierarchicalLocations.add(Location.copyFull(rootLocation));
			
			// 상위 지역의 하위지역
			Set<Location> children = sortSubLocations(rootLocation.getChildren(), sortDir);
			
			// 첫번째 하위 지역에 "--" 삽입
			for (Location subLocation : children) {
				String name = "--" + subLocation.getName();
				hierarchicalLocations.add(Location.copyFull(subLocation, name));
				
				// 그 하위 지역에 "--" 삽입 재귀반복
				listSubHierarchicalLocations(hierarchicalLocations, subLocation, 1, sortDir);
			}
		}
		
		return hierarchicalLocations;
	}
	
	// 하위지역 레벨 별 "--" 삽입 재귀 반복
	private void listSubHierarchicalLocations(List<Location> hierarchicalLocations,
			Location parent, int subLevel, String sortDir) {
		Set<Location> children = sortSubLocations(parent.getChildren(), sortDir);
		int newSubLevel = subLevel + 1;
		
		for (Location subLocation : children) {
			String name = "";
			for (int i = 0; i < newSubLevel; i++) {				
				name += "--";
			}
			name += subLocation.getName();
		
			hierarchicalLocations.add(Location.copyFull(subLocation, name));
			
			//하위 지역에 재귀 반복
			listSubHierarchicalLocations(hierarchicalLocations, subLocation, newSubLevel, sortDir);
		}
		
	}
	
	// 지역 저장
	// 상위 지역가 있을 시 상위지역 ID 도 저장
	public Location save(Location location) {
		Location parent = location.getParent();
		if (parent != null) {
			String allParentIds = parent.getAllParentIDs() == null ? "-" : parent.getAllParentIDs();
			allParentIds += String.valueOf(parent.getId()) + "-";
			location.setAllParentIDs(allParentIds);
		}
		
		return repo.save(location);
	}
	
	// 지역 폼에서 사용하기 위한 지역 리스트
	public List<Location> listLocationsUsedInForm() {
		List<Location> locationsUsedInForm = new ArrayList<>();
		
		//상위 지역 이름순으로 정렬
		Iterable<Location> locationsInDB = repo.findRootLocations(Sort.by("name").ascending());
		
		for (Location location : locationsInDB) {
			// ID와 이름만 필요
			locationsUsedInForm.add(Location.copyIdAndName(location));
			
			Set<Location> children = sortSubLocations(location.getChildren());
			
			//하위 지역 레벨 별로 "--" 삽입 재귀 반복
			for (Location subLocation : children) {
				String name = "--" + subLocation.getName();
				locationsUsedInForm.add(Location.copyIdAndName(subLocation.getId(), name,true));
				
				listSubLocationsUsedInForm(locationsUsedInForm, subLocation, 1);
			}
		}		
		
		return locationsUsedInForm;
	}
	
	// 하위 지역 "--" 삽입 재귀 반복
	private void listSubLocationsUsedInForm(List<Location> locationsUsedInForm, 
			Location parent, int subLevel) {
		int newSubLevel = subLevel + 1;
		Set<Location> children = sortSubLocations(parent.getChildren());
		
		for (Location subLocation : children) {
			String name = "";
			for (int i = 0; i < newSubLevel; i++) {				
				name += "--";
			}
			name += subLocation.getName();
			
			locationsUsedInForm.add(Location.copyIdAndName(subLocation.getId(), name,true));
			
			listSubLocationsUsedInForm(locationsUsedInForm, subLocation, newSubLevel);
		}		
	}

	// 하위 지역 리스트 (부모 지역이 없는 지역)
	public List<Location> listChildLocations() {
		List<Location> childLocations = new ArrayList<>();

		Iterable<Location> listAllLocations = repo.findAll();

		for (Location location:listAllLocations){
			if (location.getChildren().size()==0){
				childLocations.add(location);
			}
		}

		return childLocations;
	}


	
	// 지역 GET by id
	public Location get(Integer id) throws LocationNotFoundException {
		try {
			return repo.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new LocationNotFoundException("지역 ID: " + id + "을 찾을 수 없습니다.");
		}
	}
	
	// 지역 이름 및 줄임말 중복체크
	// DuplicateName or DuplicateAlias or OK
	public String checkUnique(Integer id, String name) {
		boolean isCreatingNew = (id == null || id == 0);
		
		Location locationByName = repo.findByName(name);
		
		if (isCreatingNew) { // 새로운 지역 생성 시
			if (locationByName != null) {
				return "DuplicateName";
			}
		} else { // 지역 수정 시
			if (locationByName != null && locationByName.getId() != id) {
				return "DuplicateName";
			}
		}
		return "OK";
	}
	
	// 하위지역 이름 오름차순 정렬
	private SortedSet<Location> sortSubLocations(Set<Location> children) {
		return sortSubLocations(children, "asc");
	}
	
	// 하위지역 이름순 정렬
	private SortedSet<Location> sortSubLocations(Set<Location> children, String sortDir) {
		SortedSet<Location> sortedChildren = new TreeSet<>(new Comparator<Location>() {
			@Override
			public int compare(Location cat1, Location cat2) {
				// 오름차순
				if (sortDir.equals("asc")) {
					return cat1.getName().compareTo(cat2.getName());
				} else {
					// 내림차순
					return cat2.getName().compareTo(cat1.getName());
				}
			}
		});
		
		sortedChildren.addAll(children);
		
		return sortedChildren;
	}
	
	// 지역 활성화
	public void updateLocationEnabledStatus(Integer id, boolean enabled) {
		repo.updateEnabledStatus(id, enabled);
	}
	
	// 지역 삭제 by id
	public void delete(Integer id) throws LocationNotFoundException {
		Long countById = repo.countById(id);
		if (countById == null || countById == 0) {
			throw new LocationNotFoundException("지역 ID: " + id + "을 찾을 수 없습니다.");
		}
		
		repo.deleteById(id);
	}


    public Location getByName(String name) throws LocationNotFoundException {
		try {
			return repo.findByName(name);
		} catch (NoSuchElementException ex) {
			throw new LocationNotFoundException("해당 지역이름을 찾을 수 없습니다.");
		}
    }
}

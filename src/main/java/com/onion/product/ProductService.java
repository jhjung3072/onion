package com.onion.product;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import com.onion.domain.Tag;
import com.onion.domain.User;
import com.onion.domain.product.Product;
import com.onion.exception.ProductNotFoundException;
import com.onion.paging.PagingAndSortingHelper;
import com.onion.product.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
	public static final int PRODUCTS_PER_PAGE = 5;

	private final ProductRepository repo;

	private final ApplicationEventPublisher eventPublisher;
	
	// 물건 목록
	public List<Product> listAll() {
		return (List<Product>) repo.findAll();
	}
	
	// 물건 목록 페이징
	public void listByPage(int pageNum, PagingAndSortingHelper helper, Integer locationId) {
		Pageable pageable = helper.createPageable(PRODUCTS_PER_PAGE, pageNum);
		String keyword = helper.getKeyword();
		Page<Product> page = null;
		
		// 검색어가 있다면
		if (keyword != null && !keyword.isEmpty()) {
			// 지역이 있다면
			if (locationId != null && locationId > 0) {
				// 지역끼리 묶어서 페이징
				String locationIdMatch = "-" + String.valueOf(locationId) + "-";
				page = repo.searchInLocation(locationId, locationIdMatch, keyword, pageable);
			} else {
				page = repo.findAll(keyword, pageable);
			}
		} else {
			// 검색어가 없다면
			if (locationId != null && locationId > 0) {
				String locationIdMatch = "-" + String.valueOf(locationId) + "-";
				// 모든 지역 페이징
				page = repo.findAllInLocation(locationId, locationIdMatch, pageable);
			} else {		
				page = repo.findAll(pageable);
			}
		}
		
		helper.updateModelAttributes(pageNum, page);
	}	
	
	// 물건 검색
	public void searchProducts(int pageNum, PagingAndSortingHelper helper) {
		Pageable pageable = helper.createPageable(PRODUCTS_PER_PAGE, pageNum);
		String keyword = helper.getKeyword();		
		Page<Product> page = repo.searchProductsByName(keyword, pageable);		
		helper.updateModelAttributes(pageNum, page);
	}
	
	// 물건 저장
	public Product save(Product product, User seller) {
		if (product.getId() == null) {
			product.setCreatedTime(new Date());
		}
		
		product.setSeller(seller);
		product.setCreatedTime(new Date());
		product.setLocation(seller.getLocation());

		Product updatedProduct = repo.save(product);
		return updatedProduct;

	}

	// 물건 가격저장
	public void saveProductPrice(Product productInForm) {
		Product productInDB = repo.findById(productInForm.getId()).get();
		productInDB.setPrice(productInForm.getPrice());
		repo.save(productInDB);
	}


	// 물건 삭제
	public void delete(Integer id) throws ProductNotFoundException {
		Long countById = repo.countById(id);
		Product product = repo.findById(id).get();

		if (countById == null || countById == 0) {
			throw new ProductNotFoundException("해당 물건ID를 찾을 수 없습니다.");
		}

		repo.deleteById(id);
	}	
	
	// 물건 GET by ID
	public Product get(Integer id) throws ProductNotFoundException {
		try {
			return repo.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new ProductNotFoundException("해당 물건ID를 찾을 수 없습니다.");
		}
	}

	// 모든 물건중에 최근 등록된 물건 9개
	public Page<Product> list9RecentlyRegisteredProductForAll() {
		Sort sort = Sort.by("createdTime").descending();
		Pageable pageable = PageRequest.of(0, 9, sort);

		return repo.findAll(pageable);
	}

	// 회원의 지역 물건중에 최근 등록된 물건 9개
	public Page<Product> list9RecentlyRegisteredProductForUser(User user) {
		Sort sort = Sort.by("createdTime").descending();
		Pageable pageable = PageRequest.of(0, 9, sort);

		Integer locationId=user.getLocation().getId();
		return repo.findByLocation(locationId,pageable);
	}

    public Page<Product> listProductsBySeller(User user,  int pageNum, String sortField, String sortDir) {
		Sort sort = Sort.by(sortField);
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

		Pageable pageable = PageRequest.of(pageNum - 1, PRODUCTS_PER_PAGE, sort);
		return repo.findBySeller(user.getId(), pageable);
    }

    public Page<Product> search(String keyword, int pageNum) {
		Pageable pageable = PageRequest.of(pageNum - 1, PRODUCTS_PER_PAGE);
		return repo.search(keyword, pageable);
    }


	public void addTag(Product product, Tag tag) {
		product.getTags().add(tag);
		eventPublisher.publishEvent(new ProductCreatedEvent(product));

	}

	public void removeTag(Product product, Tag tag) {
		product.getTags().remove(tag);
	}
}

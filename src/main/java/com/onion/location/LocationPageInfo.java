package com.onion.location;

//카테고리 페이징 시 전체 페이지 개수와 요소 값을 사용하기 위한 클래스
public class LocationPageInfo {
	private int totalPages;
	private long totalElements;
	
	public int getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	public long getTotalElements() {
		return totalElements;
	}
	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}
	
	
}

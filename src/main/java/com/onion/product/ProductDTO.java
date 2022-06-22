package com.onion.product;

public class ProductDTO {
	private String name;
	private String imagePath;
	private float price;

	
	public ProductDTO(String name, String imagePath, float price) {
		this.name = name;
		this.imagePath = imagePath;
		this.price = price;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	
}

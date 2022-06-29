package com.onion.report;

import java.util.Objects;

public class ReportItem {
	private String identifier;
	private float grossSales;
	private int productsCount;


	public ReportItem() {
	}

	public ReportItem(String identifier) {
		this.identifier = identifier;
	}

	public ReportItem(String identifier, float grossSales) {
		this.identifier = identifier;
		this.grossSales = grossSales;
	}
	
	

	public ReportItem(String identifier, float grossSales, int productsCount) {
		super();
		this.identifier = identifier;
		this.grossSales = grossSales;
		this.productsCount = productsCount;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public float getGrossSales() {
		return grossSales;
	}

	public void setGrossSales(float grossSales) {
		this.grossSales = grossSales;
	}



	@Override
	public int hashCode() {
		return Objects.hash(identifier);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportItem other = (ReportItem) obj;
		return Objects.equals(identifier, other.identifier);
	}

	public void addGrossSales(float amount) {
		this.grossSales += amount;
		
	}

	public void increaseProductsCount() {
		this.productsCount++;
	}

	public int getProductsCount() {
		return productsCount;
	}

	public void setProductsCount(int productsCount) {
		this.productsCount = productsCount;
	}

	public void increaseProductsCount(int count) {
		this.productsCount+=count;
		
	}
	
}

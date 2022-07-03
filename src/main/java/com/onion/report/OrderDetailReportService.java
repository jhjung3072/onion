package com.onion.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.onion.product.product.Product;
import com.onion.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailReportService extends AbstractReportService {

	@Autowired private ProductRepository repo;
	
	// 통계타입별 통계 내역 세분화
	@Override
	protected List<ReportItem> getReportDataByDateRangeInternal(
			Date startDate, Date endDate, ReportType reportType) {
		List<Product> listProducts = null;
		
		// 통계타입이 카테고리이면 해당 기간에 주문을 카테고리별로 리스트
		if (reportType.equals(ReportType.CATEGORY)) {
			listProducts = repo.findWithLocationAndTimeBetween(startDate, endDate);
		} else if (reportType.equals(ReportType.PRODUCT)) { // 통계타입이 상품이면 해당 기간에 주문을 상품별로 리스트
			listProducts = repo.findByCreatedTimeBetween(startDate, endDate);
		}
		
		//printRawData(listOrderDetails);
		
		List<ReportItem> listReportItems = new ArrayList<>();
		
		for (Product detail : listProducts) {
			String identifier = "";
			
			// 통계타입이 카테고리이면 카테고리 이름
			if (reportType.equals(ReportType.CATEGORY)) {
				identifier = detail.getLocation().getName();
			} else if (reportType.equals(ReportType.PRODUCT)) { // 통계타입이 상품이면 상품 이름
				identifier = detail.getName();
			}
			
			ReportItem reportItem = new ReportItem(identifier);
			
			float grossSales = detail.getPrice();
			
			int itemIndex = listReportItems.indexOf(reportItem);
			
			if (itemIndex >= 0) {
				reportItem = listReportItems.get(itemIndex);
				reportItem.addGrossSales(grossSales);
				reportItem.increaseProductsCount(1); // 판매량 갯수 증가
			} else {
				listReportItems.add(new ReportItem(identifier, grossSales, 1));
			}
		}
		
		//printReportData(listReportItems);
		
		return listReportItems;
	}


}

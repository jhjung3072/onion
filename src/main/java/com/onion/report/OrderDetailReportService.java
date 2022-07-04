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
		
		List<ReportItem> listReportItems = new ArrayList<>();
		
		for (Product detail : listProducts) {
			String identifier = "";
			
			
			ReportItem reportItem = new ReportItem(identifier);
			
			float grossSales = detail.getPrice();
			
			int itemIndex = listReportItems.indexOf(reportItem);
			
			if (itemIndex >= 0) {
				reportItem = listReportItems.get(itemIndex);
				reportItem.addGrossSales(grossSales);
				reportItem.increaseProductsCount(1); // 등록된 물건 개수 카운트
			} else {
				listReportItems.add(new ReportItem(identifier, grossSales, 1));
			}
		}
		
		//printReportData(listReportItems);
		
		return listReportItems;
	}


}

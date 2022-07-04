package com.onion.report;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.onion.product.product.Product;
import com.onion.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MasterOrderReportService extends AbstractReportService{
	@Autowired private ProductRepository repo;
	

	
	protected List<ReportItem> getReportDataByDateRangeInternal(Date startTime, Date endTime, ReportType reportType) {
		// 기간 내에 등록된 물건 리스트를 불러옴
		List<Product> listProducts = repo.findByCreatedTimeBetween(startTime, endTime);

		// 통계 시작 및 종료 날짜, 통계 타입을 입력받아 통계를 생성
		List<ReportItem> listReportItems = createReportData(startTime, endTime, reportType);
		
		System.out.println();
		// 통계에서 등록된 물건의 가격과, 등록 개수를 계산
		calculateSalesForReportData(listProducts, listReportItems);
		
		return listReportItems;
	}

	// 통계에서 등록된 물건의 가격과, 등록 개수를 계산하기 위한 함수
	private void calculateSalesForReportData(List<Product> listProducts, List<ReportItem> listReportItems) {
		for (Product product : listProducts) {
			String productDateString = dateFormatter.format(product.getCreatedTime());
			
			ReportItem reportItem = new ReportItem(productDateString);
			
			int itemIndex = listReportItems.indexOf(reportItem);
			
			if (itemIndex >= 0) {
				reportItem = listReportItems.get(itemIndex);
				
				reportItem.addGrossSales(product.getPrice());
				reportItem.increaseProductsCount();
			}
		}
	}


	// 통계 시작 및 종료 날짜, 통계 타입을 입력받아 통계를 생성하는 함수
	private List<ReportItem> createReportData(Date startTime, Date endTime, ReportType reportType) {
		List<ReportItem> listReportItems = new ArrayList<>();
		
		// 통계 시작 날짜 설정
		Calendar startDate = Calendar.getInstance();
		startDate.setTime(startTime);
		
		// 통계 종료 날짜 설정
		Calendar endDate = Calendar.getInstance();
		endDate.setTime(endTime);	
		
		Date currentDate = startDate.getTime();
		String dateString = dateFormatter.format(currentDate);
		
		listReportItems.add(new ReportItem(dateString));
		
		do {
			// 통계가 Day타입이면 시작 날짜를 기준으로 1일씩 add
			if (reportType.equals(ReportType.DAY)) {
				startDate.add(Calendar.DAY_OF_MONTH, 1);
			} else if (reportType.equals(ReportType.MONTH)) {//통계가 Month 타입이면 시작 날짜를 기준을 한달씩 add
				startDate.add(Calendar.MONTH, 1);
			}
			
			currentDate = startDate.getTime();
			dateString = dateFormatter.format(currentDate);	
			
			listReportItems.add(new ReportItem(dateString));
			
		} while (startDate.before(endDate)); // 시작날짜부터 종료날짜까지 반복
		
		return listReportItems;		
	}

	
}

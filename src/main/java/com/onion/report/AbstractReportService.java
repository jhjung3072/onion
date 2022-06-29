package com.onion.report;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class AbstractReportService {
	protected DateFormat dateFormatter;
	
	// 일주일 동안의 통계
	public List<ReportItem> getReportDataLast7Days(ReportType reportType) {
		return getReportDataLastXDays(7,reportType);
	}

	// 28일 동안의 통계
	public List<ReportItem> getReportDataLast28Days(ReportType reportType) {
		return getReportDataLastXDays(28,reportType);
	}
	
	// X일동안의 통계 범위를 지정하기 위한 함수
	protected List<ReportItem> getReportDataLastXDays(int days, ReportType reportType) {
		// 통계 마지막일은 현재 일
		Date endTime = new Date();
		Calendar cal = Calendar.getInstance();
		// 현재를 기준으로 days-1 일 이전을 기준으로 startTime
		cal.add(Calendar.DAY_OF_MONTH, -(days - 1));
		Date startTime = cal.getTime();
		
		System.out.println("시작 날짜: " + startTime);
		System.out.println("끝 날짜: " + endTime);
		
		dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		
		return getReportDataByDateRangeInternal(startTime, endTime, reportType);
	}
	
	// 6개월 동안의 통계
	public List<ReportItem> getReportDataLast6Months(ReportType reportType) {
		return getReportDataLastXMonths(6,reportType);
	}

	// 작년 동안의 통계
	public List<ReportItem> getReportDataLastYear(ReportType reportType) {
		return getReportDataLastXMonths(12,reportType);
	}
	
	// X개월 동안의 통계 범위를 지정하기 위한 함수
	protected List<ReportItem> getReportDataLastXMonths(int months, ReportType reportType) {
		// 통계 마지막 월은 현재 월
		Date endTime = new Date();
		Calendar cal = Calendar.getInstance();
		// 현재를 기준으로 months-1 개월 이전 기준으로 startTime
		cal.add(Calendar.MONTH, -(months - 1));
		Date startTime = cal.getTime();
		
		System.out.println("시작 날짜: " + startTime);
		System.out.println("끝 날짜: " + endTime);
		
		dateFormatter = new SimpleDateFormat("yyyy-MM");
		
		return getReportDataByDateRangeInternal(startTime, endTime, reportType);
	}	
	
	public List<ReportItem> getReportDataByDateRange(Date startTime, Date endTime,ReportType reportType) {
		dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		return getReportDataByDateRangeInternal(startTime, endTime,reportType);
	}

	protected abstract List<ReportItem> getReportDataByDateRangeInternal(
			Date startDate, Date endDate, ReportType reportType);
}

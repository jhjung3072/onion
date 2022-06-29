// Sales Report by Date
var data;
var chartOptions;
var totalGrossSales;

var totalItems;

$(document).ready(function() {
	setupButtonEventHandlers("_date", loadSalesReportByDate);
	setupButtonEventHandlers("_date", loadSalesReportByDate2);
});



function loadSalesReportByDate(period) {
	if (period == "custom") {
		startDate = $("#startDate_date").val();
		endDate = $("#endDate_date").val();
		
		requestURL = contextPath + "reports/sales_by_date/" + startDate + "/" + endDate;
	} else {
		requestURL = contextPath + "reports/sales_by_date/" + period;		
	}
	
	$.get(requestURL, function(responseJSON) {
		prepareChartDataForSalesReportByDate(responseJSON);
		customizeChartForSalesReportByDate(period);

		drawChartForSalesReportByDate(period);
		setSalesAmount(period, '_date', "등록 매물 총 개수");
	});
}

function loadSalesReportByDate2(period) {
	if (period == "custom") {
		startDate = $("#startDate_date").val();
		endDate = $("#endDate_date").val();

		requestURL = contextPath + "reports/sales_by_date/" + startDate + "/" + endDate;
	} else {
		requestURL = contextPath + "reports/sales_by_date/" + period;
	}

	$.get(requestURL, function(responseJSON) {
		prepareChartDataForSalesReportByDate2(responseJSON);
		customizeChartForSalesReportByDate2(period);

		drawChartForSalesReportByDate2(period);
	});
}

function prepareChartDataForSalesReportByDate(responseJSON) {
	data = new google.visualization.DataTable();
	data.addColumn('string', '날짜');
	data.addColumn('number', '물건 등록 총 가격');

	totalGrossSales = 0;
	totalItems = 0;
	
	$.each(responseJSON, function(index, reportItem) {
		data.addRows([[reportItem.identifier, reportItem.grossSales]]);
		totalGrossSales += parseInt(reportItem.grossSales);
		totalItems += parseInt(reportItem.productsCount);
	});
}
function prepareChartDataForSalesReportByDate2(responseJSON) {
	data = new google.visualization.DataTable();
	data.addColumn('string', '날짜');
	data.addColumn('number', '매물 수');

	totalGrossSales = 0;
	totalItems = 0;

	$.each(responseJSON, function(index, reportItem) {
		data.addRows([[reportItem.identifier, reportItem.productsCount]]);
		totalItems += parseInt(reportItem.productsCount);
	});
}

function customizeChartForSalesReportByDate(period) {
	chartOptions = {
		title: getChartTitle(period),
		'height': 500,
		legend: {position: 'top'},
		
		series: {
			0: {targetAxisIndex: 0},
		},
		
		vAxes: {
			0: {title: '물건 총 금액',  format: 'decimal'},
		}
	};
}
function customizeChartForSalesReportByDate2(period) {
	chartOptions = {
		'height': 500,
		legend: {position: 'top'},
		hAxis: {
			title: '날짜'
		},
		vAxis: {
			title: '매물수'
		},
		series: {
			1: {curveType: 'function'}
		}
	};
}

function drawChartForSalesReportByDate() {
	var salesChart = new google.visualization.ColumnChart(document.getElementById('chart_sales_by_date'));
	salesChart.draw(data, chartOptions);
}
function drawChartForSalesReportByDate2() {
	var salesChart = new google.visualization.LineChart(document.getElementById('2chart_sales_by_date'));
	salesChart.draw(data, chartOptions);
}

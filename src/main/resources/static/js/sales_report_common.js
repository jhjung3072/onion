// Sales Report Common
var MILLISECONDS_A_DAY = 24 * 60 * 60 * 1000;

function setupButtonEventHandlers(reportType, callbackFunction) {
		
	$(".button-sales-by" + reportType).on("click", function() {
		$(".button-sales-by" + reportType).each(function(e) {
			$(this).removeClass('btn-primary').addClass('btn-light');
		});
		
		$(this).removeClass('btn-light').addClass('btn-primary');
		
		period = $(this).attr("period");
		if (period) {
			callbackFunction(period);
			$("#divCustomDateRange" + reportType).addClass("d-none");
		} else {
			$("#divCustomDateRange" + reportType).removeClass("d-none");
		}		
	});
	
	initCustomDateRange(reportType);
	
	$("#buttonViewReportByDateRange" + reportType).on("click", function(e) {
		validateDateRange(reportType,callbackFunction);
	});	
}

function validateDateRange(reportType,callbackFunction) {
	startDateField = document.getElementById('startDate' + reportType);
	days = calculateDays(reportType);
	
	startDateField.setCustomValidity("");
	
	if (days >= 7 && days <= 30) {
		callbackFunction("custom");
	} else {
		startDateField.setCustomValidity("7~30일 이내의 날짜를 지정해야합니다.");
		startDateField.reportValidity();
	}
}

function calculateDays(reportType) {
	startDateField = document.getElementById('startDate' + reportType);
	endDateField = document.getElementById('endDate' + reportType);
	
	startDate = startDateField.valueAsDate;
	endDate = endDateField.valueAsDate;
	
	differenceInMilliseconds = endDate - startDate;
	return differenceInMilliseconds / MILLISECONDS_A_DAY;
}
	
function initCustomDateRange(reportType) {
	startDateField = document.getElementById('startDate' + reportType);
	endDateField = document.getElementById('endDate' + reportType);
		
	toDate = new Date();
	endDateField.valueAsDate = toDate;
	
	fromDate = new Date();
	fromDate.setDate(toDate.getDate() - 30);
	startDateField.valueAsDate = fromDate;
}	


function getChartTitle(period) {
	if (period == "last_7_days") return "지난 7일 간의 통계";
	if (period == "last_28_days") return "지난 28일 간의 통계";
	if (period == "last_6_months") return "지난 6개월 간의 통계";
	if (period == "last_year") return "작년의 통계";
	if (period == "custom") return "통계 범위 지정";
	
	return "";
}

function getDenominator(period, reportType) {
	if (period == "last_7_days") return 7;
	if (period == "last_28_days") return 28;
	if (period == "last_6_months") return 6;
	if (period == "last_year") return 12;
	if (period == "custom") return calculateDays(reportType);
	
	return 7;
}

function setSalesAmount(period, reportType, labelTotalItems) {
	$("#textTotalGrossSales" + reportType).text(totalGrossSales);

	let denominator = getDenominator(period, reportType);
	
	$("#textAvgGrossSales" + reportType).text(totalGrossSales / denominator);
	$("#labelTotalItems" + reportType).text(labelTotalItems);
	$("#textTotalItems" + reportType).text(totalItems);	
}




package com.onion.report;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportController {

	@GetMapping("/reports")
	public String viewSalesReportHome() {
		return "reports/reports";
	}

}

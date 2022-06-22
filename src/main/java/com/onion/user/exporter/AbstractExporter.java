package com.onion.user.exporter;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

// 엑셀 파일 내보낼때 파일이름 설정 클래스
public class AbstractExporter {

	public void setResponseHeader(HttpServletResponse response, String contentType, 
			String extension, String prefix) throws IOException {
		
		
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String timestamp = dateFormatter.format(new Date());
		String fileName = prefix + timestamp + extension;
		
		response.setContentType(contentType);
		response.setCharacterEncoding("UTF-8");
		
		String encordedFilename = URLEncoder.encode(fileName,"UTF-8").replace("+", "%20");

		response.setHeader("Content-Disposition",
				  "attachment;filename=" + encordedFilename + ";filename*= UTF-8''" + encordedFilename);
		
		
	}	
}

package com.onion.user.exporter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.onion.domain.User;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


// excel 행과 열 매핑 클래스
public class UserExcelExporter extends AbstractExporter {
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	
	public UserExcelExporter() {
		workbook = new XSSFWorkbook();
	}

	private void writeHeaderLine() {
		sheet = workbook.createSheet("Users");
		XSSFRow row = sheet.createRow(0);
		
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setBold(true);
		font.setFontHeight(16);
		cellStyle.setFont(font);
		
		createCell(row, 0, "유저 ID", cellStyle);
		createCell(row, 1, "이메일", cellStyle);
		createCell(row, 2, "닉네임", cellStyle);
		createCell(row, 3, "가입시간", cellStyle);
		createCell(row, 4, "활성화", cellStyle);
		
	}
	
	private void createCell(XSSFRow row, int columnIndex, Object value, CellStyle style) {
		XSSFCell cell = row.createCell(columnIndex);
		sheet.autoSizeColumn(columnIndex);
		
		if (value instanceof Integer) {
			cell.setCellValue((Integer) value);
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else {
			cell.setCellValue((String) value);
		}
		
		cell.setCellStyle(style);		
	}
	
	public void export(List<User> listUsers, HttpServletResponse response) throws IOException {
		super.setResponseHeader(response, "application/octet-stream", ".xlsx", "유저 목록_");
	
		writeHeaderLine();
		writeDataLines(listUsers);
		
		ServletOutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();
		
		
	}

	private void writeDataLines(List<User> listUsers) {
		int rowIndex = 1;
		
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setFontHeight(14);
		cellStyle.setFont(font);
		
		for (User user : listUsers) {
			XSSFRow row = sheet.createRow(rowIndex++);
			int columnIndex = 0;
			
			createCell(row, columnIndex++, user.getId(), cellStyle);
			createCell(row, columnIndex++, user.getEmail(), cellStyle);
			createCell(row, columnIndex++, user.getNickname(), cellStyle);
			createCell(row, columnIndex++, user.getJoinedAt().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h시 m분")), cellStyle);
			createCell(row, columnIndex++, user.isEnabled(), cellStyle);
		}
	}
}

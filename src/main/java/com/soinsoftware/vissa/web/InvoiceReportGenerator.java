package com.soinsoftware.vissa.web;

import java.io.File;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.soinsoftware.vissa.report.ExcelWriter;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("rawtypes")
public class InvoiceReportGenerator<Document> extends ExcelWriter {

	public InvoiceReportGenerator(String fileName) {
		super(fileName);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void createInformationRows(Sheet sheet, List columns, List dataInList) {
		CellStyle dateCellStyle = workbook.createCellStyle();
		dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(Commons.FORMAT_DATE_TIME));
		int rowNum = 1;
		for (Object data : dataInList) {
			Row row = sheet.createRow(rowNum++);

			// Override and change to map the cell with the POJO

			com.soinsoftware.vissa.model.Document document = (com.soinsoftware.vissa.model.Document) data;

			row.createCell(0).setCellValue(document.getCode());
			row.createCell(1).setCellValue(document.getDocumentType().getName());
			row.createCell(2).setCellValue(document.getPaymentType().getName());
			row.createCell(3).setCellValue(document.getPaymentStatus());

			Cell dateOfBirthCell = row.createCell(4);
			dateOfBirthCell.setCellValue(document.getDocumentDate());
			dateOfBirthCell.setCellStyle(dateCellStyle);

			row.createCell(5).setCellValue(document.getPerson().getName() + " " + document.getPerson().getLastName());
			
			row.createCell(6).setCellValue(document.getTotalValue());

		}

		for (int i = 0; i < columns.size(); i++) {
			sheet.autoSizeColumn(i);
		}
		
		
	}

}

package com.soinsoftware.vissa.web;

import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.report.ExcelWriter;
import com.soinsoftware.vissa.util.Commons;

@SuppressWarnings("rawtypes")
public class ProductReportGenerator<Document> extends ExcelWriter {

	public ProductReportGenerator(String fileName) {
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

			Product product = (com.soinsoftware.vissa.model.Product) data;

			row.createCell(0).setCellValue(product.getCode());
			row.createCell(1).setCellValue(product.getName());
			row.createCell(2).setCellValue(product.getCategory() != null ? product.getCategory().getName() : "");
			row.createCell(3).setCellValue(product.getStock());
			row.createCell(4)
					.setCellValue(product.getMeasurementUnit() != null ? product.getMeasurementUnit().getName() : "");

		}

		for (int i = 0; i < columns.size(); i++) {
			sheet.autoSizeColumn(i);
		}

	}

}

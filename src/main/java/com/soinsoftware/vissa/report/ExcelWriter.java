package com.soinsoftware.vissa.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class ExcelWriter<E> {

	protected final String fileName;
	protected final Workbook workbook;
	protected final CreationHelper createHelper;

	public ExcelWriter(final String fileName) {
		this.fileName = fileName;
		workbook = new XSSFWorkbook();
		createHelper = workbook.getCreationHelper();
	}

	private Font createFontHeader() {
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 14);
		headerFont.setColor(IndexedColors.RED.getIndex());
		return headerFont;
	}

	private CellStyle createCellStyle() {
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(createFontHeader());
		return headerCellStyle;
	}

	private void createHeaderRow(final Sheet sheet, final List<String> columns) {
		CellStyle headerCellStyle = createCellStyle();
		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < columns.size(); i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columns.get(i));
			cell.setCellStyle(headerCellStyle);
		}
	}

	protected abstract void createInformationRows(final Sheet sheet, final List<String> columns,
			final List<E> dataInList);

	public void createSheet(final String sheetName, final List<String> columns, final List<E> dataInList) {
		Sheet sheet = workbook.createSheet(sheetName);
		createHeaderRow(sheet, columns);
		createInformationRows(sheet, columns, dataInList);
	}

	public void exportFile() throws IOException, InvalidFormatException {
		try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
			workbook.write(fileOut);
		} finally {
			workbook.close();
		}
	}
}
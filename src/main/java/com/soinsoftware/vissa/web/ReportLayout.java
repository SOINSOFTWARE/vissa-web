package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.soinsoftware.vissa.bll.DocumentDetailBll;
import com.soinsoftware.vissa.util.AdvancedFileDownloader;
import com.soinsoftware.vissa.util.AdvancedFileDownloader.AdvancedDownloaderListener;
import com.soinsoftware.vissa.util.AdvancedFileDownloader.DownloaderEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;

public class ReportLayout extends VerticalLayout implements View {

	private static final long serialVersionUID = -1662071302695761216L;
	protected static final String REPORT_NAME = "/WEB-INF/reports/invoice.jrxml";

	public static final String PARAM_COMPANY = "Company";
	public static final String PARAM_INVOICE_NUMBER = "InvoiceNumber";
	public static final String PARAM_CUSTOMER = "Customer";
	public static final String PARAM_INVOICE_DATE = "InvoiceDate";
	public static final String PARAM_REPORT_NAME = "ReportName";
	private final DocumentDetailBll detailBll;
	//private final PdfGenerator pdfGenerator;

	public ReportLayout() throws IOException {
		detailBll = DocumentDetailBll.getInstance();
		//pdfGenerator = new PdfGenerator(
			//	new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + REPORT_NAME), "ventas");
	}

	@Override
	public void enter(ViewChangeEvent event) {
		View.super.enter(event);
		Button button = new Button("descargar");
		final AdvancedFileDownloader downloader = new AdvancedFileDownloader();
		downloader.addAdvancedDownloaderListener(new AdvancedDownloaderListener() {

			/**
			 * This method will be invoked just before the download starts. Thus, a new file
			 * path can be set.
			 *
			 * @param downloadEvent
			 */
			@Override
			public void beforeDownload(DownloaderEvent downloadEvent) {
			/*	try {
					String filePath = pdfGenerator.generate(createParameters(), detailBll.selectAll());
					downloader.setFilePath(filePath);
					System.out.println("Starting downlad by button " + filePath.substring(filePath.lastIndexOf("/")));
				} catch (GeneratorException ex) {
					ex.printStackTrace();
				}
				*/
			}

			private Map<String, Object> createParameters() {
				final Map<String, Object> parameters = new HashMap<>();
				parameters.put(PARAM_COMPANY, "Vissa");
				parameters.put(PARAM_INVOICE_NUMBER, "123456789");
				parameters.put(PARAM_INVOICE_DATE,
						LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
				parameters.put(PARAM_REPORT_NAME, "reporte");
				parameters.put(PARAM_CUSTOMER, "Cliente 1");

				return parameters;
			}
		});

		downloader.extend(button);
		this.addComponent(button);
	}
}
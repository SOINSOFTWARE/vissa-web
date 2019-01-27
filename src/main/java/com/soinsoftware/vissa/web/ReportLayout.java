package com.soinsoftware.vissa.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.soinsoftware.vissa.bll.UserBll;
import com.soinsoftware.vissa.util.AdvancedFileDownloader;
import com.soinsoftware.vissa.util.AdvancedFileDownloader.AdvancedDownloaderListener;
import com.soinsoftware.vissa.util.AdvancedFileDownloader.DownloaderEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.exception.DRException;

public class ReportLayout extends VerticalLayout implements View {

	private static final long serialVersionUID = -1662071302695761216L;
	protected static final String REPORT_NAME = "/reports/accountabilityReport.jasper";
	public static final String PARAM_CEO = "CEO";
	public static final String PARAM_COMPANY = "Company";
	public static final String PARAM_DOCUMENT = "Document";
	public static final String PARAM_DOCUMENT_CEO = "DocumentCEO";
	public static final String PARAM_IS_JURIDICA = "IsJuridica";
	public static final String PARAM_REPORT_DATE = "ReportDate";
	public static final String PARAM_REPORT_NAME = "ReportName";
	private final UserBll userBll;

	public ReportLayout() throws IOException {
		userBll = UserBll.getInstance();
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
				JasperReportBuilder report = DynamicReports.report();
				try {
					report.setTemplateDesign(new File(REPORT_NAME));
					report.setParameters(this.createParameters()).setDataSource(userBll.selectAll());
					/*
					 * report.columns(Columns.column("Login", "login", DataTypes.stringType()),
					 * Columns.column("First Name", "person.name", DataTypes.stringType()),
					 * Columns.column("Last Name", "person.lastName", DataTypes.stringType()),
					 * Columns.column("Role", "role.name", DataTypes.stringType()))
					 * .title(Components.text("SimpleReportExample")
					 * .setHorizontalAlignment(HorizontalAlignment.CENTER))
					 * .pageFooter(Components.pageXofY()).setDataSource(userBll.selectAll());
					 */
					try {
						File file = File.createTempFile("user", "pdf");
						report.toPdf(new FileOutputStream(file));
						String filePath = file.getAbsolutePath();

						downloader.setFilePath(filePath);

						System.out
								.println("Starting downlad by button " + filePath.substring(filePath.lastIndexOf("/")));
					} catch (DRException | IOException e) {
						e.printStackTrace();
					}
				} catch (DRException e1) {
					e1.printStackTrace();
				}
			}

			private Map<String, Object> createParameters() {
				final Map<String, Object> parameters = new HashMap<>();
				parameters.put(PARAM_COMPANY, "Vissa");
				parameters.put(PARAM_DOCUMENT, "123456789");
				parameters.put(PARAM_REPORT_DATE,
						LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
				parameters.put(PARAM_REPORT_NAME, "reporte");
				parameters.put(PARAM_IS_JURIDICA, true);
				parameters.put(PARAM_CEO, "CEO");
				parameters.put(PARAM_DOCUMENT_CEO, "123456789");

				return parameters;
			}
		});

		downloader.extend(button);
		this.addComponent(button);
	}
}
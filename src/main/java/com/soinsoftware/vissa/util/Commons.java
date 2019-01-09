package com.soinsoftware.vissa.util;

public class Commons {

	public static String DOCUMENT_TYPE;
	public static final String FORMAT_DATE = "dd/MM/yyyy HH:mm:ss";
	public static Double documentTotal;

	public String getDocumentType() {
		return DOCUMENT_TYPE;
	}

	public static Double getDocumentTotal() {
		return documentTotal;
	}

	public static void setDocumentTotal(Double documentTotal) {
		Commons.documentTotal = documentTotal;
	}
	
	
	

}

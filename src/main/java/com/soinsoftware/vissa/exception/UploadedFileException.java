package com.soinsoftware.vissa.exception;

public class UploadedFileException extends RuntimeException {

	private static final long serialVersionUID = 8935510497496195427L;
	
	public UploadedFileException(String message) {
		super(message);
	}
}
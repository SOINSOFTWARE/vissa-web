package com.soinsoftware.vissa.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.soinsoftware.vissa.exception.UploadedFileException;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

public class ImageUploader implements Receiver, SucceededListener {
    
	private static final long serialVersionUID = -4719012826720515318L;
	private static List<String> allowedFileTypes = Arrays.asList("jpg", "jpeg", "png"); 
	private final Image imageField;
	private File file;
	
	public ImageUploader(final Image imageField, final boolean allowPngFiles) {
		this.imageField = imageField;
		if (allowPngFiles) {
			allowedFileTypes.add("png");
		}
	}

    public OutputStream receiveUpload(String fileName, String mimeType) {
    	FileOutputStream fileOutputStream = null;
    	try {
    		String[] fileNameSplit = fileName.split("\\.");
    		if (allowedFileTypes.contains(fileNameSplit[1].toLowerCase())) {
    			file = File.createTempFile(fileNameSplit[0], fileNameSplit[1]);
    			fileOutputStream = new FileOutputStream(file);
    		} else {
    			throw new UploadedFileException("Solo se permite adjuntar archivos: " + allowedFileTypes.toString());
    		}
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
        	new Notification("El archivo no puede ser cargado, intentelo con uno diferente.", fileName, Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
        } catch (UploadedFileException e) {
        	new Notification(e.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		}
        return fileOutputStream;
    }

    public void uploadSucceeded(SucceededEvent event) {
    	imageField.setSource(new FileResource(file));
    }
}
package com.soinsoftware.vissa.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.StreamUtils;

import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;

public class StreamResourceUtil {
	
	public static byte[] getByteArray(Resource resource) throws IOException {
		byte[] byteArray = null;
		if (resource != null) {
			if (resource instanceof FileResource) {
				byteArray = StreamUtils.copyToByteArray(((FileResource) resource).getStream().getStream());
			} else if (resource instanceof StreamResource) {
				byteArray = StreamUtils.copyToByteArray(((StreamResource) resource).getStream().getStream());
			}
		}
		return byteArray;
	}
	
	public static StreamResource loadImage(final byte[] byteArray) {
		StreamSource streamSource = new StreamResource.StreamSource() {
			private static final long serialVersionUID = 6685907115411208560L;

			public InputStream getStream() {
				return (byteArray == null) ? null : new ByteArrayInputStream(byteArray);
			}
		};
		return new StreamResource(streamSource, "streamedSourceFromByteArray");
	}
}
package com.developer.bsince.data;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public interface IDataSet {

	void write(OutputStream os) throws IOException;
	
	String getEncoding();
	
	String getContentType();
	
	void setPropertyBeforeConnect(HttpURLConnection conn);
	
	boolean isChunkedStreamingMode();
	
	
	
}

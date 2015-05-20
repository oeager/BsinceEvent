package com.developer.bsince.data;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import com.developer.bsince.core.assist.HttpConstants;

public class FormEncodeDataSet implements IDataSet {

	private final StringBuilder builder = new StringBuilder();

	private String charset;

	public FormEncodeDataSet() {
		setCharset(HttpConstants.DEFAULT_CHARSET);
	}

	@Override
	public void write(OutputStream os) throws IOException {

		byte[] data = builder.toString().getBytes(charset);
		os.write(data);

	}

	public void put(String name, String value) {
		if (builder.length() > 0) {
			builder.append('&');
		}
		try {
			builder.append(URLEncoder.encode(name, charset)).append('=')
					.append(URLEncoder.encode(value, charset));
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	public void put(String name, Collection<String> list) {
		if (list != null && list.size() > 0) {

			for (String value : list) {

				put(name, value);
			}
		}
	}
	
	public void put(String name,String [] arrays){
		
		if(arrays!=null&&arrays.length>0){
			for (String value : arrays) {
				put(name, value);
			}
		}
	}
	
	public void put(String name,Map<String, String> map){
		for (Map.Entry<String, String> entry : map.entrySet()) {
			put(String.format(Locale.US, "%s[%s]",name,entry.getKey()), entry.getValue());
		}
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	public String getEncoding() {
		return null;
	}

	@Override
	public String getContentType() {
		return HttpConstants.CONTENT_TYPE_DEFAULT + "; charset=" + charset;
	}

	@Override
	public void setPropertyBeforeConnect(HttpURLConnection conn) {

	}

	@Override
	public boolean isChunkedStreamingMode() {
		return false;
	}

	@Override
	public String toString() {
		return builder.toString();
	}

}

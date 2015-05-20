package com.developer.bsince.data;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import com.developer.bsince.core.assist.HttpGlobalConfiguration;

import android.util.Log;

public abstract class AbstractDataSet implements IDataSet {

	protected final static String TAG = "DataSet";

	protected final Map<String, Object> params;
	private String generatedDigest = null;

	private String charset;

	private String contentType;

	public AbstractDataSet() {

		params = createDataSetCollections();
		init();
	}

	protected abstract void init();

	public void put(String name, Object values) {
		params.put(name, values);
	}

	public void put(Object... nameValueParis) {
		if (nameValueParis == null) {
			if (HttpGlobalConfiguration.debug) {
				Log.w(TAG, "the name valuePairs is null");
			}
			return ;
		}
		if (nameValueParis.length % 2 != 0) {
			if (HttpGlobalConfiguration.debug) {
				Log.w(TAG, "the length of valuePairs is odd");
			}
			return ;
		}
		for (int i = 0; i < nameValueParis.length; i += 2) {
			params.put(nameValueParis[i].toString(), nameValueParis[i + 1]);
		}
		return ;

	}

	/**
	 * just used it for cacheKey or get param[if you used for get param ,the
	 * dataset can not contains Collections,file,or inputstream]
	 */
	@Override
	public String toString() {

		if (generatedDigest == null) {
			StringBuilder encodedParams = new StringBuilder();

			for (Map.Entry<String, Object> entry : params.entrySet()) {
				encodedParams.append(entry.getKey());
				encodedParams.append('=');
				encodedParams.append(entry.getValue().toString());
				encodedParams.append('&');
			}
			generatedDigest = encodedParams.toString();
		}
		return generatedDigest;

	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getCharset() {
		return charset;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String getContentType() {
		return contentType + "; charset=" + charset;
	}

	@Override
	public String getEncoding() {
		return null;
	}

	@Override
	public boolean isChunkedStreamingMode() {
		return false;
	}

	@Override
	public void setPropertyBeforeConnect(HttpURLConnection conn) {

	}

	protected <K, V> Map<K, V> createDataSetCollections() {
		return new HashMap<>();
	}

}

package com.developer.bsince.core.assist;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import com.developer.bsince.core.Headers;
import com.developer.bsince.core.HttpHeaderParser;

public class AuthorResponse {

	private final int responseCode;
	
	private final Headers responseHeaders;
	
	private final  Proxy proxy;
	
	private final URL url;


	public AuthorResponse(int responseCode, Headers responseHeaders,
			Proxy proxy, URL url) {
		this.responseCode = responseCode;
		this.responseHeaders = responseHeaders;
		this.proxy = proxy;
		this.url = url;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public Headers getResponseHeaders() {
		return responseHeaders;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public URL getUrl() {
		return url;
	}
	
	public  List<Challenge> getChallenge() {
		String responseField;
		if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
			responseField = "WWW-Authenticate";
		} else if (responseCode == HttpURLConnection.HTTP_PROXY_AUTH) {
			responseField = "Proxy-Authenticate";
		} else {
			return Collections.emptyList();
		}
		return HttpHeaderParser.parseChallenges(responseHeaders, responseField);
	}
	
	
}

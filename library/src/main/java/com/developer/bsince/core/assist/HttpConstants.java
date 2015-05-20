package com.developer.bsince.core.assist;

import org.apache.http.protocol.HTTP;

public class HttpConstants {

	/**
	 * HTTP HEADERS
	 */

	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	
	public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
	
	public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	
	public static final String HEADER_USER_AGENT="User-Agent";
	
	public static final String HEADER_CONNECTION="Connection";
	
	public static final String HEADER_COOKIE="Cookie";
	
	public static final String HEADER_SET_COOKIE="Set-Cookie";
	
	public static final String HEADER_CONTENT_DISPOSITION="Content-Disposition";
	
	public static final String HEADER_AUTHORIZATION="Authorization";
	
	public static final String HEADER_PROXY_AUTHORIZATION="Proxy-Authorization";
	
	public static final String HEADER_IF_NONE_MATCH="If-None-Match";

	public static final String HEADER_IF_MODIFIED="If-Modified-Since";
	
	public static final String HEADER_CACHE_CONTROL="Cache-Control";
	
	/**
	 * HEADER VALUE
	 */

	public static final String ENCODING_GZIP = "gzip";

	public final static String DEFAULT_CHARSET = HTTP.UTF_8;
	
	public static final String CONNECTION_KEEP_ALIVE="keep-alive";
	
	public static final String CONNECTION_CLOSE="close";
	
	

	/**
	 * Application-Content Value
	 */

	public static final String CONTENT_TYPE_DEFAULT = "application/x-www-form-urlencoded";

	public static final String CONTENT_TYPE_JSON = "application/json";

	public static final String CONTENT_TYPE_XML = "text/xml";
	
	public static final String CONTENT_TYPE_PLAIN="text/plain";

	public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

	public static final String CONTENT_TYPE_SOAP_XML = "application/soap+xml";

	public final static String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
	/**
	 * HTTP DEFAULT SETTINGS
	 */
	
    public static final int DEFAULT_TIMEOUT_MS = 5500;
    
    public static final int DEFAULT_MAX_RETRIES = 2;
    
    public static final float DEFAULT_BACKOFF_MULT = 1f;
    
    public static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    
    public static final int DEFAULT_NETWORK_THREAD_POOL_SIZE =3;
    
    /** Default maximum disk usage in bytes. */
    public static final int DEFAULT_DISK_USAGE_BYTES = 10 * 1024 * 1024;
}

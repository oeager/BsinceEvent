package com.developer.bsince.core;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.cookie.DateUtils;

import android.text.TextUtils;
import android.util.Log;

import com.developer.bsince.cache.HttpCache;
import com.developer.bsince.core.assist.AuthorResponse;
import com.developer.bsince.core.assist.ByteArrayPool;
import com.developer.bsince.core.assist.HttpConstants;
import com.developer.bsince.core.assist.HttpGlobalConfiguration;
import com.developer.bsince.core.assist.PoolingByteArrayOutputStream;
import com.developer.bsince.data.IDataSet;
import com.developer.bsince.event.Event;
import com.developer.bsince.event.HttpEventImp;
import com.developer.bsince.exceptions.ConnectFailedException;
import com.developer.bsince.exceptions.ServerErrorException;
import com.developer.bsince.exceptions.UnAuthorizationException;
import com.developer.bsince.extras.IOHelper;
import com.developer.bsince.request.CacheModel;
import com.developer.bsince.request.Method;
import com.developer.bsince.request.RetryPolicy;
import com.developer.bsince.response.NetworkResponse;

public class BaseNetworker implements IWork {

	private final static String TAG = "Network";

	protected final ByteArrayPool mPool;

	private static int DEFAULT_POOL_SIZE = 4096;

	public BaseNetworker() {
		this(new ByteArrayPool(DEFAULT_POOL_SIZE));
	}

	public BaseNetworker(ByteArrayPool pool) {
		mPool = pool;
	}

	@Override
	public <T> NetworkResponse requestEvent(HttpEventImp<T> event)
			throws Exception {

		while (true) {

			int responseCode = 0;
			InputStream inputStream = null;
			try {

				URL url = new URL(event.url);
				Proxy proxy = event.mProxy;
				
				HttpURLConnection conn = (HttpURLConnection)(proxy==null?url.openConnection():url.openConnection(proxy));
				conn.setRequestMethod(event.method);
				conn.setInstanceFollowRedirects(event.allowRedirect);
				conn.setConnectTimeout(event.connectTimeOut);
				conn.setReadTimeout(event.readTimeOut);
				conn.setUseCaches(false);
				conn.setDoInput(true);
				if ("https".equals(url.getProtocol())) {
					SSLSocketFactory factory = event.mSSLFactory;
					if (factory != null) {
						((HttpsURLConnection) conn)
								.setSSLSocketFactory(factory);
					}

				}
				addRequestHeaders(conn,event);
				
				// handle cookie

				// add body if exist
				outPutBodyifExist(event, conn);
				/****
				 * **************************************requestSeaparator******
				 * ************************
				 */
				responseCode = conn.getResponseCode();
				if (responseCode == -1) {
					// -1 is returned by getResponseCode() if the response code
					// could
					// not be retrieved.
					// Signal to the caller that something was wrong with the
					// connection.
					throw new IOException(
							"Could not retrieve response code from HttpUrlConnection.");
				}
				Headers responseHeaders = convertResponseHeaders(conn.getHeaderFields());

				if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
					return new NetworkResponse(HttpURLConnection.HTTP_NOT_MODIFIED,
							event.cacheEntry == null ? null
									: event.cacheEntry.data, responseHeaders, true);
				}

				String contentEncoding = conn.getContentEncoding();

				try {

					if (!TextUtils.isEmpty(contentEncoding)
							&& contentEncoding
									.equalsIgnoreCase(HttpConstants.ENCODING_GZIP)) {
						inputStream = new GZIPInputStream(conn.getInputStream());
					} else {
						inputStream = conn.getInputStream();
					}
				} catch (IOException ioe) {
					inputStream = conn.getErrorStream();
				}

				if (responseCode < 200 || responseCode > 299) {
					
					if(isUnAuthorException(responseCode)&&event.authenticator!=null){
						AuthorResponse authResponse = new AuthorResponse(responseCode, responseHeaders, event.mProxy, url);
						String credential;
						if(responseCode==HttpURLConnection.HTTP_PROXY_AUTH){
							credential = event.authenticator.authenticateProxy(authResponse);
							if(!TextUtils.isEmpty(credential)){
								event.setHeaderProperty(HttpConstants.HEADER_PROXY_AUTHORIZATION, credential);
							}
							
						}else{
							credential = event.authenticator.authenticate(authResponse);
							if(!TextUtils.isEmpty(credential)){
								event.setHeaderProperty(HttpConstants.HEADER_AUTHORIZATION, credential);
							}
						}
					}
					
					throw new IOException();
				}
				// [--------------------------------------------------------]
				
				// [---------when download large files,must set cacheMode
				// [Cache_Never]-----]
				// [--------------------------------------------------------]
				if (CacheModel.invalCache(event.mCacheModel)) {
					NetworkResponse response = new NetworkResponse(responseCode, inputStream,
							responseHeaders, false);
					response.contentLength = conn.getContentLength();
					return response;
				} else {
					// some requests like response 204
					byte[] responseContents;
					if (inputStream != null) {
						responseContents = entityToBytes(inputStream,
								conn.getContentLength(),event.bufferSize);
					} else {
						// 实例化byte[]
						responseContents = new byte[0];
					}

					return new NetworkResponse(responseCode, responseContents,
							responseHeaders, false);
				}

			} catch (SocketTimeoutException e) {
				attemptRetryOnException("socket", event, e);
			} catch (ConnectTimeoutException e) {
				attemptRetryOnException("connection", event, e);
			} catch (MalformedURLException e) {
				throw new RuntimeException("Bad URL " + event.url, e);
			} catch (IOException e) {

				if (inputStream == null) {
					throw new ConnectFailedException(
							"connect to server failed",e);
				}
				String errorInfo = IOHelper.streamToString(inputStream);

				if (HttpGlobalConfiguration.debug) {
					Log.e(TAG, "Unexpected response code " + responseCode
							+ " for " + event.url);
				}
				if (isUnAuthorException(responseCode)) {
					attemptRetryOnException("auth", event,
							new UnAuthorizationException("responseCode="+responseCode+",errorInfo:"+errorInfo));
				} else {
					throw new ServerErrorException("responseCode="
							+ responseCode);
				}
			}
		}
	}
	
	public static boolean isUnAuthorException(int responseCode){
		return responseCode == HttpURLConnection.HTTP_UNAUTHORIZED||responseCode==HttpURLConnection.HTTP_PROXY_AUTH
				|| responseCode == HttpURLConnection.HTTP_FORBIDDEN;
	}
	
	private static Headers convertResponseHeaders(Map<String, List<String>> headerFields) {
		Headers.Builder builder = new Headers.Builder();

		for (Map.Entry<String, List<String>> header : headerFields
				.entrySet()) {
			String key = header.getKey();
			if (key != null) {
				List<String> values = header.getValue();
				
				for (String value : values) {
					builder.add(key,value);
				}
			}

		}
		return builder.build();
	}

	private void addRequestHeaders(HttpURLConnection conn,HttpEventImp<?> event){
		Headers mHeaders = event.getHeaders();
		int size = mHeaders.size();
		for (int i = 0; i < size; i++) {
			conn.addRequestProperty(mHeaders.name(i), mHeaders.value(i));
		}
		HttpCache.Entry entry =event.cacheEntry;
		// add cache headers;
		if (entry == null) {
			return;
		}

		if (entry.etag != null) {
			conn.addRequestProperty(HttpConstants.HEADER_IF_NONE_MATCH,
					entry.etag);
		}
		if(entry.lastModifiedTime>0){
       	 Date refTime = new Date(entry.lastModifiedTime);
       	 conn.addRequestProperty("If-Modified-Since", DateUtils.formatDate(refTime));
       	 return;
       }
		if (entry.serverDate > 0) {
			Date refTime = new Date(entry.serverDate);
			conn.addRequestProperty(HttpConstants.HEADER_IF_MODIFIED,
					DateUtils.formatDate(refTime));
		}
	}
	
	
	protected static void outPutBodyifExist(HttpEventImp<?> event,HttpURLConnection conn)throws IOException{
		String method = event.method;
		
		if(Method.permitsRequestBody(method)){
			IDataSet ds = event.mDataSet;
			if(ds!=null){
				conn.setDoOutput(true);
				conn.setRequestProperty(HttpConstants.HEADER_CONTENT_TYPE, ds.getContentType());
				if(ds.getEncoding()!=null){
					conn.setRequestProperty(HttpConstants.HEADER_CONTENT_ENCODING, ds.getEncoding());
				}
				if(ds.isChunkedStreamingMode()){
					conn.setChunkedStreamingMode(256*1024);
				}
				ds.setPropertyBeforeConnect(conn);
				DataOutputStream out = new DataOutputStream(conn.getOutputStream());
				try {
					ds.write(out);
				} catch (IOException e) {
					throw e;
				}finally{
					IOHelper.silentCloseOutputStream(out);
				}
				
			}
		}
	}
	

	private static void attemptRetryOnException(String logPrefix,
			Event<?> request, Exception exception) throws Exception {
		if (request instanceof HttpEventImp) {
			HttpEventImp<?> event = (HttpEventImp<?>) request;
			RetryPolicy retryPolicy = event.policy;

			int oldTimeout = event.connectTimeOut;

			try {
				retryPolicy.retry(exception);
			} catch (Exception e) {
				if (HttpGlobalConfiguration.debug) {
					Log.d(TAG, String.format("%s-timeout-giveup [timeout=%s]",
							logPrefix, oldTimeout));
				}

				throw exception;
			}
			if (HttpGlobalConfiguration.debug) {
				Log.d(TAG, String.format("%s-retry [timeout=%s]", logPrefix,
						oldTimeout));
			}
		}

	}

	private byte[] entityToBytes(InputStream in, int contentLength,int buffersize)
			throws Exception {
		PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(
				mPool, contentLength);
		byte[] buffer = null;
		try {

			if (in == null) {
				throw new NullPointerException("entity.getContent() is null...");
			}
			buffer = mPool.getBuf(buffersize);
			int count;
			while ((count = in.read(buffer)) != -1) {
				bytes.write(buffer, 0, count);
			}
			return bytes.toByteArray();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				//
				if (HttpGlobalConfiguration.debug) {
					Log.v(TAG, "Error occured when calling consumingContent");
				}
			}
			mPool.returnBuf(buffer);
			bytes.close();
		}
	}

	
}

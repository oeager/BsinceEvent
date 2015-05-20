package com.developer.bsince.event;

import java.io.IOException;
import java.net.Proxy;

import javax.net.ssl.SSLSocketFactory;

import android.util.Log;

import com.developer.bsince.cache.HttpCache;
import com.developer.bsince.core.Authenticator;
import com.developer.bsince.core.Headers;
import com.developer.bsince.core.HttpHeaderParser;
import com.developer.bsince.core.IWork;
import com.developer.bsince.core.assist.HttpGlobalConfiguration;
import com.developer.bsince.data.IDataSet;
import com.developer.bsince.exceptions.ParseException;
import com.developer.bsince.parser.IParser;
import com.developer.bsince.request.CacheModel;
import com.developer.bsince.request.Method.HttpMethod;
import com.developer.bsince.request.RetryPolicy;
import com.developer.bsince.response.NetworkResponse;
import com.developer.bsince.response.ResponseListener;
import com.developer.bsince.response.ResponseResult;

public class HttpEventImp<T> extends Event<T> {

	private static final String TAG = "HttpEventImp";

	public final int connectTimeOut;

	public final int readTimeOut;

	public final int bufferSize;

	public final boolean allowRedirect;

	public final String url;

	public final @HttpMethod String method;

	public final Proxy mProxy;

	public final IDataSet mDataSet;

	public final SSLSocketFactory mSSLFactory;

	public final IWork mNetwork;

	public final int mCacheModel;

	public final HttpCache mCache;

	public final IParser<T> mParser;

	public HttpCache.Entry cacheEntry;

	public final RetryPolicy policy;

	public final Authenticator authenticator;

	private Headers mHeaders;
	
	public final Object [] extra;

	public HttpEventImp(EventPublisher.Builder<T> builder, IWork mNetwork,
			HttpCache mCache, ResponseListener<T> responseListener) {
		super(responseListener);

		this.mCacheModel = builder.mCacheModel;
		this.connectTimeOut = builder.connectTimeOut;
		this.readTimeOut = builder.readTimeOut;
		this.bufferSize = builder.bufferSize;
		this.allowRedirect = builder.allowRedirect;
		this.url = builder.url;
		this.method = builder.method;
		this.mProxy = builder.mProxy;
		this.mDataSet = builder.mDataSet;
		this.mParser = builder.mParser;
		this.mHeaders = builder.mHeaders.build();
		this.mSSLFactory = builder.mSocketFactory;
		this.policy = builder.retryPolicy;
		this.authenticator = builder.authenticator;
		this.extra = builder.extraData;
		this.mNetwork = mNetwork;
		this.mCache = mCache;
	}

	public void setCacheEntry(HttpCache.Entry cacheEntry) {
		this.cacheEntry = cacheEntry;
	}

	public Headers getHeaders() {
		return mHeaders;
	}

	public void setHeaderProperty(String name, String value) {

		mHeaders = mHeaders.newBuilder().set(name, value).build();

	}

	public String getCacheKey() {
		if (mDataSet == null) {
			return url;
		}
		return url + mDataSet.toString();

	}

	public ResponseResult<T> parseNetworkResponse(NetworkResponse response) {
		try {
			cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
			return ResponseResult.success(mParser.parsed(response, this));
		} catch (ParseException e) {
			if (response.ioData != null) {
				try {
					response.ioData.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return ResponseResult.error(e);
		}
	}
	

	@Override
	public ResponseResult<T> call() throws Exception {
		NetworkResponse networkResponse = mNetwork.requestEvent(this);

		if (networkResponse.notModified && hasHadResponseDelivered()) {
			return null;
		}
		switch (mCacheModel) {
		case CacheModel.CACHE_FOREVER:
			if (cacheEntry == null && networkResponse != null) {
				cacheEntry = new HttpCache.Entry();
				cacheEntry.data = networkResponse.data;
				mCache.put(getCacheKey(), cacheEntry);
				if(HttpGlobalConfiguration.debug){
					Log.d(TAG, "CacheModel.CACHE_FOREVER,put it in cache");
				}
				break;
			}

		case CacheModel.CACHE_BY_SERVER:
			if (cacheEntry != null) {
				mCache.put(getCacheKey(), cacheEntry);
				if(HttpGlobalConfiguration.debug){
					Log.d(TAG, "CacheModel.CACHE_BY_SERVER,put it in cache");
				}
			}else{
				if(HttpGlobalConfiguration.debug){
					Log.d(TAG, "CacheModel.CACHE_BY_SERVER,cacheEntry is null,do not put it to cache");
				}
			}
			break;
		case CacheModel.CACHE_NEVER:
			if(HttpGlobalConfiguration.debug){
				Log.d(TAG, "CacheModel.CACHE_NEVER,do nothing about cache");
			}
			break;
		}
		ResponseResult<T> response = parseNetworkResponse(networkResponse);
		return response;
	}

	@Override
	public void runExtras(ResponseResult<?> response) {

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(
				"---HttpTaskImp{settings:[connectTimeOut(" + connectTimeOut
						+ "),ReadTimeOut(" + readTimeOut + "),CacheMode("
						+ mCacheModel + "),retrtyCount("
						+ policy.getCurrentRetryCount() + ")]__<url:[" + url
						+ "]>").append(",");
		builder.append("<method:[" + method + "]>").append(",");
		builder.append("<headers:[" + getHeaders().toString() + "]>").append(
				",");
		if (mDataSet != null) {
			builder.append("<data:[" + mDataSet.toString() + "]>}");
		}

		return builder.toString();
	}

}

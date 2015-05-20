package com.developer.bsince.response;

import java.io.InputStream;
import java.net.HttpURLConnection;


import com.developer.bsince.core.Headers;

public class NetworkResponse {

    /**
     * 封装的网络请求响应实体
     * @param statusCode 
     * @param data 
     * @param headers 
     * @param notModified 返回true,则表示数据在高速缓存中，并由服务端返回了304
     */
    public NetworkResponse(int statusCode, byte [] data, Headers headers,
            boolean notModified) {
        this.statusCode = statusCode;
        this.data = data;
        this.ioData = null;
        this.headers = headers==null?Headers.EMPTY_HEADERS:headers;
        this.notModified = notModified;
    }
    
    public NetworkResponse(int statusCode,InputStream ioData, Headers headers,
            boolean notModified) {
        this.statusCode = statusCode;
        this.data = null;
        this.ioData = ioData;
        this.headers = headers==null?Headers.EMPTY_HEADERS:headers;
        this.notModified = notModified;
        
    }


    public NetworkResponse(byte [] data) {

        this.statusCode = HttpURLConnection.HTTP_OK;
        this.data = data;
        this.ioData = null;
        this.headers = Headers.EMPTY_HEADERS;
        this.notModified = false;

    }

    public NetworkResponse(byte [] data, Headers  headers) {
        this(HttpURLConnection.HTTP_OK, data, headers, false);
    }

    
    public final int statusCode;

    
    public final byte [] data;
    
    public final InputStream ioData;

    public  int contentLength;
    
    public final Headers headers;

    
    public final boolean notModified;

}

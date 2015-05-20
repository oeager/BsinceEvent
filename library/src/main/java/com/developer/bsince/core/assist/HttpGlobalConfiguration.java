package com.developer.bsince.core.assist;

import java.io.File;
import java.net.ContentHandlerFactory;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import android.content.Context;

import com.developer.bsince.core.Authenticator;
import com.developer.bsince.core.Headers;
import com.developer.bsince.request.DefaultRetryPolicy;
import com.developer.bsince.request.RetryPolicy;
import com.developer.bsince.request.UrlRewriter;


public class HttpGlobalConfiguration {

	/**
	 * int param 
	 */
    public final int readTimeOut;

    public final int bufferSize;

    public final int retryCount;

    public final int connectTimeOut;

    public final int threadSize;
    
    public final int cacheSize;

    /**
     *  boolean param
     */

    public final boolean useCookie;

    public final boolean keepAlive;

    public static  boolean debug;

    public final boolean isProxy;

    /**
     * String param
     */

    public final String userAgent;

    public final String endPoint;


    /**
     * other param
     */

    public final UrlRewriter mUrlRewriter;
    
    public final Proxy proxy;

    public final File netCacheDir;

    public final List<String> cookieStrs;
    
    public final Authenticator authenticator;
    
    public final Headers.Builder defaultRequestProperty;

    protected HttpGlobalConfiguration(Builder builder) {
        readTimeOut = builder.readTimeOut;
        bufferSize = builder.bufferSize;
        retryCount = builder.retryCount;
        connectTimeOut = builder.connectTimeOut;
        threadSize = builder.threadSize;
        cacheSize = builder.cacheSize;
        useCookie = builder.useCookie;
        keepAlive = builder.useCookie;
        debug = builder.debug;
        isProxy = builder.isProxy;
        userAgent = builder.userAgent;
        endPoint = builder.endPoint;
        defaultRequestProperty =builder.headersBuild;
        proxy = builder.proxy;
        authenticator = builder.authenticator;
        mUrlRewriter = builder.mUrlRewriter;
        netCacheDir = builder.netCacheDir;
        cookieStrs = builder.cookieStrs;
        if(builder.cookieHandler!=null){
        	CookieHandler.setDefault(builder.cookieHandler);
        }
    }
    
    public static class Builder {

        int readTimeOut;

        int bufferSize;

        int retryCount = 0;

        int connectTimeOut;
        
        int cacheSize;

        int threadSize;

        int engineType;

        // boolean param

        boolean useCookie = false;

        boolean keepAlive = true;

        boolean debug = false;

        private boolean isProxy = false;

        //String param

        String userAgent;

        String endPoint=null;

        //other param

        Proxy proxy=null;

        RetryPolicy retryPolicy;
        
        Authenticator authenticator;

        UrlRewriter mUrlRewriter = null;

        Headers.Builder headersBuild;

        CookieHandler cookieHandler;
        
        File netCacheDir;

        private List<String> cookieStrs;


        public Builder readTimeOut(int readTimeOut) {
            this.readTimeOut = readTimeOut;
            return this;
        }

        public Builder bufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder retryCount(int retryCount){
            this.retryCount = retryCount;
            return  this;
        }

        public Builder connectTimeOut(int connectTimeOut){
            this.connectTimeOut = connectTimeOut;
            return this;
        }

        public Builder threadSize(int threadSize){
            this.threadSize = threadSize;
            return  this;
        }

        public Builder engineType(EngineType type){
            this.engineType = type.getEnginValue();
            return  this;
        }


        public Builder useCookie(boolean useCookie) {
            this.useCookie = useCookie;
            cookieStrs = new LinkedList<String>();
            return this;
        }

        public Builder keepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder endPoint(String commonUrl) {
            this.endPoint = commonUrl;
            return this;
        }
        
        public Builder cookieHandler(CookieHandler handler){
        	this.cookieHandler = handler;
        	this.useCookie = true;
        	return this;
        }

     
        public synchronized Builder addDefaultRequestProperty(String propertyName,String propertyValue) {
        	if(headersBuild==null){
        		headersBuild =  new Headers.Builder();
        	}
        	headersBuild.add(propertyName,propertyValue);
            return this;
        }

        public Builder proxy(Proxy proxy) {
            if(proxy!=null){
                this.proxy = proxy;
                isProxy = true;
            }

            return this;
        }

        public Builder retryPolicy(RetryPolicy policy) {
            this.retryPolicy = policy;
            return this;
        }
        
        public Builder authenticator(Authenticator authenticator){
        	this.authenticator = authenticator;
        	return this;
        }
        
        public Builder urlRewriter(UrlRewriter rewriter){
            this.mUrlRewriter = rewriter;
            return this;
        }

        public Builder defaultSSLSocketFactory(SSLSocketFactory factory){
            HttpsURLConnection.setDefaultSSLSocketFactory(factory);
            return this;
        }
        
        public Builder defaultHostnameVerifier(HostnameVerifier verifier){
        	HttpsURLConnection.setDefaultHostnameVerifier(verifier);
        	return this;
        }
        
        public Builder defaultContentHandlerFactory(ContentHandlerFactory factory){
        	URLConnection.setContentHandlerFactory(factory);
        	return this;
        }
        
        public Builder defaultAllowRedirect(boolean allow){
        	HttpURLConnection.setFollowRedirects(allow);
        	return this;
        }

        public Builder netCacheDir(File netCacheDir){

            if(netCacheDir.isDirectory()){
                this.netCacheDir = netCacheDir;
            }
            return this;
        }




        public HttpGlobalConfiguration build(Context cx) {
            checkParams(cx);
            return new HttpGlobalConfiguration(this);
        }

        ;


        protected void checkParams(Context context) {
            if(connectTimeOut<=0){
                connectTimeOut = HttpConstants.DEFAULT_TIMEOUT_MS;
            }

            if (readTimeOut <= 0) {
                readTimeOut = HttpConstants.DEFAULT_TIMEOUT_MS;
            }
            if (bufferSize <= 0) {
                bufferSize = HttpConstants.DEFAULT_SOCKET_BUFFER_SIZE;
            }
            if(threadSize<=0){
                threadSize =HttpConstants.DEFAULT_NETWORK_THREAD_POOL_SIZE;
            }
            
            if(cacheSize<=0){
            	cacheSize = HttpConstants.DEFAULT_DISK_USAGE_BYTES;
            }

            if (userAgent == null) {
                userAgent = "BsinceManager_net_moudle_client";
            }
            if (retryPolicy == null) {
                retryPolicy = new DefaultRetryPolicy();
            }
            if(netCacheDir==null){
                netCacheDir =new File(context.getCacheDir(),"Bsince");
                
            }
            if(authenticator==null){
            	authenticator=BasicAuthenticatorImp.INSTANCE;
            }
            
            if(useCookie){
            	if(cookieHandler==null){
            		cookieHandler = new CookieManager();
            	}
            }

        }


    }
}

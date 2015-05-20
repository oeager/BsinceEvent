package com.developer.bsince.event;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;

import javax.net.ssl.SSLSocketFactory;

import android.graphics.Bitmap.Config;
import android.text.TextUtils;
import android.util.Log;

import com.developer.bsince.cache.DiskBasedCache;
import com.developer.bsince.cache.HttpCache;
import com.developer.bsince.core.Authenticator;
import com.developer.bsince.core.BaseNetworker;
import com.developer.bsince.core.BsinceEngineImp;
import com.developer.bsince.core.Headers;
import com.developer.bsince.core.IEngine;
import com.developer.bsince.core.IWork;
import com.developer.bsince.core.assist.AuthorizationHelper;
import com.developer.bsince.core.assist.HttpConstants;
import com.developer.bsince.core.assist.HttpGlobalConfiguration;
import com.developer.bsince.data.IDataSet;
import com.developer.bsince.exceptions.ConfigurationNullpointException;
import com.developer.bsince.parser.IParser;
import com.developer.bsince.parser.TextParserImp;
import com.developer.bsince.request.CacheModel;
import com.developer.bsince.request.CacheModel.CacheEnum;
import com.developer.bsince.request.DefaultRetryPolicy;
import com.developer.bsince.request.Method;
import com.developer.bsince.request.Method.HttpMethod;
import com.developer.bsince.request.Priority;
import com.developer.bsince.request.Priority.Sequence;
import com.developer.bsince.request.ProxyInfo;
import com.developer.bsince.request.RetryPolicy;
import com.developer.bsince.response.ResponseListener;

public class EventPublisher {

    private static final String TAG = "BsinceEvent";

    private static volatile EventPublisher INSTANCE;

    private static HttpGlobalConfiguration globalConfig;

    private final IEngine mEngine;

    private final IWork mNetwork;

    private final HttpCache cache;

    private EventPublisher() {
        checkConfiguration();
        cache = new DiskBasedCache(globalConfig.netCacheDir,
                globalConfig.cacheSize);

        mNetwork = new BaseNetworker();
        mEngine = new BsinceEngineImp(cache, globalConfig.threadSize);
    }

    private static EventPublisher getSingleInstance() {

        if (INSTANCE == null) {
            synchronized (EventPublisher.class) {
                if (INSTANCE == null) {
                    INSTANCE = new EventPublisher();
                }
            }
        }
        return INSTANCE;
    }

    public static void submit(Event<?> task) {
        getSingleInstance().mEngine.submit(task);

    }

    public static <T> T execute(Event<T> event) {
        return getSingleInstance().mEngine.execute(event);
    }

    public static void init(HttpGlobalConfiguration config) {
        if (globalConfig != null) {
            throw new IllegalArgumentException(
                    "the globalConfig had inited,don't initialization once more");
        }
        globalConfig = config;
    }

    public static void cancel(int serialNum) {
        getSingleInstance().mEngine.cancel(serialNum);
    }

    public static void cancel(Object tag) {
        getSingleInstance().mEngine.cancel(tag);
    }

    public static void cancel(EventFilter filter) {
        getSingleInstance().mEngine.cancel(filter);
    }

    public static void cancelAll() {
        getSingleInstance().mEngine.cancelAll();
    }

    public static void release() {
        getSingleInstance().mEngine.shutdown();
    }

    public static void checkConfiguration() {
        if (globalConfig == null) {
            throw new ConfigurationNullpointException(
                    "HttpGlobalConfiguration must init before used ");
        }
    }

    public static <Result> Builder<Result> connect(Class<Result> cls, String path) {
        checkConfiguration();
        return new Builder<Result>(path);
    }
	public static void clearHttpCache(Runnable callback){
		ClearCacheEvent event = new ClearCacheEvent(getSingleInstance().cache,callback);
		submit(event);
	}

    public static CallableEvent.Builder task() {
        checkConfiguration();
        return new CallableEvent.Builder();
    }

    public static String urlReWriter(String url) {

        StringBuilder builder = new StringBuilder();
        if (TextUtils.isEmpty(url)) {
            if (TextUtils.isEmpty(globalConfig.endPoint)) {
                throw new NullPointerException("endponit["
                        + globalConfig.endPoint
                        + "]is null,and set the path/url [" + url
                        + "]is also null");
            }
            builder.append(globalConfig.endPoint);
        } else {
            if (url.startsWith("http")) {
                builder.append(url);
            } else if (url.startsWith(File.separator)) {
                if (TextUtils.isEmpty(globalConfig.endPoint)) {
                    throw new NullPointerException("endponit["
                            + globalConfig.endPoint
                            + "]is null,and set the path/url [" + url
                            + "]is not start with http");
                }
                builder.append(globalConfig.endPoint);
                builder.append(url);
            } else {
                throw new IllegalArgumentException(
                        "unrecognized Url["
                                + url
                                + "],<if you want to set the whole url,the url should start with 'http',if you just want to set the path,the path should start with '/'>");
            }
        }

        return builder.toString();
    }

    public static class Builder<Result> {


        int connectTimeOut;

        int readTimeOut;

        int bufferSize;

        boolean keepAlive;

        boolean allowRedirect = false;

        String url;

        String userAgent;

        Headers.Builder mHeaders;

        Object mTag;

        @HttpMethod
        String method = Method.GET;

        Proxy mProxy;

        IDataSet mDataSet;

        IParser<Result> mParser;

        @Sequence
        int priority = Priority.NORMAL;

        RetryPolicy retryPolicy;

        int mCacheModel;

        SSLSocketFactory mSocketFactory;

        Authenticator authenticator;

        Object[] extraData;

        protected Builder(String url) {
            mHeaders = new Headers.Builder();
            this.url = url;
            this.keepAlive = globalConfig.keepAlive;
        }

        public Builder<Result> connectTimeOut(int timeOut) {
            this.connectTimeOut = timeOut;

            return this;
        }

        public Builder<Result> readTimeOut(int timeOut) {
            this.readTimeOut = timeOut;

            return this;
        }

        public Builder<Result> bufferSize(int buffersize) {
            this.bufferSize = buffersize;

            return this;
        }

        public Builder<Result> keepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public Builder<Result> redirect(boolean allow) {
            this.allowRedirect = allow;
            return this;
        }

        public Builder<Result> userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        @Deprecated
        public Builder<Result> authorization(String authCode) {

            if (!TextUtils.isEmpty(authCode)) {
                setHeader(HttpConstants.HEADER_AUTHORIZATION, authCode);
            }
            return this;

        }

        @Deprecated
        public Builder<Result> proxyAuthorization(String proxyAuth) {
            if (!TextUtils.isEmpty(proxyAuth)) {
                setHeader(HttpConstants.HEADER_PROXY_AUTHORIZATION, proxyAuth);
                if (HttpGlobalConfiguration.debug) {
                    Log.w(TAG,
                            "the proxyAuthorization set by proxyInfo will be replaced");
                }
            }

            return this;
        }

        public Builder<Result> authenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public Builder<Result> headers(Headers headers) {
            if (headers != null) {
                mHeaders = headers.newBuilder();
                if (HttpGlobalConfiguration.debug) {
                    Log.d(TAG, "the header had be replaced");
                }
            }
            return this;
        }

        public Builder<Result> addHeader(String line) {

            mHeaders.add(line);
            return this;
        }

        public Builder<Result> addHeader(String key, String value) {

            mHeaders.add(key, value);
            return this;
        }

        public Builder<Result> setHeader(String key, String value) {
            mHeaders.set(key, value);
            return this;
        }

        public Builder<Result> tag(Object mTag) {
            this.mTag = mTag;
            return this;
        }


        public Builder<Result> method(@HttpMethod String method) {
            this.method = method;
            return this;
        }


        public Builder<Result> parser(IParser<Result> mparser) {
            this.mParser = mparser;
            return this;
        }

        public Builder<Result> data(IDataSet mDataSet) {
            this.mDataSet = mDataSet;
            return this;
        }

        public Builder<Result> priority(@Sequence int priority) {
            this.priority = priority;
            return this;
        }

        public Builder<Result> proxy(ProxyInfo proxy) {
            this.mProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                    proxy.proxyHost, proxy.proxyPort));
            if (HttpGlobalConfiguration.debug) {
                Log.w(TAG, "you had open the proxy ");
            }
            if (!TextUtils.isEmpty(proxy.userName)
                    && !TextUtils.isEmpty(proxy.password)) {
                setHeader(HttpConstants.HEADER_PROXY_AUTHORIZATION,
                        AuthorizationHelper.BASIC(proxy.userName,
                                proxy.password));
                if (HttpGlobalConfiguration.debug) {
                    Log.w(TAG, "the proxyAuthorization may be reseted ");
                }
            }
            return this;
        }

        public Builder<Result> proxy(Proxy proxy) {
            this.mProxy = proxy;
            if (HttpGlobalConfiguration.debug) {
                Log.w(TAG, "you had open the proxy ");
            }
            return this;
        }

        public Builder<Result> retry(int maxNumRetries) {
            this.retryPolicy = new DefaultRetryPolicy(
                    HttpConstants.DEFAULT_TIMEOUT_MS, maxNumRetries,
                    HttpConstants.DEFAULT_BACKOFF_MULT);
            return this;
        }

        public Builder<Result> retry(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        public Builder<Result> cacheMode(@CacheEnum int mCacheModel) {
            this.mCacheModel = mCacheModel;
            return this;
        }

        public Builder<Result> sslSocketFactory(SSLSocketFactory factory) {
            this.mSocketFactory = factory;
            return this;
        }

        public Builder<Result> bitmapExtras(int maxWidth, int maxHeight, Config config) {
            this.extraData = new Object[]{maxWidth, maxHeight, config};
            if (HttpGlobalConfiguration.debug) {
                Log.w(TAG, "the extra data you set before will be replaced");
            }
            return this;
        }


        public Builder<Result> noProxy() {
            mProxy = Proxy.NO_PROXY;
            if (HttpGlobalConfiguration.debug) {
                Log.w(TAG,
                        "the proxy you set before all set in the globalconfig will all be unuseful ");
            }
            return this;
        }

        public Builder<Result> acceptGzip() {
            addHeader(HttpConstants.HEADER_ACCEPT_ENCODING,
                    HttpConstants.ENCODING_GZIP);
            return this;
        }

        public HttpEventImp<Result> submit(ResponseListener<Result> listener) {
            checkTaskInfos();
            HttpEventImp<Result> event = new HttpEventImp<Result>(this, getSingleInstance().mNetwork, getSingleInstance().cache, listener);
            event.setTag(mTag);
            event.setPriority(priority);
            getSingleInstance().mEngine.submit(event);
            return event;

        }

        ;

        public Result submit() {
            checkTaskInfos();
            HttpEventImp<Result> event = new HttpEventImp<Result>(this, getSingleInstance().mNetwork, getSingleInstance().cache, null);
            event.setTag(mTag);
            event.setPriority(priority);
            return getSingleInstance().mEngine.execute(event);
        }

        ;


        @SuppressWarnings("unchecked")
        private void checkTaskInfos() {
            checkConfiguration();
            if (method == null) {
                method = Method.GET;
            }
            url = urlReWriter(url);

            if (mDataSet != null) {
                if (!Method.permitsRequestBody(method)) {
                    url += "?";
                    url += mDataSet.toString();
                }

            }
            if (globalConfig.mUrlRewriter != null) {
                url = globalConfig.mUrlRewriter.rewriteUrl(url);
            }

            if (connectTimeOut <= 0) {
                connectTimeOut = globalConfig.connectTimeOut;
            }
            if (readTimeOut <= 0) {
                readTimeOut = globalConfig.readTimeOut;
            }
            if (bufferSize < 1024) {
                bufferSize = globalConfig.bufferSize;
            }
            if (TextUtils.isEmpty(userAgent)) {
                userAgent = globalConfig.userAgent;
            }

            if (retryPolicy == null) {
                retryPolicy = new DefaultRetryPolicy(
                        globalConfig.connectTimeOut, globalConfig.retryCount,
                        HttpConstants.DEFAULT_BACKOFF_MULT);
            }
            if (priority < Priority.LOW || priority > Priority.IMMEDIATE) {
                priority = Priority.NORMAL;
            }

            if (authenticator == null) {
                authenticator = globalConfig.authenticator;
            }

            if (mCacheModel <= 0) {

                if (Method.invalidatesCache(method)) {
                    mCacheModel = CacheModel.CACHE_NEVER;
                } else {
                    mCacheModel = CacheModel.CACHE_BY_SERVER;
                }

            }
            if (mParser == null) {
                mParser = (IParser<Result>) TextParserImp.INSTANCE;
            }
            Headers.Builder builder = globalConfig.defaultRequestProperty;
            if (builder != null) {
                mHeaders.namesAndValues().addAll(builder.namesAndValues());
            }
            mHeaders.set(HttpConstants.HEADER_USER_AGENT, userAgent);
            mHeaders.set(HttpConstants.HEADER_CONNECTION,
                    keepAlive ? HttpConstants.CONNECTION_KEEP_ALIVE
                            : HttpConstants.CONNECTION_CLOSE);
        }
    }
}

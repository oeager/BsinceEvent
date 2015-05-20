package com.developer.bsince.event.example;

import android.app.Application;
import android.os.Environment;

import com.developer.bsince.core.assist.EngineType;
import com.developer.bsince.core.assist.HttpGlobalConfiguration;
import com.developer.bsince.event.EventPublisher;

import java.io.File;

/**
 * Created by oeager on 2015/4/22.
 */
public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HttpGlobalConfiguration globalConfiguration
                = new HttpGlobalConfiguration.Builder()
                .connectTimeOut(5000)
                .readTimeOut(5000)
                .bufferSize(2048)
                .debug(true)
                .endPoint("http://192.168.1.109:8080")
                .userAgent("Bsince/test/engine")
                .useCookie(true)
                .engineType(EngineType.Engine_Bsince)
                .threadSize(3)
                .defaultAllowRedirect(false)
                .keepAlive(true)
//              .proxy(...)
//              .urlRewriter(...)
//              .defaultSSLSocketFactory(...)
//              .defaultHostnameVerifier(...)
//              .defaultContentHandlerFactory(...)
//              .authenticator(...)
//              .cookieHandler(...)
//              .netCacheDir(...)
                //more you want ...
                .retryCount(2)
                .addDefaultRequestProperty("customHeader", "customerHeaderValue")
                .build(this);
        EventPublisher.init(globalConfiguration);
    }
}

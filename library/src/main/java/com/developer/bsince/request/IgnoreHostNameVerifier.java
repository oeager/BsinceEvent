package com.developer.bsince.request;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by oeager on 2015/6/1.
 * email: oeager@foxmail.com
 */
public class IgnoreHostNameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}

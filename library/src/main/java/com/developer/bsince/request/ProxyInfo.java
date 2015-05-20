package com.developer.bsince.request;

/**
 * Created by oeager on 2015/1/23.
 */
public class ProxyInfo{

    public final String userName;
    public final String password;
    public final String proxyHost;
    public final int proxyPort;
    public ProxyInfo (String proxyHost,int proxyPort){
        this(null,null,proxyHost,proxyPort);
    }
    public ProxyInfo(String userName,String password,String proxyHost,int proxyPort ){
        this.userName = userName;
        this.password = password;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public boolean needAuthorize(){
        return !(userName==null||password==null);
    }
}
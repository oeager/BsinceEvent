package com.developer.bsince.cache;

import com.developer.bsince.core.Headers;

public interface HttpCache  {
	
	public  Entry get(String k) ;

	public  void put(String k, Entry v) ;

	public void remove(String k);

	public void clear();

	public void initialize();

    public void invalidate(String key, boolean fullExpire);
	

	
	public static class Entry{
        /**
         * 从缓存中返回的字节数组
         */
        public byte [] data;

        /**
         * etag标记
         */
        public String etag;

        /**服务端响应日期 */
        public long serverDate;

        //文件最后修改时间
        public long lastModifiedTime;

        /** 该记录的ttl */
        public long ttl;

        /** Soft TTL for this record. */
        public long softTtl;

        /** 接收来自服务器的不可改变的响应头；必须是非空的。 */
        public Headers responseHeaders = Headers.EMPTY_HEADERS;

        public boolean isExpired() {
            return this.ttl < System.currentTimeMillis();
        }

        public boolean refreshNeeded() {
            return this.softTtl < System.currentTimeMillis();
        }
    }

	
	

}

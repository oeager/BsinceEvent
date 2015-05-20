

package com.developer.bsince.request;

/**
 * 重试机制
 */
public interface RetryPolicy {

    /**
     * 返回当前超时时间
     */
    public int getCurrentTimeout();

    /**
     * 返回当前重试次数
     */
    public int getCurrentRetryCount();

    /**
     * 重尝试
     */
    public void retry(Exception error) throws Exception;
}

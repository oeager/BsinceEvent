

package com.developer.bsince.request;

import com.developer.bsince.core.assist.HttpConstants;

/**
 * 网络请求默认的重试机制
 */
public class DefaultRetryPolicy implements RetryPolicy {
    /**
     * 当前超时毫秒数
     */
    private int mCurrentTimeoutMs;

    /**
     * 当前已重试次数.
     */
    private int mCurrentRetryCount;

    /**
     * 最大重试次数.
     */
    private final int mMaxNumRetries;

    /**
     * 用于求积的浮点数
     */
    private final float mBackoffMultiplier;


    public DefaultRetryPolicy() {
        this(HttpConstants.DEFAULT_TIMEOUT_MS, HttpConstants.DEFAULT_MAX_RETRIES, HttpConstants.DEFAULT_BACKOFF_MULT);
    }

    public DefaultRetryPolicy(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier) {
        mCurrentTimeoutMs = initialTimeoutMs;
        mMaxNumRetries = maxNumRetries;
        mBackoffMultiplier = backoffMultiplier;
    }


    @Override
    public int getCurrentTimeout() {
        return mCurrentTimeoutMs;
    }


    @Override
    public int getCurrentRetryCount() {
        return mCurrentRetryCount;
    }

    /**
     * Prepares for the next retry by applying a backoff to the timeout.
     *
     * @param error The error code of the last attempt.
     */
    @Override
    public void retry(Exception error) throws Exception {
        mCurrentRetryCount++;
        mCurrentTimeoutMs += (mCurrentTimeoutMs * mBackoffMultiplier);
        if (!hasAttemptRemaining()) {
            throw error;
        }
    }

    /**
     * 判断是否已超过重试次数
     *
     * @return
     */
    protected boolean hasAttemptRemaining() {
        return mCurrentRetryCount <= mMaxNumRetries;
    }
}

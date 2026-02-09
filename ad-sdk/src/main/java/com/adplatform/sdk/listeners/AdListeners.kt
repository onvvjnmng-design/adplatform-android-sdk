package com.adplatform.sdk.listeners

import com.adplatform.sdk.models.Ad

/**
 * Listener for ad events
 */
interface AdListener {
    /**
     * Called when ad is successfully loaded
     */
    fun onAdLoaded()
    
    /**
     * Called when ad fails to load
     * @param error Error message
     */
    fun onAdFailed(error: String)
    
    /**
     * Called when ad is clicked
     */
    fun onAdClicked()
    
    /**
     * Called when ad is displayed
     */
    fun onAdShown() {}
    
    /**
     * Called when ad is closed
     */
    fun onAdClosed() {}
}

/**
 * Listener for rewarded ad events
 */
interface RewardedAdListener : AdListener {
    /**
     * Called when user earns reward
     * @param amount Reward amount
     * @param type Reward type (e.g., "coins", "points")
     */
    fun onUserEarnedReward(amount: Int, type: String)
}

/**
 * Listener for native ad events
 */
interface NativeAdListener {
    /**
     * Called when native ad is loaded
     * @param ad The loaded native ad
     */
    fun onNativeAdLoaded(ad: Ad)
    
    /**
     * Called when native ad fails to load
     * @param error Error message
     */
    fun onAdFailed(error: String)
}

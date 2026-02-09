package com.adplatform.sdk.native

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.adplatform.sdk.AdPlatform
import com.adplatform.sdk.listeners.NativeAdListener
import com.adplatform.sdk.models.Ad
import com.adplatform.sdk.models.DeviceInfo
import kotlinx.coroutines.*

/**
 * Native Ad Loader
 * 
 * Native ads give you full control over how ads are displayed.
 * 
 * Usage:
 * ```kotlin
 * val nativeAdLoader = NativeAdLoader(activity)
 * 
 * nativeAdLoader.loadAd(object : NativeAdListener {
 *     override fun onNativeAdLoaded(ad: Ad) {
 *         // Display ad in your custom layout
 *         titleTextView.text = ad.title
 *         descriptionTextView.text = ad.description
 *         Glide.with(this).load(ad.imageUrl).into(imageView)
 *         ctaButton.text = ad.callToAction ?: "Learn More"
 *         
 *         // Track impression
 *         nativeAdLoader.recordImpression(ad)
 *         
 *         // Handle click
 *         ctaButton.setOnClickListener {
 *             nativeAdLoader.handleClick(ad)
 *         }
 *     }
 *     
 *     override fun onAdFailed(error: String) {
 *         // Handle error
 *     }
 * })
 * ```
 */
class NativeAdLoader(private val activity: Activity) {
    
    companion object {
        private const val TAG = "NativeAdLoader"
    }
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isLoading = false
    
    /**
     * Load a native ad
     * @param listener Callback for ad events
     */
    fun loadAd(listener: NativeAdListener) {
        AdPlatform.ensureInitialized()
        
        if (isLoading) {
            Log.w(TAG, "Ad is already loading")
            return
        }
        
        isLoading = true
        
        scope.launch {
            try {
                val deviceInfo = getDeviceInfo()
                val result = AdPlatform.apiClient?.requestAd("native", deviceInfo)
                
                result?.fold(
                    onSuccess = { ad ->
                        isLoading = false
                        listener.onNativeAdLoaded(ad)
                    },
                    onFailure = { error ->
                        isLoading = false
                        listener.onAdFailed(error.message ?: "Unknown error")
                    }
                ) ?: run {
                    isLoading = false
                    listener.onAdFailed("SDK not initialized")
                }
            } catch (e: Exception) {
                isLoading = false
                listener.onAdFailed(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Record that the ad was displayed (impression)
     * Note: Impression is already tracked when ad is loaded, this is optional
     */
    fun recordImpression(ad: Ad) {
        // Impression is tracked server-side when ad is requested
        Log.d(TAG, "Impression recorded for ad ${ad.id}")
    }
    
    /**
     * Handle ad click - tracks the click and opens the target URL
     * @param ad The ad that was clicked
     */
    fun handleClick(ad: Ad) {
        // Track click
        scope.launch {
            ad.impressionId?.let { impressionId ->
                AdPlatform.apiClient?.trackClick(ad.id, impressionId)
            }
        }
        
        // Open target URL
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ad.targetUrl))
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open URL: ${e.message}")
        }
    }
    
    private fun getDeviceInfo(): DeviceInfo {
        val displayMetrics = activity.resources.displayMetrics
        return DeviceInfo(
            screenWidth = displayMetrics.widthPixels,
            screenHeight = displayMetrics.heightPixels
        )
    }
    
    /**
     * Cancel any pending operations
     */
    fun cancel() {
        scope.cancel()
        isLoading = false
    }
}

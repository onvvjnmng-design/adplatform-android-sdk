package com.AdNova.sdk.banner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import coil.load
import com.AdNova.sdk.AdNova
import com.AdNova.sdk.R
import com.AdNova.sdk.listeners.AdListener
import com.AdNova.sdk.models.Ad
import com.AdNova.sdk.models.DeviceInfo
import kotlinx.coroutines.*

/**
 * Banner Ad View
 * 
 * Add to your layout:
 * ```xml
 * <com.AdNova.sdk.banner.BannerAdView
 *     android:id="@+id/bannerAd"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" />
 * ```
 * 
 * Then in your Activity/Fragment:
 * ```kotlin
 * val bannerAd = findViewById<BannerAdView>(R.id.bannerAd)
 * bannerAd.loadAd()
 * ```
 */
class BannerAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    companion object {
        private const val TAG = "BannerAdView"
    }
    
    private var adListener: AdListener? = null
    private var currentAd: Ad? = null
    private var isLoading = false
    private var autoRefresh = true
    private var refreshIntervalMs = 60_000L // 60 seconds
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var refreshJob: Job? = null
    
    private var adContainer: View? = null
    private var adImageView: ImageView? = null
    private var adTitleView: TextView? = null
    private var adLabelView: TextView? = null
    
    init {
        inflateLayout()
        setupClickListener()
    }
    
    private fun inflateLayout() {
        adContainer = LayoutInflater.from(context).inflate(R.layout.banner_ad_view, this, false)
        addView(adContainer)
        
        adImageView = adContainer?.findViewById(R.id.adImage)
        adTitleView = adContainer?.findViewById(R.id.adTitle)
        adLabelView = adContainer?.findViewById(R.id.adLabel)
        
        visibility = View.GONE
    }
    
    private fun setupClickListener() {
        adContainer?.setOnClickListener {
            handleAdClick()
        }
    }
    
    /**
     * Set listener for ad events
     */
    fun setAdListener(listener: AdListener) {
        this.adListener = listener
    }
    
    /**
     * Enable/disable auto refresh
     * @param enabled Whether to auto refresh
     * @param intervalMs Refresh interval in milliseconds (default: 60 seconds)
     */
    fun setAutoRefresh(enabled: Boolean, intervalMs: Long = 60_000L) {
        autoRefresh = enabled
        refreshIntervalMs = intervalMs
        
        if (!enabled) {
            refreshJob?.cancel()
        }
    }
    
    /**
     * Load a banner ad
     */
    fun loadAd() {
        AdNova.ensureInitialized()
        
        if (isLoading) {
            Log.w(TAG, "Ad is already loading")
            return
        }
        
        isLoading = true
        
        scope.launch {
            try {
                val deviceInfo = getDeviceInfo()
                val result = AdNova.apiClient?.requestAd("banner", deviceInfo)
                
                result?.fold(
                    onSuccess = { ad ->
                        displayAd(ad)
                        isLoading = false
                        adListener?.onAdLoaded()
                        startAutoRefresh()
                    },
                    onFailure = { error ->
                        isLoading = false
                        visibility = View.GONE
                        adListener?.onAdFailed(error.message ?: "Unknown error")
                    }
                ) ?: run {
                    isLoading = false
                    adListener?.onAdFailed("SDK not initialized")
                }
            } catch (e: Exception) {
                isLoading = false
                adListener?.onAdFailed(e.message ?: "Unknown error")
            }
        }
    }
    
    private fun displayAd(ad: Ad) {
        currentAd = ad
        
        adImageView?.load(ad.imageUrl) {
            crossfade(true)
            error(android.R.color.darker_gray)
        }
        
        adTitleView?.text = ad.title
        visibility = View.VISIBLE
        
        adListener?.onAdShown()
    }
    
    private fun handleAdClick() {
        val ad = currentAd ?: return
        
        adListener?.onAdClicked()
        
        // Track click
        scope.launch {
            ad.impressionId?.let { impressionId ->
                AdNova.apiClient?.trackClick(ad.id, impressionId)
            }
        }
        
        // Open target URL
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ad.targetUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open URL: ${e.message}")
        }
    }
    
    private fun startAutoRefresh() {
        if (!autoRefresh) return
        
        refreshJob?.cancel()
        refreshJob = scope.launch {
            delay(refreshIntervalMs)
            loadAd()
        }
    }
    
    private fun getDeviceInfo(): DeviceInfo {
        val displayMetrics = context.resources.displayMetrics
        return DeviceInfo(
            screenWidth = displayMetrics.widthPixels,
            screenHeight = displayMetrics.heightPixels
        )
    }
    
    /**
     * Destroy the ad and release resources
     */
    fun destroy() {
        refreshJob?.cancel()
        scope.cancel()
        currentAd = null
        visibility = View.GONE
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroy()
    }
}


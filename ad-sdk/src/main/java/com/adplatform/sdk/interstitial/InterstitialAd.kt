package com.AdNova.sdk.interstitial

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
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
 * Interstitial (Full-screen) Ad
 * 
 * Usage:
 * ```kotlin
 * val interstitialAd = InterstitialAd(activity)
 * interstitialAd.loadAd()
 * 
 * interstitialAd.setAdListener(object : AdListener {
 *     override fun onAdLoaded() {
 *         // Ad is ready to show
 *     }
 *     override fun onAdFailed(error: String) {
 *         // Handle error
 *     }
 *     override fun onAdClicked() {}
 *     override fun onAdClosed() {
 *         // Load next ad
 *         interstitialAd.loadAd()
 *     }
 * })
 * 
 * // Show when ready
 * if (interstitialAd.isLoaded) {
 *     interstitialAd.show()
 * }
 * ```
 */
class InterstitialAd(private val activity: Activity) {
    
    companion object {
        private const val TAG = "InterstitialAd"
    }
    
    private var adListener: AdListener? = null
    private var currentAd: Ad? = null
    private var isLoading = false
    
    var isLoaded: Boolean = false
        private set
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var dialog: Dialog? = null
    
    /**
     * Set listener for ad events
     */
    fun setAdListener(listener: AdListener) {
        this.adListener = listener
    }
    
    /**
     * Load an interstitial ad
     */
    fun loadAd() {
        AdNova.ensureInitialized()
        
        if (isLoading) {
            Log.w(TAG, "Ad is already loading")
            return
        }
        
        isLoading = true
        isLoaded = false
        
        scope.launch {
            try {
                val deviceInfo = getDeviceInfo()
                val result = AdNova.apiClient?.requestAd("interstitial", deviceInfo)
                
                result?.fold(
                    onSuccess = { ad ->
                        currentAd = ad
                        isLoaded = true
                        isLoading = false
                        adListener?.onAdLoaded()
                    },
                    onFailure = { error ->
                        isLoading = false
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
    
    /**
     * Show the interstitial ad
     */
    fun show() {
        if (!isLoaded || currentAd == null) {
            Log.w(TAG, "Ad not loaded yet")
            return
        }
        
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "Activity is not valid")
            return
        }
        
        showAdDialog()
    }
    
    private fun showAdDialog() {
        val ad = currentAd ?: return
        
        dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.interstitial_ad_dialog)
            
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.parseColor("#E6000000")))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
            }
            
            setCanceledOnTouchOutside(false)
            
            // Setup views
            val imageView = findViewById<ImageView>(R.id.adImage)
            val titleView = findViewById<TextView>(R.id.adTitle)
            val descriptionView = findViewById<TextView>(R.id.adDescription)
            val ctaButton = findViewById<Button>(R.id.ctaButton)
            val closeButton = findViewById<ImageButton>(R.id.closeButton)
            
            imageView.load(ad.imageUrl) {
                crossfade(true)
            }
            
            titleView.text = ad.title
            descriptionView.text = ad.description ?: ""
            descriptionView.visibility = if (ad.description.isNullOrBlank()) View.GONE else View.VISIBLE
            
            ctaButton.text = ad.callToAction ?: "Learn More"
            ctaButton.setOnClickListener {
                handleAdClick(ad)
            }
            
            closeButton.setOnClickListener {
                dismiss()
                isLoaded = false
                currentAd = null
                adListener?.onAdClosed()
            }
            
            setOnDismissListener {
                isLoaded = false
                currentAd = null
            }
            
            show()
            adListener?.onAdShown()
        }
    }
    
    private fun handleAdClick(ad: Ad) {
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
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open URL: ${e.message}")
        }
        
        // Close dialog
        dialog?.dismiss()
        isLoaded = false
        currentAd = null
        adListener?.onAdClosed()
    }
    
    private fun getDeviceInfo(): DeviceInfo {
        val displayMetrics = activity.resources.displayMetrics
        return DeviceInfo(
            screenWidth = displayMetrics.widthPixels,
            screenHeight = displayMetrics.heightPixels
        )
    }
    
    /**
     * Destroy the ad and release resources
     */
    fun destroy() {
        dialog?.dismiss()
        scope.cancel()
        currentAd = null
        isLoaded = false
    }
}


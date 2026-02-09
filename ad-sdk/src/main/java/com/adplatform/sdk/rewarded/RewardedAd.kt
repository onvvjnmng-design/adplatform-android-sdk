package com.adplatform.sdk.rewarded

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import coil.load
import com.adplatform.sdk.AdPlatform
import com.adplatform.sdk.R
import com.adplatform.sdk.listeners.RewardedAdListener
import com.adplatform.sdk.models.Ad
import com.adplatform.sdk.models.DeviceInfo
import kotlinx.coroutines.*

/**
 * Rewarded Ad
 * 
 * Usage:
 * ```kotlin
 * val rewardedAd = RewardedAd(activity)
 * rewardedAd.loadAd()
 * 
 * rewardedAd.setRewardedAdListener(object : RewardedAdListener {
 *     override fun onAdLoaded() {
 *         // Ad is ready
 *     }
 *     override fun onAdFailed(error: String) {
 *         // Handle error
 *     }
 *     override fun onAdClicked() {}
 *     override fun onAdClosed() {
 *         rewardedAd.loadAd()
 *     }
 *     override fun onUserEarnedReward(amount: Int, type: String) {
 *         // Give reward to user
 *         giveReward(amount, type)
 *     }
 * })
 * 
 * // Show when ready
 * if (rewardedAd.isLoaded) {
 *     rewardedAd.show()
 * }
 * ```
 */
class RewardedAd(private val activity: Activity) {
    
    companion object {
        private const val TAG = "RewardedAd"
        private const val WATCH_DURATION_MS = 5000L // 5 seconds to earn reward
        private const val DEFAULT_REWARD_AMOUNT = 10
        private const val DEFAULT_REWARD_TYPE = "coins"
    }
    
    private var adListener: RewardedAdListener? = null
    private var currentAd: Ad? = null
    private var isLoading = false
    private var hasEarnedReward = false
    
    var isLoaded: Boolean = false
        private set
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var dialog: Dialog? = null
    private var countDownTimer: CountDownTimer? = null
    
    // Configurable reward
    var rewardAmount: Int = DEFAULT_REWARD_AMOUNT
    var rewardType: String = DEFAULT_REWARD_TYPE
    
    /**
     * Set listener for rewarded ad events
     */
    fun setRewardedAdListener(listener: RewardedAdListener) {
        this.adListener = listener
    }
    
    /**
     * Load a rewarded ad
     */
    fun loadAd() {
        AdPlatform.ensureInitialized()
        
        if (isLoading) {
            Log.w(TAG, "Ad is already loading")
            return
        }
        
        isLoading = true
        isLoaded = false
        hasEarnedReward = false
        
        scope.launch {
            try {
                val deviceInfo = getDeviceInfo()
                val result = AdPlatform.apiClient?.requestAd("rewarded", deviceInfo)
                
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
     * Show the rewarded ad
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
        
        hasEarnedReward = false
        showAdDialog()
    }
    
    private fun showAdDialog() {
        val ad = currentAd ?: return
        
        dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.rewarded_ad_dialog)
            
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.parseColor("#E6000000")))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
            }
            
            setCanceledOnTouchOutside(false)
            setCancelable(false) // Cannot close until timer finishes
            
            // Setup views
            val imageView = findViewById<ImageView>(R.id.adImage)
            val titleView = findViewById<TextView>(R.id.adTitle)
            val descriptionView = findViewById<TextView>(R.id.adDescription)
            val ctaButton = findViewById<Button>(R.id.ctaButton)
            val closeButton = findViewById<Button>(R.id.closeButton)
            val timerView = findViewById<TextView>(R.id.timerText)
            val progressBar = findViewById<ProgressBar>(R.id.timerProgress)
            val rewardInfoView = findViewById<TextView>(R.id.rewardInfo)
            
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
            
            // Initially hide close button
            closeButton.visibility = View.GONE
            closeButton.setOnClickListener {
                dismiss()
                isLoaded = false
                currentAd = null
                
                if (hasEarnedReward) {
                    adListener?.onUserEarnedReward(rewardAmount, rewardType)
                }
                adListener?.onAdClosed()
            }
            
            // Show reward info
            rewardInfoView.text = "شاهد حتى النهاية لتحصل على $rewardAmount $rewardType"
            
            // Start countdown timer
            progressBar.max = (WATCH_DURATION_MS / 1000).toInt()
            progressBar.progress = progressBar.max
            
            countDownTimer = object : CountDownTimer(WATCH_DURATION_MS, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsLeft = (millisUntilFinished / 1000).toInt() + 1
                    timerView.text = "$secondsLeft"
                    progressBar.progress = secondsLeft
                }
                
                override fun onFinish() {
                    hasEarnedReward = true
                    timerView.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    closeButton.visibility = View.VISIBLE
                    rewardInfoView.text = "🎉 تهانينا! حصلت على $rewardAmount $rewardType"
                }
            }.start()
            
            setOnDismissListener {
                countDownTimer?.cancel()
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
     * Destroy the ad and release resources
     */
    fun destroy() {
        countDownTimer?.cancel()
        dialog?.dismiss()
        scope.cancel()
        currentAd = null
        isLoaded = false
    }
}

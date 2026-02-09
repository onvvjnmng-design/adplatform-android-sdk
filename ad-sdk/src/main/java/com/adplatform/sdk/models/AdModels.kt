package com.AdNova.sdk.models

import com.google.gson.annotations.SerializedName

/**
 * Ad data model
 */
data class Ad(
    @SerializedName("id")
    val id: Any, // Can be Long (production) or String (test ads)
    
    @SerializedName("campaign_id")
    val campaignId: Long? = 0,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("image_url")
    val imageUrl: String,
    
    @SerializedName(value = "click_url", alternate = ["target_url"])
    val targetUrl: String,
    
    @SerializedName(value = "cta_text", alternate = ["call_to_action"])
    val callToAction: String?,
    
    @SerializedName(value = "type", alternate = ["ad_type"])
    val adType: String,
    
    @SerializedName("impression_id")
    val impressionId: String?, // Changed to String to support UUID
    
    @SerializedName("reward_amount")
    val rewardAmount: Int? = null,
    
    @SerializedName("reward_type")
    val rewardType: String? = null,
    
    @SerializedName("is_test")
    val isTest: Boolean? = false
) {
    fun getReward(): Reward? {
        return if (rewardAmount != null && rewardType != null) {
            Reward(rewardAmount, rewardType)
        } else null
    }
}

/**
 * Ad request body
 */
data class AdRequest(
    @SerializedName("sdk_key")
    val sdkKey: String,
    
    @SerializedName("ad_type")
    val adType: String,
    
    @SerializedName("device_info")
    val deviceInfo: DeviceInfo
)

/**
 * Device information
 */
data class DeviceInfo(
    @SerializedName("os")
    val os: String = "android",
    
    @SerializedName("os_version")
    val osVersion: String = android.os.Build.VERSION.RELEASE,
    
    @SerializedName("device_model")
    val deviceModel: String = android.os.Build.MODEL,
    
    @SerializedName("device_manufacturer")
    val deviceManufacturer: String = android.os.Build.MANUFACTURER,
    
    @SerializedName("screen_width")
    var screenWidth: Int = 0,
    
    @SerializedName("screen_height")
    var screenHeight: Int = 0,
    
    @SerializedName("language")
    val language: String = java.util.Locale.getDefault().language,
    
    @SerializedName("country")
    val country: String = java.util.Locale.getDefault().country
)

/**
 * Ad response from server
 */
data class AdResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("ad")
    val ad: Ad?,
    
    @SerializedName("message")
    val message: String?
)

/**
 * Click request body
 */
data class ClickRequest(
    @SerializedName("sdk_key")
    val sdkKey: String,
    
    @SerializedName("ad_id")
    val adId: Any, // Can be Long or String
    
    @SerializedName("impression_id")
    val impressionId: String?
)

/**
 * Reward data for rewarded ads
 */
data class Reward(
    @SerializedName("amount")
    val amount: Int,
    
    @SerializedName("type")
    val type: String
)


package com.adplatform.sdk.models

import com.google.gson.annotations.SerializedName

/**
 * Ad data model
 */
data class Ad(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("campaign_id")
    val campaignId: Long,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("image_url")
    val imageUrl: String,
    
    @SerializedName("target_url")
    val targetUrl: String,
    
    @SerializedName("call_to_action")
    val callToAction: String?,
    
    @SerializedName("ad_type")
    val adType: String,
    
    @SerializedName("impression_id")
    val impressionId: Long?
)

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
    val adId: Long,
    
    @SerializedName("impression_id")
    val impressionId: Long
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

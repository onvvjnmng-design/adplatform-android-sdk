package com.adplatform.sdk.network

import com.adplatform.sdk.AdPlatform
import com.adplatform.sdk.models.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * API client for AdPlatform server communication
 */
class AdApiClient(private val sdkKey: String) {
    
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    
    private var baseUrl = AdPlatform.baseUrl
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    fun updateBaseUrl(url: String) {
        baseUrl = url
    }
    
    /**
     * Request an ad from the server
     */
    suspend fun requestAd(adType: String, deviceInfo: DeviceInfo): Result<Ad> {
        return withContext(Dispatchers.IO) {
            try {
                val request = AdRequest(
                    sdkKey = sdkKey,
                    adType = adType,
                    deviceInfo = deviceInfo
                )
                
                val requestBody = gson.toJson(request).toRequestBody(jsonMediaType)
                
                val httpRequest = Request.Builder()
                    .url("$baseUrl/api/sdk/ad")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                val response = client.newCall(httpRequest).execute()
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    val adResponse = gson.fromJson(responseBody, AdResponse::class.java)
                    if (adResponse.success && adResponse.ad != null) {
                        Result.success(adResponse.ad)
                    } else {
                        Result.failure(Exception(adResponse.message ?: "No ad available"))
                    }
                } else {
                    Result.failure(Exception("Server error: ${response.code}"))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Network error: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Track ad click
     */
    suspend fun trackClick(adId: Long, impressionId: Long): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ClickRequest(
                    sdkKey = sdkKey,
                    adId = adId,
                    impressionId = impressionId
                )
                
                val requestBody = gson.toJson(request).toRequestBody(jsonMediaType)
                
                val httpRequest = Request.Builder()
                    .url("$baseUrl/api/sdk/click")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                val response = client.newCall(httpRequest).execute()
                
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Failed to track click"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

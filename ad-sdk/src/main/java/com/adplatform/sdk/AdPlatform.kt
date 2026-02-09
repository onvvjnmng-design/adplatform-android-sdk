package com.AdNova.sdk

import android.content.Context
import android.util.Log
import com.AdNova.sdk.network.AdApiClient
import java.lang.ref.WeakReference

/**
 * AdNova SDK - Main entry point
 * 
 * Initialize this SDK in your Application class:
 * ```kotlin
 * AdNova.initialize(this, "YOUR_SDK_KEY")
 * ```
 */
object AdNova {
    private const val TAG = "AdNova"
    
    internal var sdkKey: String? = null
        private set
    
    internal var contextRef: WeakReference<Context>? = null
        private set
    
    internal var apiClient: AdApiClient? = null
        private set
    
    internal var isInitialized = false
        private set
    
    internal var baseUrl = "https://adnova.bbs.tr" // Production URL
        private set
    
    /**
     * Initialize the AdNova SDK
     * @param context Application context
     * @param sdkKey Your SDK key from the AdNova dashboard
     */
    @JvmStatic
    fun initialize(context: Context, sdkKey: String) {
        if (isInitialized) {
            Log.w(TAG, "AdNova SDK already initialized")
            return
        }
        
        if (sdkKey.isBlank()) {
            throw IllegalArgumentException("SDK key cannot be empty")
        }
        
        this.sdkKey = sdkKey
        this.contextRef = WeakReference(context.applicationContext)
        this.apiClient = AdApiClient(sdkKey)
        this.isInitialized = true
        
        Log.i(TAG, "AdNova SDK initialized successfully")
    }
    
    /**
     * Set custom base URL for the API (for testing or custom deployments)
     * @param url The base URL (e.g., "https://api.AdNova.com")
     */
    @JvmStatic
    fun setBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
        apiClient?.updateBaseUrl(baseUrl)
    }
    
    /**
     * Check if SDK is initialized
     */
    @JvmStatic
    fun isReady(): Boolean = isInitialized
    
    /**
     * Get SDK version
     */
    @JvmStatic
    fun getVersion(): String = BuildConfig.SDK_VERSION
    
    /**
     * Get the application context
     */
    internal fun getContext(): Context? = contextRef?.get()
    
    /**
     * Ensure SDK is initialized before use
     */
    internal fun ensureInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("AdNova SDK not initialized. Call AdNova.initialize() first.")
        }
    }
}


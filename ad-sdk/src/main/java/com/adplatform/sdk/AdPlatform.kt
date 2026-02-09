package com.adplatform.sdk

import android.content.Context
import android.util.Log
import com.adplatform.sdk.network.AdApiClient
import java.lang.ref.WeakReference

/**
 * AdPlatform SDK - Main entry point
 * 
 * Initialize this SDK in your Application class:
 * ```kotlin
 * AdPlatform.initialize(this, "YOUR_SDK_KEY")
 * ```
 */
object AdPlatform {
    private const val TAG = "AdPlatform"
    
    internal var sdkKey: String? = null
        private set
    
    internal var contextRef: WeakReference<Context>? = null
        private set
    
    internal var apiClient: AdApiClient? = null
        private set
    
    internal var isInitialized = false
        private set
    
    internal var baseUrl = "http://10.0.2.2:3000" // Default for emulator, change for production
        private set
    
    /**
     * Initialize the AdPlatform SDK
     * @param context Application context
     * @param sdkKey Your SDK key from the AdPlatform dashboard
     */
    @JvmStatic
    fun initialize(context: Context, sdkKey: String) {
        if (isInitialized) {
            Log.w(TAG, "AdPlatform SDK already initialized")
            return
        }
        
        if (sdkKey.isBlank()) {
            throw IllegalArgumentException("SDK key cannot be empty")
        }
        
        this.sdkKey = sdkKey
        this.contextRef = WeakReference(context.applicationContext)
        this.apiClient = AdApiClient(sdkKey)
        this.isInitialized = true
        
        Log.i(TAG, "AdPlatform SDK initialized successfully")
    }
    
    /**
     * Set custom base URL for the API (for testing or custom deployments)
     * @param url The base URL (e.g., "https://api.adplatform.com")
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
            throw IllegalStateException("AdPlatform SDK not initialized. Call AdPlatform.initialize() first.")
        }
    }
}

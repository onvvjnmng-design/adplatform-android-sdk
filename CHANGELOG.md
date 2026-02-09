# Changelog

## [1.0.15] - 2026-02-10

### Fixed
- Fixed backend API integration compatibility
- Support both `type` and `ad_type` field names
- Support both `click_url` and `target_url` field names  
- Support both `cta_text` and `call_to_action` field names
- Support `reward_type`/`reward_amount` as separate fields
- Made `id` dynamic to support both Long (production) and String (test ads)
- Made `impression_id` String type to match backend UUID format

## [1.0.0] - 2026-02-09

### Added
- Initial release
- Banner Ads (BannerAdView)
- Interstitial Ads (InterstitialAd)
- Rewarded Ads (RewardedAd) with reward callbacks
- Native Ads (NativeAdLoader)
- Ad Listener callbacks (onAdLoaded, onAdFailed, onAdClicked, onAdClosed, onAdImpression)
- Reward callbacks (onRewarded, onRewardedCompleted)
- Kotlin and Java support
- Minimum SDK 21 (Android 5.0)

### Features
- Easy initialization with SDK key
- Pre-loading support for better ad delivery
- Customizable banner sizes
- Test mode for development
- Comprehensive error handling
- Coroutines support for async operations


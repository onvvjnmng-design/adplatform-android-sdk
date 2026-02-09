# AdPlatform Android SDK

مكتبة إعلانات لتطبيقات Android - تدعم Java و Kotlin

## التثبيت

### 1. إضافة JitPack Repository

في ملف `settings.gradle` (Gradle 7.0+):

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

أو في ملف `build.gradle` (Project level) للإصدارات الأقدم:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. إضافة التبعية

في ملف `build.gradle` (Module: app):

```gradle
dependencies {
    implementation 'com.github.adplatform:ad-sdk:1.0.0'
}
```

### 3. إضافة أذونات الإنترنت

في ملف `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## الاستخدام

### التهيئة

```kotlin
// Kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdPlatform.initialize(this, "YOUR_SDK_KEY")
    }
}
```

```java
// Java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AdPlatform.initialize(this, "YOUR_SDK_KEY");
    }
}
```

### إعلان البانر

```kotlin
// Kotlin
val bannerAd = findViewById<BannerAdView>(R.id.bannerAd)
bannerAd.loadAd()
```

### الإعلان البيني

```kotlin
// Kotlin
val interstitialAd = InterstitialAd(this)
interstitialAd.loadAd()
interstitialAd.show()
```

### إعلان المكافأة

```kotlin
// Kotlin
val rewardedAd = RewardedAd(this)
rewardedAd.loadAd()
rewardedAd.show()
```

## الترخيص

MIT License

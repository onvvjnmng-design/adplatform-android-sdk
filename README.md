# AdNova Android SDK

مكتبة إعلانات لتطبيقات Android - تدعم Java و Kotlin

[![](https://jitpack.io/v/onvvjnmng-design/adplatform-android-sdk.svg)](https://jitpack.io/#onvvjnmng-design/adplatform-android-sdk)

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
    implementation 'com.github.onvvjnmng-design:adplatform-android-sdk:v1.0.4'
}
```

### 3. إضافة أذونات الإنترنت

في ملف `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## الاستخدام

### 1. التهيئة

#### Kotlin
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdNova.initialize(this, "YOUR_SDK_KEY")
    }
}
```

#### Java
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AdNova.initialize(this, "YOUR_SDK_KEY");
    }
}
```

**لا تنسى تسجيل Application في AndroidManifest.xml:**
```xml
<application
    android:name=".MyApplication"
    ...>
```

---

### 2. إعلان البانر (Banner Ad)

#### XML Layout
```xml
<com.adnova.sdk.BannerAdView
    android:id="@+id/bannerAd"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

#### Kotlin
```kotlin
val bannerAd = findViewById<BannerAdView>(R.id.bannerAd)
bannerAd.loadAd()

bannerAd.setAdListener(object : AdListener {
    override fun onAdLoaded() {
        // الإعلان جاهز
    }
    
    override fun onAdFailed(error: String) {
        Log.e("AdNova", "فشل التحميل: $error")
    }
    
    override fun onAdClicked() {
        // تم النقر على الإعلان
    }
})
```

#### Java
```java
BannerAdView bannerAd = findViewById(R.id.bannerAd);
bannerAd.loadAd();

bannerAd.setAdListener(new AdListener() {
    @Override
    public void onAdLoaded() {
        // الإعلان جاهز
    }
    
    @Override
    public void onAdFailed(String error) {
        Log.e("AdNova", "فشل التحميل: " + error);
    }
    
    @Override
    public void onAdClicked() {
        // تم النقر على الإعلان
    }
});
```

---

### 3. الإعلان البيني (Interstitial Ad)

#### Kotlin
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var interstitialAd: InterstitialAd
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        interstitialAd = InterstitialAd(this)
        interstitialAd.loadAd()
        
        interstitialAd.setAdListener(object : AdListener {
            override fun onAdLoaded() {
                // الإعلان جاهز للعرض
            }
            
            override fun onAdClosed() {
                // تم إغلاق الإعلان - حمل إعلان جديد
                interstitialAd.loadAd()
            }
            
            override fun onAdFailed(error: String) {
                Log.e("AdNova", "فشل التحميل: $error")
            }
        })
    }
    
    fun showAd() {
        if (interstitialAd.isLoaded) {
            interstitialAd.show()
        }
    }
}
```

#### Java
```java
public class MainActivity extends AppCompatActivity {
    private InterstitialAd interstitialAd;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        interstitialAd = new InterstitialAd(this);
        interstitialAd.loadAd();
        
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // الإعلان جاهز للعرض
            }
            
            @Override
            public void onAdClosed() {
                // تم إغلاق الإعلان - حمل إعلان جديد
                interstitialAd.loadAd();
            }
            
            @Override
            public void onAdFailed(String error) {
                Log.e("AdNova", "فشل التحميل: " + error);
            }
        });
    }
    
    public void showAd() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }
}
```

---

### 4. إعلان المكافأة (Rewarded Ad)

#### Kotlin
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var rewardedAd: RewardedAd
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        rewardedAd = RewardedAd(this)
        rewardedAd.loadAd()
        
        rewardedAd.setRewardedAdListener(object : RewardedAdListener {
            override fun onAdLoaded() {
                // الإعلان جاهز
            }
            
            override fun onUserEarnedReward(amount: Int, type: String) {
                // المستخدم شاهد الإعلان كاملاً - امنحه المكافأة
                Toast.makeText(this@MainActivity, "حصلت على $amount $type", Toast.LENGTH_SHORT).show()
            }
            
            override fun onAdClosed() {
                rewardedAd.loadAd()
            }
            
            override fun onAdFailed(error: String) {
                Log.e("AdNova", "فشل التحميل: $error")
            }
        })
    }
    
    fun showRewardedAd() {
        if (rewardedAd.isLoaded) {
            rewardedAd.show()
        }
    }
}
```

#### Java
```java
public class MainActivity extends AppCompatActivity {
    private RewardedAd rewardedAd;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        rewardedAd = new RewardedAd(this);
        rewardedAd.loadAd();
        
        rewardedAd.setRewardedAdListener(new RewardedAdListener() {
            @Override
            public void onAdLoaded() {
                // الإعلان جاهز
            }
            
            @Override
            public void onUserEarnedReward(int amount, String type) {
                // المستخدم شاهد الإعلان كاملاً - امنحه المكافأة
                Toast.makeText(MainActivity.this, "حصلت على " + amount + " " + type, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onAdClosed() {
                rewardedAd.loadAd();
            }
            
            @Override
            public void onAdFailed(String error) {
                Log.e("AdNova", "فشل التحميل: " + error);
            }
        });
    }
    
    public void showRewardedAd() {
        if (rewardedAd.isLoaded()) {
            rewardedAd.show();
        }
    }
}
```

---

### 5. الإعلان الأصلي (Native Ad)

#### Kotlin
```kotlin
val nativeAdLoader = NativeAdLoader(this)
nativeAdLoader.loadAd(object : NativeAdListener {
    override fun onNativeAdLoaded(nativeAd: NativeAd) {
        // عرض الإعلان في واجهتك المخصصة
        titleTextView.text = nativeAd.title
        descriptionTextView.text = nativeAd.description
        Glide.with(this@MainActivity).load(nativeAd.imageUrl).into(adImageView)
        ctaButton.text = nativeAd.callToAction
        
        // تسجيل النقر
        ctaButton.setOnClickListener {
            nativeAd.performClick()
        }
        
        // تسجيل الظهور
        nativeAd.recordImpression()
    }
    
    override fun onAdFailed(error: String) {
        Log.e("AdNova", "فشل التحميل: $error")
    }
})
```

#### Java
```java
NativeAdLoader nativeAdLoader = new NativeAdLoader(this);
nativeAdLoader.loadAd(new NativeAdListener() {
    @Override
    public void onNativeAdLoaded(NativeAd nativeAd) {
        // عرض الإعلان في واجهتك المخصصة
        titleTextView.setText(nativeAd.getTitle());
        descriptionTextView.setText(nativeAd.getDescription());
        Glide.with(MainActivity.this).load(nativeAd.getImageUrl()).into(adImageView);
        ctaButton.setText(nativeAd.getCallToAction());
        
        // تسجيل النقر
        ctaButton.setOnClickListener(v -> {
            nativeAd.performClick();
        });
        
        // تسجيل الظهور
        nativeAd.recordImpression();
    }
    
    @Override
    public void onAdFailed(String error) {
        Log.e("AdNova", "فشل التحميل: " + error);
    }
});
```

---

## الحصول على SDK Key

1. سجل في منصة AdNova
2. أضف تطبيقك في لوحة تحكم الناشر
3. انسخ SDK Key من إعدادات التطبيق

---

## الدعم

- الموقع: [adnova.com](https://adnova.com)
- البريد: support@adnova.com

## الترخيص

MIT License


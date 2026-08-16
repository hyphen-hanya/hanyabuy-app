// 韩亚Buy App - app 模块 Gradle 配置
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

// 读取签名配置 (keystore.properties 由 CI 注入, 不提交仓库)
def keystorePropertiesFile = rootProject.file("keystore.properties")
def keystoreProperties = new Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.withInputStream { keystoreProperties.load(it) }
}

android {
    namespace 'com.hanyabuy.app'
    compileSdk 34

    defaultConfig {
        applicationId "com.hanyabuy.app"
        minSdk 24          // Android 7.0+, 覆盖绝大多数设备
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
    }

    signingConfigs {
        // 签名配置从 keystore.properties 读取(CI注入). 无签名字段则回退.
        if (keystoreProperties.containsKey("storeFile")) {
            release {
                storeFile file(keystoreProperties["storeFile"])
                storePassword keystoreProperties["storePassword"]
                keyAlias keystoreProperties["keyAlias"]
                keyPassword keystoreProperties["keyPassword"]
            }
        }
    }

    buildTypes {
        debug {
            // 调试包: 允许未签名构建
        }
        release {
            minifyEnabled false   // WebView 无原生 bridge, 不需混淆
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            if (keystoreProperties.containsKey("storeFile")) {
                signingConfig signingConfigs.release
            }
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = '17'
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.fragment:fragment-ktx:1.6.2'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.core:core-ktx:1.12.0'
}

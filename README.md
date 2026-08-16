# 韩亚Buy Android App

韩亚投资集团整体 App：主站 / 全球采购 / 语音客服 / 图书馆，全家桶套壳。

## 技术栈
- 纯原生 **Kotlin** WebView 套壳（非 Capacitor/Cordova——本项目只需麦克风权限一个原生能力，原生 WebView `onPermissionRequest` 即可，避免重型框架）
- 底部导航多 Tab，每 Tab 独立 WebView（状态隔离）
- Gradle (JDK 17) + Android SDK，CI 用 GitHub Actions 云端构建
- 分发：MVOMV 官网 APK 直装；后续国内商店（软著 + ICP 备案）

## Tab 结构
| Tab | 页面 |
|---|---|
| 首页 | https://hanyabuy.com/ |
| 全球采购 | https://hanyabuy.com/ (采购区) |
| 语音客服 | https://voice.hanyabuy.com/ |
| 图书馆 | https://hanyabuy.com/library_catalog/ |

## 构建
本机无 Java/Gradle/SDK，用 GitHub Actions 云端构建：
- push 到 main 自动触发 `.github/workflows/build-apk.yml`
- 或仓库 Actions 手动 Run workflow
- APK 产物在 Actions 的 Artifacts 里下载

## 签名 (CI Secrets)
在 GitHub 仓库 Settings → Secrets 配置：
- `ANDROID_KEYSTORE_BASE64` — keystore 文件 base64
- `KEYSTORE_PASSWORD` — keystore 密码
- `KEY_ALIAS` — 别名
- `KEY_PASSWORD` — 别名密码

不配这些则产出未签名 release (不能安装), debug 包可装。

## 语音客服麦克风(核心坑)
WebView 麦克风必须三处齐开：
1. Manifest `RECORD_AUDIO` + 运行时动态申请（MainActivity 已做）
2. `webSettings.mediaPlaybackRequiresUserGesture=false`
3. **重写 `WebChromeClient.onPermissionRequest` 并 `grant(RESOURCE_AUDIO_CAPTURE)`**（AppWebView.kt 已做——这是总开关，不重写=永远静默失败）

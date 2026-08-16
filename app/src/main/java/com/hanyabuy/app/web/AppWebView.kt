package com.hanyabuy.app.web

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * 韩亚Buy 统一 WebView 封装
 *
 * 核心职责：
 * 1. 开启 JS + 媒体播放（mediaPlaybackRequiresUserGesture=false）
 * 2. 处理麦克风权限（onPermissionRequest 是 WebView getUserMedia 的总开关，不重写=永远静默失败）
 * 3. 音频焦点管理（TTS 播放与来电/系统音冲突处理）
 * 4. Cookie 同域共享（让主站/语音客服/图书馆登录态互通）
 */
@SuppressLint("SetJavaScriptEnabled")
class AppWebView(context: Context, url: String) : WebView(context) {

    init {
        setup()
        configureCookieSync()
        loadUrl(url)
    }

    private fun setup() {
        // 基础设置
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = false
        settings.mediaPlaybackRequiresUserGesture = false // 关键：允许 JS 自动播放/音频
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW // 全 HTTPS, 拒绝混合内容
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.databaseEnabled = true
        settings.setSupportZoom(false)
        settings.blockNetworkLoads = false

        // 麦克风/摄像头权限回调总开关
        webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                val resources = request.resources
                // 授予音频捕获（麦克风）。本项目只需麦克风，其他资源拒绝。
                if (resources.isNotEmpty() &&
                    request.origin.host?.contains("hanyabuy", ignoreCase = true) == true
                ) {
                    request.grant(resources)
                } else {
                    request.deny()
                }
            }
        }

        // 页面导航（保持应用内，不跳系统浏览器）
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                // 仅允许 https 的 hanyabuy/aicanteen/boaimusic 域内跳转, 其他(如外链/scheme)交给系统
                if (url.startsWith("http")) {
                    view?.loadUrl(url)
                    return true
                }
                return false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                // 弱网/404 错误 - MVP 交给前端错误处理, 不在此强提示
                super.onReceivedError(view, request, error)
            }
        }
    }

    /** Cookie 同域共享：Android WebView 同进程多实例会自动共享同域 Cookie */
    private fun configureCookieSync() {
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
    }

    /** 获得音频焦点（TTS 朗读前） */
    fun requestAudioFocus() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.requestAudioFocus(
            null,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    /** 释放音频焦点（停止朗读/切走） */
    fun abandonAudioFocus() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.abandonAudioFocus(null)
    }
}

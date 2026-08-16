package com.hanyabuy.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hanyabuy.app.R

/**
 * 韩亚Buy App 主界面
 * 底部导航多 Tab，每 Tab 独立 WebView（状态隔离）
 *
 * Tab:
 *  - 首页      hanyabuy.com
 *  - 全球采购  hanyabuy.com (采购区, 新窗口/锚点)
 *  - 语音客服  voice.hanyabuy.com
 *  - 图书馆    hanyabuy.com/library_catalog/
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private val fragments = mutableMapOf<Int, Fragment>()
    private var activeTab: Int = R.id.nav_home

    // 麦克风运行时权限请求（语音客服需要）
    private val micPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // 已授权, 语音客服可正常用麦克风
            } else {
                // 拒绝: 语音客服页面内的 WebRTC 会得到 NotAllowedError, 前端有兜底提示
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_nav)
        setupBottomNav()

        if (savedInstanceState == null) {
            switchTab(R.id.nav_home)
        }
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            switchTab(item.itemId)
            true
        }
    }

    private fun switchTab(tabId: Int) {
        if (tabId == activeTab) return

        // 切换前暂停当前 WebView（语音停止）
        supportFragmentManager.findFragmentById(R.id.fragment_container)?.onPause()

        activeTab = tabId
        val tag = "tab_$tabId"
        val fm = supportFragmentManager
        var frag = fragments[tabId]
        val ft = fm.beginTransaction()

        if (frag == null) {
            frag = when (tabId) {
                R.id.nav_home -> WebTabFragment.newInstance("https://hanyabuy.com/")
                R.id.nav_global_procure -> WebTabFragment.newInstance("https://hanyabuy.com/")
                R.id.nav_voice -> {
                    // 进入语音客服前, 预请求麦克风权限
                    requestMicIfNeeded()
                    WebTabFragment.newInstance("https://voice.hanyabuy.com/")
                }
                R.id.nav_library -> WebTabFragment.newInstance("https://hanyabuy.com/library_catalog/")
                else -> WebTabFragment.newInstance("https://hanyabuy.com/")
            }
            fragments[tabId] = frag
            ft.replace(R.id.fragment_container, frag, tag)
        } else {
            ft.replace(R.id.fragment_container, frag, tag)
        }
        ft.commitNowAllowingStateLoss()
    }

    private fun requestMicIfNeeded() {
        val perm = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            micPermissionLauncher.launch(perm)
        }
    }
}

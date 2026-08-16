package com.hanyabuy.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.hanyabuy.app.R
import com.hanyabuy.app.web.AppWebView

/**
 * 每个 Tab 一个 WebView 实例（状态独立隔离）
 */
class WebTabFragment : Fragment() {

    private var webView: AppWebView? = null
    private var url: String = ""

    companion object {
        const val ARG_URL = "url"
        fun newInstance(url: String): WebTabFragment {
            val f = WebTabFragment()
            f.arguments = Bundle().apply { putString(ARG_URL, url) }
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString(ARG_URL) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 复用单 WebView, 避免重复创建
        if (webView == null) {
            webView = AppWebView(requireContext(), url)
        }
        return webView!!
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
        webView?.abandonAudioFocus()
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 不销毁 WebView 以保状态 (Fragment 复用)。Activity 真正销毁时清理。
    }

    override fun onDestroy() {
        super.onDestroy()
        webView?.destroy()
        webView = null
    }
}

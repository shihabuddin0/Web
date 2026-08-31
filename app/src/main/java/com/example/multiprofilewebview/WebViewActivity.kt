package com.example.multiprofilewebview

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var homeUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeUrl = intent.getStringExtra("url") ?: "https://example.com"
        val profileId = intent.getStringExtra("profile_id") ?: "default"
        val profileName = "account_profile_$profileId"

        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) {
            Toast.makeText(
                this,
                "Your Android WebView does not support separate profiles.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        webView = WebView(this)

        // Must be done before configuring or navigating the WebView.
        WebViewCompat.setProfile(webView, profileName)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

        webView.webViewClient = WebViewClient()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val home = Button(this).apply {
            text = "Home"
            setOnClickListener { webView.loadUrl(homeUrl) }
        }

        val refresh = Button(this).apply {
            text = "Refresh"
            setOnClickListener { webView.reload() }
        }

        bar.addView(home, LinearLayout.LayoutParams(0, -2, 1f))
        bar.addView(refresh, LinearLayout.LayoutParams(0, -2, 1f))

        root.addView(bar, LinearLayout.LayoutParams(-1, -2))
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))

        setContentView(root)
        webView.loadUrl(homeUrl)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }
}

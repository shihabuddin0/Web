package com.example.multiprofilewebview

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
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

        // Check WebView Multi-Profile support
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) {
            Toast.makeText(
                this,
                "Your Android WebView does not support separate profiles.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        // Create WebView
        webView = WebView(this)

        // Set separate WebView profile
        WebViewCompat.setProfile(webView, profileName)

        // WebView settings
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.cacheMode =
            android.webkit.WebSettings.LOAD_DEFAULT

        webView.webViewClient = WebViewClient()

        // Main layout
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Top button bar
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // HOME BUTTON
        val home = Button(this).apply {
            text = "Home"

            setOnClickListener {

                /*
                 * Home চাপলে WebView থেকে বের হয়ে
                 * MainActivity-এর My Web Profiles screen-এ ফিরে যাবে।
                 */
                finish()
            }
        }

        // REFRESH BUTTON
        val refresh = Button(this).apply {
            text = "Refresh"

            setOnClickListener {
                webView.reload()
            }
        }

        // Add buttons
        bar.addView(
            home,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        bar.addView(
            refresh,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        // Add top bar
        root.addView(
            bar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // Add WebView
        root.addView(
            webView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        // Open selected account website
        webView.loadUrl(homeUrl)
    }

    // Android back button
    @Deprecated("Deprecated in Java")
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

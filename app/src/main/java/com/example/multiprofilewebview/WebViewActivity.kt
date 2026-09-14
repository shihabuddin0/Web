package com.example.multiprofilewebview

import android.os.Bundle
import android.view.Gravity
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import org.json.JSONArray

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private lateinit var titleButton: Button

    private val accounts = mutableListOf<Account>()

    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadAccounts()

        currentIndex =
            intent.getIntExtra(
                "page_index",
                0
            )

        if (accounts.isEmpty()) {
            finish()
            return
        }

        if (currentIndex < 0 ||
            currentIndex >= accounts.size
        ) {
            currentIndex = 0
        }

        if (!WebViewFeature.isFeatureSupported(
                WebViewFeature.MULTI_PROFILE
            )
        ) {

            finish()
            return
        }

        createWebView()
        createLayout()

        loadCurrentPage()
    }

    private fun createWebView() {

        webView = WebView(this)

        /*
         * Every Page has its OWN profile.
         *
         * The profile ID is permanent,
         * so its cookies/storage remain attached
         * to that Page.
         */
        val profileId =
            accounts[currentIndex].id

        WebViewCompat.setProfile(
            webView,
            "facebook_$profileId"
        )

        webView.settings.apply {

            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true

            loadsImagesAutomatically = true

            cacheMode =
                android.webkit.WebSettings.LOAD_DEFAULT

            javaScriptCanOpenWindowsAutomatically =
                true

            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }

        webView.webViewClient =
            WebViewClient()

        CookieManager
            .getInstance()
            .setAcceptCookie(true)
    }

    private fun createLayout() {

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
            }

        /*
         * TOP NAVIGATION
         */
        val topBar =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        previousButton =
            Button(this).apply {

                text = "←"

                setOnClickListener {
                    goPrevious()
                }
            }

        titleButton =
            Button(this).apply {

                isAllCaps = false

                text = "Page"
            }

        nextButton =
            Button(this).apply {

                text = "→"

                setOnClickListener {
                    goNext()
                }
            }

        topBar.addView(
            previousButton,
            LinearLayout.LayoutParams(
                70,
                -2
            )
        )

        topBar.addView(
            titleButton,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        topBar.addView(
            nextButton,
            LinearLayout.LayoutParams(
                70,
                -2
            )
        )

        /*
         * SECOND BAR
         */
        val bottomBar =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL
            }

        val homeButton =
            Button(this).apply {

                text = "HOME"

                setOnClickListener {
                    finish()
                }
            }

        val refreshButton =
            Button(this).apply {

                text = "REFRESH"

                setOnClickListener {
                    webView.reload()
                }
            }

        bottomBar.addView(
            homeButton,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        bottomBar.addView(
            refreshButton,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        root.addView(topBar)
        root.addView(bottomBar)

        root.addView(
            webView,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun loadCurrentPage() {

        if (currentIndex < 0 ||
            currentIndex >= accounts.size
        ) {
            return
        }

        val account =
            accounts[currentIndex]

        titleButton.text =
            "${currentIndex + 1}. ${account.name}"

        /*
         * IMPORTANT:
         *
         * Switching Page changes profile.
         * The current WebView must be recreated
         * with the selected profile.
         */
        recreateWebViewForCurrentPage()

        updateButtons()
    }

    private fun recreateWebViewForCurrentPage() {

        val oldWebView = webView

        val parent =
            oldWebView.parent as? LinearLayout

        parent?.removeView(oldWebView)

        oldWebView.stopLoading()
        oldWebView.destroy()

        webView = WebView(this)

        val profileId =
            accounts[currentIndex].id

        WebViewCompat.setProfile(
            webView,
            "facebook_$profileId"
        )

        webView.settings.apply {

            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true

            loadsImagesAutomatically = true

            cacheMode =
                android.webkit.WebSettings.LOAD_DEFAULT

            javaScriptCanOpenWindowsAutomatically =
                true

            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }

        webView.webViewClient =
            WebViewClient()

        CookieManager
            .getInstance()
            .setAcceptCookie(true)

        parent?.addView(
            webView,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        webView.loadUrl(
            accounts[currentIndex].url
        )
    }

    private fun goPrevious() {

        if (currentIndex > 0) {

            currentIndex--

            loadCurrentPage()
        }
    }

    private fun goNext() {

        if (currentIndex <
            accounts.size - 1
        ) {

            currentIndex++

            loadCurrentPage()
        }
    }

    private fun updateButtons() {

        previousButton.isEnabled =
            currentIndex > 0

        nextButton.isEnabled =
            currentIndex <
                    accounts.size - 1
    }

    private fun loadAccounts() {

        accounts.clear()

        val raw =
            getSharedPreferences(
                "accounts",
                MODE_PRIVATE
            ).getString(
                "list",
                "[]"
            ) ?: "[]"

        val array =
            JSONArray(raw)

        for (i in 0 until array.length()) {

            val obj =
                array.getJSONObject(i)

            accounts.add(
                Account(
                    id = obj.optString(
                        "id",
                        "profile_$i"
                    ),
                    name = obj.optString(
                        "name",
                        "Page ${i + 1}"
                    ),
                    url = obj.optString(
                        "url",
                        "https://www.facebook.com/"
                    )
                )
            )
        }
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

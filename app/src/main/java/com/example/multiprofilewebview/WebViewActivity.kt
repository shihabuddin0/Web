package com.example.multiprofilewebview

import android.os.Bundle
import android.view.Gravity
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import org.json.JSONArray

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private lateinit var titleButton: Button

    private val accounts =
        mutableListOf<Account>()

    private var currentIndex = 0

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

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

        if (
            currentIndex < 0 ||
            currentIndex >= accounts.size
        ) {

            currentIndex = 0
        }

        if (
            !WebViewFeature.isFeatureSupported(
                WebViewFeature.MULTI_PROFILE
            )
        ) {

            Toast.makeText(
                this,
                "Your Android WebView does not support separate profiles.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        createWebView()

        createLayout()

        loadCurrentPage()
    }

    // =========================================================
    // CREATE WEBVIEW
    // =========================================================

    private fun createWebView() {

        webView =
            WebView(this)

        val profileId =
            accounts[currentIndex].id

        /*
         * IMPORTANT:
         *
         * Keep the SAME profile naming style
         * used by the previous version:
         *
         * account_profile_xxx
         *
         * This helps preserve old logged-in
         * WebView profiles after updating the app.
         */
        val profileName =
            "account_profile_$profileId"

        WebViewCompat.setProfile(
            webView,
            profileName
        )

        configureWebView()
    }

    // =========================================================
    // WEBVIEW SETTINGS
    // =========================================================

    private fun configureWebView() {

        webView.settings.apply {

            javaScriptEnabled =
                true

            domStorageEnabled =
                true

            databaseEnabled =
                true

            loadsImagesAutomatically =
                true

            cacheMode =
                android.webkit.WebSettings.LOAD_DEFAULT

            javaScriptCanOpenWindowsAutomatically =
                true

            setSupportZoom(
                false
            )

            builtInZoomControls =
                false

            displayZoomControls =
                false
        }

        webView.webViewClient =
            WebViewClient()

        CookieManager
            .getInstance()
            .setAcceptCookie(
                true
            )
    }

    // =========================================================
    // CREATE UI
    // =========================================================

    private fun createLayout() {

        val root =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        // -----------------------------------------------------
        // TOP BAR
        // -----------------------------------------------------

        val topBar =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        previousButton =
            Button(this).apply {

                text =
                    "←"

                setOnClickListener {

                    goPrevious()
                }
            }

        titleButton =
            Button(this).apply {

                isAllCaps =
                    false

                text =
                    "Page"

                isClickable =
                    false
            }

        nextButton =
            Button(this).apply {

                text =
                    "→"

                setOnClickListener {

                    goNext()
                }
            }

        topBar.addView(

            previousButton,

            LinearLayout.LayoutParams(
                65,
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
                65,
                -2
            )
        )

        // -----------------------------------------------------
        // SECOND BAR
        // -----------------------------------------------------

        val bottomBar =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        val homeButton =
            Button(this).apply {

                text =
                    "HOME"

                setOnClickListener {

                    finish()
                }
            }

        val refreshButton =
            Button(this).apply {

                text =
                    "REFRESH"

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

        // -----------------------------------------------------
        // ADD EVERYTHING
        // -----------------------------------------------------

        root.addView(
            topBar,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        root.addView(
            bottomBar,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        root.addView(

            webView,

            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(
            root
        )
    }

    // =========================================================
    // LOAD CURRENT PAGE
    // =========================================================

    private fun loadCurrentPage() {

        if (
            currentIndex < 0 ||
            currentIndex >= accounts.size
        ) {
            return
        }

        val account =
            accounts[currentIndex]

        titleButton.text =
            "${currentIndex + 1}. ${account.name}"

        recreateWebViewForCurrentPage()

        updateButtons()
    }

    // =========================================================
    // CHANGE PROFILE
    // =========================================================

    private fun recreateWebViewForCurrentPage() {

        /*
         * Remove old WebView
         */
        val oldWebView =
            webView

        val parent =
            oldWebView.parent
                    as? LinearLayout

        parent?.removeView(
            oldWebView
        )

        oldWebView.stopLoading()

        oldWebView.destroy()

        /*
         * Create new WebView
         * for the selected profile.
         */
        webView =
            WebView(this)

        val profileId =
            accounts[currentIndex].id

        val profileName =
            "account_profile_$profileId"

        WebViewCompat.setProfile(
            webView,
            profileName
        )

        configureWebView()

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

    // =========================================================
    // PREVIOUS
    // =========================================================

    private fun goPrevious() {

        if (currentIndex > 0) {

            currentIndex--

            loadCurrentPage()
        }
    }

    // =========================================================
    // NEXT
    // =========================================================

    private fun goNext() {

        if (
            currentIndex <
            accounts.size - 1
        ) {

            currentIndex++

            loadCurrentPage()
        }
    }

    // =========================================================
    // ENABLE / DISABLE NAVIGATION
    // =========================================================

    private fun updateButtons() {

        previousButton.isEnabled =
            currentIndex > 0

        nextButton.isEnabled =
            currentIndex <
                    accounts.size - 1
    }

    // =========================================================
    // LOAD ACCOUNTS
    // =========================================================

    private fun loadAccounts() {

        accounts.clear()

        val raw =
            getSharedPreferences(
                "accounts",
                MODE_PRIVATE
            )
                .getString(
                    "list",
                    "[]"
                )
                ?: "[]"

        val array =
            JSONArray(raw)

        for (
            i in 0 until array.length()
        ) {

            val obj =
                array.getJSONObject(i)

            val id =
                if (obj.has("id")) {

                    obj.optString(
                        "id"
                    )

                } else {

                    /*
                     * Old account compatibility.
                     */
                    "legacy_$i"
                }

            accounts.add(

                Account(

                    id =
                        id,

                    name =
                        obj.optString(
                            "name",
                            "Page ${i + 1}"
                        ),

                    url =
                        obj.optString(
                            "url",
                            "https://www.facebook.com/"
                        )
                )
            )
        }
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

    override fun onBackPressed() {

        if (
            ::webView.isInitialized &&
            webView.canGoBack()
        ) {

            webView.goBack()

        } else {

            super.onBackPressed()
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        if (
            ::webView.isInitialized
        ) {

            webView.stopLoading()

            webView.destroy()
        }

        super.onDestroy()
    }
}

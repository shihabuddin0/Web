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

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private lateinit var pageTitle: Button

    private var currentIndex = 0

    private val accounts = mutableListOf<Account>()

    private val prefs by lazy {
        getSharedPreferences(
            "accounts",
            MODE_PRIVATE
        )
    }

    private val sharedProfile =
        MainActivity.SHARED_PROFILE

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
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

    private fun createWebView() {

        webView = WebView(this)

        /*
         * IMPORTANT:
         *
         * Every Facebook Page uses the SAME
         * WebView profile.
         *
         * Therefore Facebook login session
         * is shared between the Pages.
         */
        WebViewCompat.setProfile(
            webView,
            sharedProfile
        )

        webView.settings.apply {

            javaScriptEnabled = true

            domStorageEnabled = true

            databaseEnabled = true

            loadsImagesAutomatically = true

            cacheMode =
                android.webkit.WebSettings.LOAD_DEFAULT

            setSupportZoom(false)

            builtInZoomControls = false

            displayZoomControls = false

            javaScriptCanOpenWindowsAutomatically =
                true
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
         * TOP BAR
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

        pageTitle =
            Button(this).apply {

                text = "Page"

                isAllCaps = false

                setOnClickListener {

                    /*
                     * Pressing page name also
                     * opens the current page again.
                     */
                    loadCurrentPage()
                }
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
            pageTitle,
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

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        val homeButton =
            Button(this).apply {

                text = "HOME"

                setOnClickListener {

                    /*
                     * Return to MainActivity.
                     */
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

        setContentView(root)
    }

    private fun loadCurrentPage() {

        if (accounts.isEmpty()) {
            return
        }

        val account =
            accounts[currentIndex]

        pageTitle.text =
            "${currentIndex + 1}. ${account.name}"

        webView.loadUrl(
            account.url
        )

        updateNavigationButtons()
    }

    private fun goPrevious() {

        if (currentIndex > 0) {

            currentIndex--

            loadCurrentPage()
        }
    }

    private fun goNext() {

        if (currentIndex < accounts.size - 1) {

            currentIndex++

            loadCurrentPage()
        }
    }

    private fun updateNavigationButtons() {

        previousButton.isEnabled =
            currentIndex > 0

        nextButton.isEnabled =
            currentIndex <
                    accounts.size - 1
    }

    private fun loadAccounts() {

        accounts.clear()

        val raw =
            prefs.getString(
                "list",
                "[]"
            ) ?: "[]"

        val array =
            org.json.JSONArray(raw)

        for (i in 0 until array.length()) {

            val obj =
                array.getJSONObject(i)

            accounts.add(
                Account(
                    id = obj.optString(
                        "id",
                        "page_$i"
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

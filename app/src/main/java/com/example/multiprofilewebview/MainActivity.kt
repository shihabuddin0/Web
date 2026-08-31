package com.example.multiprofilewebview

import android.content.Context
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

data class Account(
    val name: String,
    val url: String
)

class MainActivity : AppCompatActivity() {

    private lateinit var listLayout: LinearLayout
    private val accounts = mutableListOf<Account>()

    private val prefs by lazy {
        getSharedPreferences("accounts", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(20, 20, 20, 20)

        val addButton = Button(this)
        addButton.text = "+ Add Account"

        listLayout = LinearLayout(this)
        listLayout.orientation = LinearLayout.VERTICAL

        root.addView(
            addButton,
            LinearLayout.LayoutParams(-1, -2)
        )

        root.addView(
            listLayout,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(root)

        loadAccounts()
        refreshList()

        addButton.setOnClickListener {
            showAddDialog()
        }
    }

    private fun showAddDialog() {

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 10, 30, 10)

        val nameInput = EditText(this)
        nameInput.hint = "Account name"

        val urlInput = EditText(this)
        urlInput.hint = "https://example.com"
        urlInput.inputType =
            android.text.InputType.TYPE_CLASS_TEXT or
            android.text.InputType.TYPE_TEXT_VARIATION_URI

        layout.addView(nameInput)
        layout.addView(urlInput)

        AlertDialog.Builder(this)
            .setTitle("Add Account")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->

                val name = nameInput.text.toString().trim()
                var url = urlInput.text.toString().trim()

                if (name.isEmpty() || url.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Name and URL required",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                if (!url.startsWith("http://") &&
                    !url.startsWith("https://")) {
                    url = "https://$url"
                }

                accounts.add(Account(name, url))
                saveAccounts()
                refreshList()
            }
            .show()
    }

    private fun refreshList() {

        listLayout.removeAllViews()

        accounts.forEachIndexed { index, account ->

            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.setPadding(0, 8, 0, 8)

            val nameButton = Button(this)
            nameButton.text = account.name

            val deleteButton = Button(this)
            deleteButton.text = "Delete"

            row.addView(
                nameButton,
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            row.addView(
                deleteButton,
                LinearLayout.LayoutParams(-2, -2)
            )

            nameButton.setOnClickListener {
                openWebsite(account)
            }

            deleteButton.setOnClickListener {

                AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage(
                        "Delete ${account.name}?"
                    )
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete") { _, _ ->

                        accounts.removeAt(index)
                        saveAccounts()
                        refreshList()
                    }
                    .show()
            }

            listLayout.addView(row)
        }
    }

    private fun openWebsite(account: Account) {

        val webView = WebView(this)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true

        webView.webViewClient = WebViewClient()

        setContentView(webView)

        webView.loadUrl(account.url)
    }

    private fun saveAccounts() {

        val array = JSONArray()

        accounts.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("url", it.url)
            array.put(obj)
        }

        prefs.edit()
            .putString("list", array.toString())
            .apply()
    }

    private fun loadAccounts() {

        accounts.clear()

        val data = prefs.getString("list", "[]")
            ?: "[]"

        val array = JSONArray(data)

        for (i in 0 until array.length()) {

            val obj = array.getJSONObject(i)

            accounts.add(
                Account(
                    obj.getString("name"),
                    obj.getString("url")
                )
            )
        }
    }

    override fun onBackPressed() {
        recreate()
    }
}

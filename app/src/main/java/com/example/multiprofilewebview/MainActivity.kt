package com.example.multiprofilewebview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

data class Account(val name: String, val url: String)

class MainActivity : AppCompatActivity() {

    private val accounts = mutableListOf<Account>()
    private lateinit var listLayout: LinearLayout

    private val prefs by lazy {
        getSharedPreferences("accounts", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(this).apply {
            text = "My Web Profiles"
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }

        val add = Button(this).apply {
            text = "+ Add Account"
            setOnClickListener { showAddDialog() }
        }

        listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(title, LinearLayout.LayoutParams(-1, -2))
        root.addView(add, LinearLayout.LayoutParams(-1, -2))
        root.addView(listLayout, LinearLayout.LayoutParams(-1, 0, 1f))

        setContentView(root)

        loadAccounts()
        refreshList()
    }

    private fun showAddDialog() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 8, 32, 0)
        }

        val name = EditText(this).apply {
            hint = "Account name"
        }

        val url = EditText(this).apply {
            hint = "https://example.com"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_URI
        }

        box.addView(name)
        box.addView(url)

        AlertDialog.Builder(this)
            .setTitle("Add Account")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val n = name.text.toString().trim()
                var u = url.text.toString().trim()

                if (n.isEmpty() || u.isEmpty()) {
                    Toast.makeText(this, "Name and URL are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (!u.startsWith("http://") && !u.startsWith("https://")) {
                    u = "https://$u"
                }

                accounts.add(Account(n, u))
                saveAccounts()
                refreshList()
            }
            .show()
    }

    private fun refreshList() {
        listLayout.removeAllViews()

        accounts.forEachIndexed { index, account ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }

            val open = Button(this).apply {
                text = account.name
                setOnClickListener {
                    startActivity(
                        Intent(this@MainActivity, WebViewActivity::class.java)
                            .putExtra("url", account.url)
                            .putExtra("profile_id", index)
                    )
                }
            }

            val delete = Button(this).apply {
                text = "Delete"
                setOnClickListener {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Delete Account")
                        .setMessage("Delete ${account.name}?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete") { _, _ ->
                            accounts.removeAt(index)
                            saveAccounts()
                            refreshList()
                        }
                        .show()
                }
            }

            row.addView(open, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(delete, LinearLayout.LayoutParams(-2, -2))
            listLayout.addView(row)
        }
    }

    private fun saveAccounts() {
        val arr = JSONArray()
        accounts.forEach {
            arr.put(JSONObject().apply {
                put("name", it.name)
                put("url", it.url)
            })
        }
        prefs.edit().putString("list", arr.toString()).apply()
    }

    private fun loadAccounts() {
        accounts.clear()
        val raw = prefs.getString("list", "[]") ?: "[]"
        val arr = JSONArray(raw)

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            accounts.add(Account(obj.getString("name"), obj.getString("url")))
        }
    }
}
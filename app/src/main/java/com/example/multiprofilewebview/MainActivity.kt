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

data class Account(
    val id: String,
    val name: String,
    val url: String
)

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
            setOnClickListener {
                showAddDialog()
            }
        }

        listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(title)
        root.addView(add)
        root.addView(
            listLayout,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(root)

        loadAccounts()
        refreshList()
    }

    private fun showAddDialog() {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 8, 32, 0)
        }

        val name = EditText(this)
        name.hint = "Account name"

        val url = EditText(this)
        url.hint = "https://example.com"
        url.inputType =
            android.text.InputType.TYPE_CLASS_TEXT or
            android.text.InputType.TYPE_TEXT_VARIATION_URI

        box.addView(name)
        box.addView(url)

        AlertDialog.Builder(this)
            .setTitle("Add Account")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->

                val accountName =
                    name.text.toString().trim()

                var accountUrl =
                    url.text.toString().trim()

                if (accountName.isEmpty() ||
                    accountUrl.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Name and URL are required",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                if (!accountUrl.startsWith("http://") &&
                    !accountUrl.startsWith("https://")) {

                    accountUrl =
                        "https://$accountUrl"
                }

                val account = Account(
                    id = System.currentTimeMillis().toString(),
                    name = accountName,
                    url = accountUrl
                )

                accounts.add(account)

                saveAccounts()
                refreshList()
            }
            .show()
    }

    private fun refreshList() {

        listLayout.removeAllViews()

        accounts.forEach { account ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }

            val openButton = Button(this).apply {

                text = account.name

                setOnClickListener {

                    val intent = Intent(
                        this@MainActivity,
                        WebViewActivity::class.java
                    )

                    intent.putExtra(
                        "url",
                        account.url
                    )

                    intent.putExtra(
                        "profile_id",
                        account.id
                    )

                    startActivity(intent)
                }
            }

            val deleteButton = Button(this).apply {

                text = "Delete"

                setOnClickListener {

                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Delete Account")
                        .setMessage(
                            "Delete ${account.name}?"
                        )
                        .setNegativeButton(
                            "Cancel",
                            null
                        )
                        .setPositiveButton(
                            "Delete"
                        ) { _, _ ->

                            accounts.remove(account)

                            saveAccounts()
                            refreshList()
                        }
                        .show()
                }
            }

            row.addView(
                openButton,
                LinearLayout.LayoutParams(
                    0,
                    -2,
                    1f
                )
            )

            row.addView(
                deleteButton,
                LinearLayout.LayoutParams(
                    -2,
                    -2
                )
            )

            listLayout.addView(row)
        }
    }

    private fun saveAccounts() {

        val array = JSONArray()

        accounts.forEach {

            val obj = JSONObject()

            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("url", it.url)

            array.put(obj)
        }

        prefs.edit()
            .putString(
                "list",
                array.toString()
            )
            .apply()
    }

    private fun loadAccounts() {

        accounts.clear()

        val raw =
            prefs.getString("list", "[]")
                ?: "[]"

        val array = JSONArray(raw)

        for (i in 0 until array.length()) {

            val obj =
                array.getJSONObject(i)

            val id =
                obj.optString(
                    "id",
                    "legacy_$i"
                )

            accounts.add(
                Account(
                    id,
                    obj.getString("name"),
                    obj.getString("url")
                )
            )
        }
    }
}

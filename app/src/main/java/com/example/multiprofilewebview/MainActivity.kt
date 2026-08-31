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

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            add,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            listLayout,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
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

        val name = EditText(this).apply {
            hint = "Account name"
        }

        val url = EditText(this).apply {
            hint = "https://example.com"

            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
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

                    Toast.makeText(
                        this,
                        "Name and URL are required",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                if (
                    !u.startsWith("http://") &&
                    !u.startsWith("https://")
                ) {
                    u = "https://$u"
                }

                /*
                 * IMPORTANT:
                 *
                 * এই ID-টাই ওই account-এর WebView profile-এর
                 * permanent identity হবে।
                 */
                val permanentId =
                    "profile_" + System.currentTimeMillis()

                val account = Account(
                    id = permanentId,
                    name = n,
                    url = u
                )

                accounts.add(account)

                /*
                 * ID সহ save করা হচ্ছে
                 */
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

            val open = Button(this).apply {

                text = account.name

                setOnClickListener {

                    val intent =
                        Intent(
                            this@MainActivity,
                            WebViewActivity::class.java
                        )

                    intent.putExtra(
                        "url",
                        account.url
                    )

                    /*
                     * একই ID প্রতিবার পাঠানো হবে।
                     *
                     * তাই একই WebView profile খুলবে।
                     */
                    intent.putExtra(
                        "profile_id",
                        account.id
                    )

                    startActivity(intent)
                }
            }

            val delete = Button(this).apply {

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
                open,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            row.addView(
                delete,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            listLayout.addView(row)
        }
    }

    private fun saveAccounts() {

        val arr = JSONArray()

        accounts.forEach { account ->

            val obj = JSONObject()

            /*
             * সবচেয়ে গুরুত্বপূর্ণ অংশ:
             * ID এখন permanent ভাবে save হচ্ছে।
             */
            obj.put(
                "id",
                account.id
            )

            obj.put(
                "name",
                account.name
            )

            obj.put(
                "url",
                account.url
            )

            arr.put(obj)
        }

        prefs.edit()
            .putString(
                "list",
                arr.toString()
            )
            .apply()
    }

    private fun loadAccounts() {

        accounts.clear()

        val raw =
            prefs.getString(
                "list",
                "[]"
            ) ?: "[]"

        val arr = JSONArray(raw)

        for (i in 0 until arr.length()) {

            val obj =
                arr.getJSONObject(i)

            /*
             * নতুন account হলে saved ID পাওয়া যাবে।
             */
            var id =
                obj.optString("id", "")

            /*
             * পুরোনো account-এর ID যদি না থাকে,
             * তাহলে একবার নতুন permanent ID তৈরি হবে।
             *
             * এরপর saveAccounts() করলে সেটা আর বদলাবে না।
             */
            if (id.isEmpty()) {

                id =
                    "profile_" +
                            System.currentTimeMillis() +
                            "_" +
                            i
            }

            val name =
                obj.optString(
                    "name",
                    "Account ${i + 1}"
                )

            val url =
                obj.optString(
                    "url",
                    "https://example.com"
                )

            accounts.add(
                Account(
                    id = id,
                    name = name,
                    url = url
                )
            )
        }

        /*
         * পুরোনো account-এর নতুন ID-ও
         * এখন permanently save হয়ে যাবে।
         */
        saveAccounts()
    }
}

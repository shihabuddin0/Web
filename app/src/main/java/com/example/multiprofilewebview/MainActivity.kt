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
    var name: String,
    var url: String
)

class MainActivity : AppCompatActivity() {

    private val accounts = mutableListOf<Account>()

    private lateinit var listLayout: LinearLayout

    private val prefs by lazy {
        getSharedPreferences("accounts", Context.MODE_PRIVATE)
    }

    companion object {
        const val SHARED_PROFILE = "facebook_shared_profile"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadAccounts()
        showHome()
    }

    private fun showHome() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(this).apply {
            text = "My Facebook Pages"
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }

        val addButton = Button(this).apply {
            text = "+ ADD PAGE"

            setOnClickListener {
                showAddPageDialog()
            }
        }

        listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        root.addView(
            addButton,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        root.addView(
            listLayout,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(root)

        refreshList()
    }

    private fun showAddPageDialog() {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 8, 32, 0)
        }

        val nameInput = EditText(this).apply {
            hint = "Page name"
        }

        val urlInput = EditText(this).apply {
            hint = "https://www.facebook.com/yourpage"

            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_URI
        }

        box.addView(nameInput)
        box.addView(urlInput)

        AlertDialog.Builder(this)
            .setTitle("Add Facebook Page")
            .setView(box)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("SAVE") { _, _ ->

                val name =
                    nameInput.text.toString().trim()

                var url =
                    urlInput.text.toString().trim()

                if (name.isEmpty() || url.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Page name and URL are required",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                if (!url.startsWith("http://") &&
                    !url.startsWith("https://")
                ) {
                    url = "https://$url"
                }

                val newPage = Account(
                    id = "page_" +
                            System.currentTimeMillis(),
                    name = name,
                    url = url
                )

                accounts.add(newPage)

                saveAccounts()
                refreshList()
            }
            .show()
    }

    private fun showEditDialog(index: Int) {

        val account = accounts[index]

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 8, 32, 0)
        }

        val nameInput = EditText(this).apply {
            hint = "Page name"
            setText(account.name)
            setSelection(text.length)
        }

        val urlInput = EditText(this).apply {
            hint = "Page URL"
            setText(account.url)
            setSelection(text.length)

            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_URI
        }

        box.addView(nameInput)
        box.addView(urlInput)

        AlertDialog.Builder(this)
            .setTitle("Edit Page")
            .setView(box)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("SAVE") { _, _ ->

                val newName =
                    nameInput.text.toString().trim()

                var newUrl =
                    urlInput.text.toString().trim()

                if (newName.isEmpty() ||
                    newUrl.isEmpty()
                ) {

                    Toast.makeText(
                        this,
                        "Name and URL are required",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                if (!newUrl.startsWith("http://") &&
                    !newUrl.startsWith("https://")
                ) {
                    newUrl = "https://$newUrl"
                }

                accounts[index].name = newName
                accounts[index].url = newUrl

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
                setPadding(0, 6, 0, 6)
            }

            val openButton = Button(this).apply {

                text = "${index + 1}. ${account.name}"

                setOnClickListener {

                    openPage(index)
                }
            }

            val editButton = Button(this).apply {

                text = "EDIT"

                setOnClickListener {

                    showEditDialog(index)
                }
            }

            val deleteButton = Button(this).apply {

                text = "DELETE"

                setOnClickListener {

                    confirmDelete(index)
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
                editButton,
                LinearLayout.LayoutParams(
                    -2,
                    -2
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

    private fun openPage(index: Int) {

        if (index < 0 ||
            index >= accounts.size
        ) {
            return
        }

        startActivity(
            Intent(
                this,
                WebViewActivity::class.java
            )
                .putExtra(
                    "page_index",
                    index
                )
                .putExtra(
                    "profile_id",
                    SHARED_PROFILE
                )
        )
    }

    private fun confirmDelete(index: Int) {

        val account = accounts[index]

        AlertDialog.Builder(this)
            .setTitle("Delete Page")
            .setMessage(
                "Delete ${account.name}?"
            )
            .setNegativeButton(
                "CANCEL",
                null
            )
            .setPositiveButton(
                "DELETE"
            ) { _, _ ->

                accounts.removeAt(index)

                saveAccounts()
                refreshList()
            }
            .show()
    }

    private fun saveAccounts() {

        val array = JSONArray()

        accounts.forEach { account ->

            array.put(
                JSONObject().apply {

                    put(
                        "id",
                        account.id
                    )

                    put(
                        "name",
                        account.name
                    )

                    put(
                        "url",
                        account.url
                    )
                }
            )
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
            prefs.getString(
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
}

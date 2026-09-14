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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAccounts()
        showHome()
    }

    private fun showHome() {

    val root = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(20, 20, 20, 20)
    }

    val title = TextView(this).apply {
        text = "My Facebook Pages"
        textSize = 24f
        gravity = Gravity.CENTER
        setPadding(0, 0, 0, 15)
    }

    val addButton = Button(this).apply {
        text = "+ ADD PAGE"

        setOnClickListener {
            showAddDialog()
        }
    }

    // Profile list
    listLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
    }

    // IMPORTANT:
    // This makes the profile list scrollable.
    val scrollView = ScrollView(this).apply {
        isFillViewport = true

        addView(
            listLayout,
            ScrollView.LayoutParams(
                -1,
                -2
            )
        )
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
        scrollView,
        LinearLayout.LayoutParams(
            -1,
            0,
            1f
        )
    )

    setContentView(root)

    refreshList()
}

    private fun showAddDialog() {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 5, 30, 0)
        }

        val nameInput = EditText(this).apply {
            hint = "Page name"
        }

        val urlInput = EditText(this).apply {
            hint = "https://www.facebook.com/"
            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_URI
        }

        box.addView(nameInput)
        box.addView(urlInput)

        AlertDialog.Builder(this)
            .setTitle("Add Page")
            .setView(box)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("SAVE") { _, _ ->

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
                    !url.startsWith("https://")
                ) {
                    url = "https://$url"
                }

                accounts.add(
                    Account(
                        id = "profile_" +
                                System.currentTimeMillis(),
                        name = name,
                        url = url
                    )
                )

                saveAccounts()
                refreshList()
            }
            .show()
    }

    private fun showEditDialog(index: Int) {

        val account = accounts[index]

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 5, 30, 0)
        }

        val nameInput = EditText(this).apply {
            hint = "Page name"
            setText(account.name)
        }

        val urlInput = EditText(this).apply {
            hint = "Page URL"
            setText(account.url)
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

                val name =
                    nameInput.text.toString().trim()

                var url =
                    urlInput.text.toString().trim()

                if (name.isEmpty() || url.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Name and URL required",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                if (!url.startsWith("http://") &&
                    !url.startsWith("https://")
                ) {
                    url = "https://$url"
                }

                accounts[index].name = name
                accounts[index].url = url

                saveAccounts()
                refreshList()
            }
            .show()
    }

    private fun copyPage(index: Int) {

        val source = accounts[index]

        val copied = Account(
            id = "profile_" +
                    System.currentTimeMillis(),
            name = source.name + " Copy",
            url = source.url
        )

        accounts.add(copied)

        saveAccounts()
        refreshList()

        Toast.makeText(
            this,
            "New profile created. Open it and switch to the required Page.",
            Toast.LENGTH_LONG
        ).show()
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

    private fun openPage(index: Int) {

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
                    accounts[index].id
                )
        )
    }

    private fun refreshList() {

        listLayout.removeAllViews()

        accounts.forEachIndexed { index, account ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 5, 0, 5)
            }

            val open = Button(this).apply {
                text = "${index + 1}. ${account.name}"

                setOnClickListener {
                    openPage(index)
                }
            }

            val copy = Button(this).apply {
                text = "COPY"

                setOnClickListener {
                    copyPage(index)
                }
            }

            val edit = Button(this).apply {
                text = "EDIT"

                setOnClickListener {
                    showEditDialog(index)
                }
            }

            val delete = Button(this).apply {
                text = "DELETE"

                setOnClickListener {
                    confirmDelete(index)
                }
            }

            row.addView(
                open,
                LinearLayout.LayoutParams(
                    0,
                    -2,
                    1f
                )
            )

            row.addView(copy)
            row.addView(edit)
            row.addView(delete)

            listLayout.addView(row)
        }
    }

    private fun saveAccounts() {

        val array = JSONArray()

        accounts.forEach {

            array.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("name", it.name)
                    put("url", it.url)
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

        val array = JSONArray(raw)

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

        // Fix old accounts that didn't have permanent IDs
        var changed = false

        accounts.forEachIndexed { index, account ->

            if (account.id == "profile_$index") {

                accounts[index] = account.copy(
                    id = "profile_" +
                            System.currentTimeMillis() +
                            "_" +
                            index
                )

                changed = true
            }
        }

        if (changed) {
            saveAccounts()
        }
    }
}

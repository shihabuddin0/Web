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
        getSharedPreferences(
            "accounts",
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadAccounts()
        showHome()
    }

    private fun showHome() {

        val root = LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            setPadding(
                20,
                20,
                20,
                20
            )
        }

        // TITLE
        val title = TextView(this).apply {

            text =
                "My Facebook Pages"

            textSize = 24f

            gravity =
                Gravity.CENTER

            setPadding(
                0,
                0,
                0,
                15
            )
        }

        // ADD PAGE BUTTON
        val addButton = Button(this).apply {

            text =
                "+ ADD PAGE"

            setOnClickListener {

                showAddDialog()
            }
        }

        // PROFILE LIST
        listLayout = LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL
        }

        /*
         * IMPORTANT:
         *
         * ScrollView is used here so that
         * unlimited profiles/pages can be
         * viewed by scrolling.
         */
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

    // =========================================================
    // ADD PAGE
    // =========================================================

    private fun showAddDialog() {

        val box = LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            setPadding(
                30,
                5,
                30,
                0
            )
        }

        val nameInput = EditText(this).apply {

            hint =
                "Page / Profile name"
        }

        val urlInput = EditText(this).apply {

            hint =
                "https://www.facebook.com/yourpage"

            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_URI
        }

        box.addView(nameInput)
        box.addView(urlInput)

        AlertDialog.Builder(this)

            .setTitle(
                "Add Page"
            )

            .setView(box)

            .setNegativeButton(
                "CANCEL",
                null
            )

            .setPositiveButton(
                "SAVE"
            ) { _, _ ->

                val name =
                    nameInput.text
                        .toString()
                        .trim()

                var url =
                    urlInput.text
                        .toString()
                        .trim()

                if (
                    name.isEmpty() ||
                    url.isEmpty()
                ) {

                    Toast.makeText(
                        this,
                        "Name and URL required",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                if (
                    !url.startsWith("http://") &&
                    !url.startsWith("https://")
                ) {

                    url =
                        "https://$url"
                }

                /*
                 * Every newly created profile gets
                 * a permanent unique ID.
                 */
                val newAccount =
                    Account(
                        id =
                            "profile_" +
                                    System.currentTimeMillis(),

                        name =
                            name,

                        url =
                            url
                    )

                accounts.add(
                    newAccount
                )

                saveAccounts()

                refreshList()
            }

            .show()
    }

    // =========================================================
    // EDIT PAGE
    // =========================================================

    private fun showEditDialog(
        index: Int
    ) {

        val account =
            accounts[index]

        val box =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    30,
                    5,
                    30,
                    0
                )
            }

        val nameInput =
            EditText(this).apply {

                hint =
                    "Page name"

                setText(
                    account.name
                )

                setSelection(
                    text.length
                )
            }

        val urlInput =
            EditText(this).apply {

                hint =
                    "Page URL"

                setText(
                    account.url
                )

                setSelection(
                    text.length
                )

                inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_URI
            }

        box.addView(nameInput)
        box.addView(urlInput)

        AlertDialog.Builder(this)

            .setTitle(
                "Edit Page"
            )

            .setView(box)

            .setNegativeButton(
                "CANCEL",
                null
            )

            .setPositiveButton(
                "SAVE"
            ) { _, _ ->

                val newName =
                    nameInput.text
                        .toString()
                        .trim()

                var newUrl =
                    urlInput.text
                        .toString()
                        .trim()

                if (
                    newName.isEmpty() ||
                    newUrl.isEmpty()
                ) {

                    Toast.makeText(
                        this,
                        "Name and URL required",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                if (
                    !newUrl.startsWith("http://") &&
                    !newUrl.startsWith("https://")
                ) {

                    newUrl =
                        "https://$newUrl"
                }

                /*
                 * IMPORTANT:
                 *
                 * ID is NOT changed.
                 *
                 * Therefore existing WebView
                 * profile/session remains attached
                 * to this page.
                 */
                accounts[index].name =
                    newName

                accounts[index].url =
                    newUrl

                saveAccounts()

                refreshList()
            }

            .show()
    }

    // =========================================================
    // COPY PROFILE
    // =========================================================

    private fun copyPage(
        index: Int
    ) {

        val source =
            accounts[index]

        /*
         * COPY creates a NEW profile ID.
         *
         * We do NOT copy Facebook private
         * authentication/session tokens.
         *
         * The new profile starts with the
         * same URL and name, then you can open
         * Facebook and switch to the required Page.
         */
        val copied =
            Account(

                id =
                    "profile_" +
                            System.currentTimeMillis(),

                name =
                    source.name +
                            " Copy",

                url =
                    source.url
            )

        accounts.add(
            copied
        )

        saveAccounts()

        refreshList()

        Toast.makeText(
            this,
            "New profile created. Open it and switch to the required Page.",
            Toast.LENGTH_LONG
        ).show()
    }

    // =========================================================
    // DELETE
    // =========================================================

    private fun confirmDelete(
        index: Int
    ) {

        val account =
            accounts[index]

        AlertDialog.Builder(this)

            .setTitle(
                "Delete Page"
            )

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

                accounts.removeAt(
                    index
                )

                saveAccounts()

                refreshList()
            }

            .show()
    }

    // =========================================================
    // OPEN WEBVIEW
    // =========================================================

    private fun openPage(
        index: Int
    ) {

        if (
            index < 0 ||
            index >= accounts.size
        ) {
            return
        }

        val account =
            accounts[index]

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
                    account.id
                )
        )
    }

    // =========================================================
    // REFRESH LIST
    // =========================================================

    private fun refreshList() {

        listLayout.removeAllViews()

        accounts.forEachIndexed {
                index,
                account ->

            val row =
                LinearLayout(this).apply {

                    orientation =
                        LinearLayout.HORIZONTAL

                    gravity =
                        Gravity.CENTER_VERTICAL

                    setPadding(
                        0,
                        5,
                        0,
                        5
                    )
                }

            // OPEN
            val openButton =
                Button(this).apply {

                    text =
                        "${index + 1}. ${account.name}"

                    setOnClickListener {

                        openPage(
                            index
                        )
                    }
                }

            // COPY
            val copyButton =
                Button(this).apply {

                    text =
                        "COPY"

                    setOnClickListener {

                        copyPage(
                            index
                        )
                    }
                }

            // EDIT
            val editButton =
                Button(this).apply {

                    text =
                        "EDIT"

                    setOnClickListener {

                        showEditDialog(
                            index
                        )
                    }
                }

            // DELETE
            val deleteButton =
                Button(this).apply {

                    text =
                        "DELETE"

                    setOnClickListener {

                        confirmDelete(
                            index
                        )
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
                copyButton,
                LinearLayout.LayoutParams(
                    -2,
                    -2
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

            listLayout.addView(
                row
            )
        }
    }

    // =========================================================
    // SAVE
    // =========================================================

    private fun saveAccounts() {

        val array =
            JSONArray()

        accounts.forEach {

            array.put(

                JSONObject().apply {

                    put(
                        "id",
                        it.id
                    )

                    put(
                        "name",
                        it.name
                    )

                    put(
                        "url",
                        it.url
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

    // =========================================================
    // LOAD
    // =========================================================

    private fun loadAccounts() {

        accounts.clear()

        val raw =
            prefs.getString(
                "list",
                "[]"
            ) ?: "[]"

        val array =
            JSONArray(raw)

        for (
            i in 0 until array.length()
        ) {

            val obj =
                array.getJSONObject(i)

            /*
             * IMPORTANT COMPATIBILITY:
             *
             * Old accounts created by your previous
             * app did not save ID.
             *
             * We keep their old ID as:
             *
             * legacy_0
             * legacy_1
             * legacy_2
             *
             * This prevents changing their old
             * WebView profile names during update.
             */
            val id =
                if (obj.has("id")) {

                    obj.optString(
                        "id"
                    )

                } else {

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
}

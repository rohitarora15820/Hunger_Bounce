package com.rohit.hungerbounce.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.rohit.hungerbounce.R
import com.rohit.hungerbounce.utility.ConnectionManager
import com.rohit.hungerbounce.utility.SessionManager
import com.rohit.hungerbounce.utility.Validations

import org.json.JSONObject
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {
    lateinit var layoutRegister: RelativeLayout
    lateinit var toolbar: Toolbar
    lateinit var editText1Register: EditText
    lateinit var editText2Register: EditText
    lateinit var editText3Register: EditText
    lateinit var editText4Register: EditText
    lateinit var editText5Register: EditText
    lateinit var editText6Register: EditText
    lateinit var buttonRegister: Button
    lateinit var progressBar: ProgressBar
    lateinit var sessionManager: SessionManager
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        layoutRegister = findViewById(R.id.layoutRegister)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Register Yourself"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sessionManager = SessionManager(this@RegisterActivity)
        sharedPreferences = this@RegisterActivity.getSharedPreferences(
            sessionManager.PREF_NAME,
            sessionManager.PRIVATE_MODE
        )
        editText1Register = findViewById(R.id.editText1Register)
        editText2Register = findViewById(R.id.editText2Register)
        editText3Register = findViewById(R.id.editText3Register)
        editText4Register = findViewById(R.id.editText4Register)
        editText5Register = findViewById(R.id.editText5Register)
        editText6Register = findViewById(R.id.editText6Register)
        buttonRegister = findViewById(R.id.buttonRegister)
        progressBar = findViewById(R.id.progressBar)
        layoutRegister.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
        buttonRegister.setOnClickListener {
            layoutRegister.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            if (Validations.validateNameLength(editText1Register.text.toString())) {
                editText1Register.error = null
                if (Validations.validateEmail(editText2Register.text.toString())) {
                    editText3Register.error = null
                    if (Validations.validateMobile(editText3Register.text.toString())) {
                        editText3Register.error = null
                        if (Validations.validatePasswordLength(editText4Register.text.toString())) {
                            editText4Register.error = null
                            if (Validations.matchPassword(
                                    editText5Register.text.toString(),
                                    editText6Register.text.toString()
                                )
                            ) {
                                editText5Register.error = null
                                editText6Register.error = null
                                if (ConnectionManager().checkConnectivity(this@RegisterActivity)) {
                                    sendRegisterRequest(
                                        editText1Register.text.toString(),
                                        editText3Register.text.toString(),
                                        editText4Register.text.toString(),
                                        editText5Register.text.toString(),
                                        editText2Register.text.toString()
                                    )
                                } else {
                                    layoutRegister.visibility = View.VISIBLE
                                    progressBar.visibility = View.INVISIBLE
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "No Internet Connection",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            } else {
                                layoutRegister.visibility = View.VISIBLE
                                progressBar.visibility = View.INVISIBLE
                                editText5Register.error = "Passwords don't match"
                                editText6Register.error = "Passwords don't match"
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Passwords don't match",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        } else {
                            layoutRegister.visibility = View.VISIBLE
                            progressBar.visibility = View.INVISIBLE
                            editText5Register.error =
                                "Password should be more than or equal 4 digits"
                            Toast.makeText(
                                this@RegisterActivity,
                                "Password should be more than or equal 4 digits",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        layoutRegister.visibility = View.VISIBLE
                        progressBar.visibility = View.INVISIBLE
                        editText3Register.error = "Invalid Mobile number"
                        Toast.makeText(
                            this@RegisterActivity,
                            "Invalid Mobile number",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    layoutRegister.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                    editText2Register.error = "Invalid Email"
                    Toast.makeText(this@RegisterActivity, "Invalid Email", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                layoutRegister.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
                editText1Register.error = "Invalid Name"
                Toast.makeText(this@RegisterActivity, "Invalid Name", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun sendRegisterRequest(
        name: String,
        phone: String,
        address: String,
        password: String,
        email: String
    ) {

        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v2/register/fetch_result"

        val jsonParams = JSONObject()
        jsonParams.put("name", name)
        jsonParams.put("mobile_number", phone)
        jsonParams.put("password", password)
        jsonParams.put("address", address)
        jsonParams.put("email", email)

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonParams,
            Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        val response = data.getJSONObject("data")
                        sharedPreferences.edit()
                            .putString("user_id", response.getString("user_id")).apply()
                        sharedPreferences.edit()
                            .putString("user_name", response.getString("name")).apply()
                        sharedPreferences.edit()
                            .putString(
                                "user_mobile_number",
                                response.getString("mobile_number")
                            )
                            .apply()
                        sharedPreferences.edit()
                            .putString("user_address", response.getString("address"))
                            .apply()
                        sharedPreferences.edit()
                            .putString("user_email", response.getString("email")).apply()
                        sessionManager.setLogin(true)
                        startActivity(
                            Intent(
                                this@RegisterActivity,
                                DashboardActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        layoutRegister.visibility = View.VISIBLE
                        progressBar.visibility = View.INVISIBLE
                        val errorMessage = data.getString("errorMessage")
                        Toast.makeText(
                            this@RegisterActivity,
                            errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    layoutRegister.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this@RegisterActivity, it.message, Toast.LENGTH_SHORT).show()
                layoutRegister.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"


                headers["token"] = "28c1eaad0887fc"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }
    override fun onSupportNavigateUp(): Boolean {
        Volley.newRequestQueue(this).cancelAll(this::class.java.simpleName)
        onBackPressed()
        return true
    }


}
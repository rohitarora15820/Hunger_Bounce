package com.rohit.hungerbounce.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.rohit.hungerbounce.R
import com.rohit.hungerbounce.utility.ConnectionManager
import com.rohit.hungerbounce.utility.Validations
import org.json.JSONObject

class ForgotActivity : AppCompatActivity() {
    lateinit var editText1Forgot: EditText
    lateinit var editText2Forgot: EditText
    lateinit var buttonForgot: Button
    lateinit var progressBar: ProgressBar
    lateinit var layoutForgot: RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)
        editText1Forgot = findViewById(R.id.editText1Forgot)
        editText2Forgot = findViewById(R.id.editText2Forgot)
        buttonForgot = findViewById(R.id.buttonForgot)
        layoutForgot = findViewById(R.id.layoutForgot)
        progressBar = findViewById(R.id.progressBar)
        layoutForgot.visibility = View.VISIBLE
        progressBar.visibility = View.GONE


        buttonForgot.setOnClickListener {
            val forgotMobileNumber = editText1Forgot.text.toString()
            if (Validations.validateMobile(forgotMobileNumber)) {
                editText1Forgot.error = null
                if (Validations.validateEmail(editText2Forgot.text.toString())) {
                    if (ConnectionManager().checkConnectivity(this@ForgotActivity)) {
                        layoutForgot.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        sendOTP(editText1Forgot.text.toString(), editText2Forgot.text.toString())
                    } else {
                        layoutForgot.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@ForgotActivity,
                            "No Internet Connection!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
                    layoutForgot.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    editText2Forgot.error = "Invalid Email"
                }
            } else {
                layoutForgot.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                editText1Forgot.error = "Invalid Mobile Number"
            }
        }
    }

    private fun sendOTP(mobileNumber: String, email: String) {
        val queue = Volley.newRequestQueue(this)

        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNumber)
        jsonParams.put("email", email)
        val url="http://13.235.250.119/v2/forgot_password/fetch_result"

        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        val firstTry = data.getBoolean("first_try")
                        if (firstTry) {
                            val builder = AlertDialog.Builder(this@ForgotActivity)
                            builder.setTitle("Information")
                            builder.setMessage("Please check your registered Email for the OTP.")
                            builder.setCancelable(false)
                            builder.setPositiveButton("Ok") { _, _ ->
                                val intent = Intent(
                                    this@ForgotActivity,
                                    ResetActivity::class.java
                                )
                                intent.putExtra("user_mobile", mobileNumber)
                                startActivity(intent)
                            }
                            builder.create().show()
                        } else {
                            val builder = AlertDialog.Builder(this@ForgotActivity)
                            builder.setTitle("Information")
                            builder.setMessage("Please refer to the previous email for the OTP.")
                            builder.setCancelable(false)
                            builder.setPositiveButton("Ok") { _, _ ->
                                val intent = Intent(
                                    this@ForgotActivity,
                                    ResetActivity::class.java
                                )
                                intent.putExtra("user_mobile", mobileNumber)
                                startActivity(intent)
                            }
                            builder.create().show()
                        }
                    } else {
                        layoutForgot.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@ForgotActivity,
                            "Mobile number not registered!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    layoutForgot.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ForgotActivity,
                        "Incorrect response error!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, Response.ErrorListener {
                layoutForgot.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                VolleyLog.e("Error::::", "/post request fail! Error: ${it.message}")
                Toast.makeText(this@ForgotActivity, it.message, Toast.LENGTH_SHORT).show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"

                    /*The below used token will not work, kindly use the token provided to you in the training*/
                    headers["token"] = "28c1eaad0887fc"
                    return headers
                }
            }
        queue.add(jsonObjectRequest)

    }
}


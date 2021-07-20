package com.rohit.hungerbounce.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.rohit.hungerbounce.R
import com.rohit.hungerbounce.utility.ConnectionManager
import com.rohit.hungerbounce.utility.Validations
import org.json.JSONObject

class ResetActivity : AppCompatActivity() {
    lateinit var layoutReset:RelativeLayout
    lateinit var txtReset:TextView
    lateinit var editText1Reset:EditText
    lateinit var editText2Reset:EditText
    lateinit var editText3Reset:EditText
    lateinit var buttonReset:Button
    lateinit var progressBar: ProgressBar
    lateinit var mobileNumber:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset)
        layoutReset=findViewById(R.id.layoutReset)
        txtReset=findViewById(R.id.txtReset)
        editText1Reset=findViewById(R.id.editText1Reset)
        editText2Reset=findViewById(R.id.editText2Reset)
        editText3Reset=findViewById(R.id.editText3Reset)
        buttonReset=findViewById(R.id.buttonReset)
        progressBar=findViewById(R.id.progressBar)
        layoutReset.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        if (intent != null) {
            mobileNumber = intent.getStringExtra("user_mobile") as String
        }
        buttonReset.setOnClickListener {
            layoutReset.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            if (ConnectionManager().checkConnectivity(this@ResetActivity)) {
                if (editText1Reset.text.length == 4) {
                    if (Validations.validatePasswordLength(editText2Reset.text.toString())) {

                        if (Validations.matchPassword(
                                editText2Reset.text.toString(),
                                editText3Reset.text.toString()
                            )
                        ) {
                            resetPassword(
                                mobileNumber,
                                editText1Reset.text.toString(),
                                editText2Reset.text.toString()
                            )
                        } else {
                            layoutReset.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                            Toast.makeText(
                                this@ResetActivity,
                                "Passwords do not match",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else {
                        layoutReset.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@ResetActivity,
                            "Invalid Password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    layoutReset.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ResetActivity, "Incorrect OTP", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                layoutReset.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@ResetActivity,
                    "No Internet Connection!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun resetPassword(mobileNumber: String, otp: String, password: String) {
        val queue = Volley.newRequestQueue(this)

        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNumber)
        jsonParams.put("password", password)
        jsonParams.put("otp", otp)
        val url="http://13.235.250.119/v2/reset_password/fetch_result"

        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        progressBar.visibility = View.INVISIBLE
                        val dialog = AlertDialog.Builder(this@ResetActivity)
                        dialog.setTitle("Confirm")
                        dialog.setMessage("Your password successfully changed")
                        dialog.setIcon(R.drawable.ic_sucess)
                        dialog.setCancelable(false)
                        dialog.setPositiveButton("Ok") { _, _ ->
                            startActivity(
                                Intent(
                                    this@ResetActivity,
                                    LoginActivity::class.java
                                )
                            )
                            ActivityCompat.finishAffinity(this@ResetActivity)
                        }
                        dialog.create()
                            dialog.show()
                    } else {
                        layoutReset.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        val error = data.getString("errorMessage")
                        Toast.makeText(
                            this@ResetActivity,
                            error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    layoutReset.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ResetActivity,
                        "Incorrect Response!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, Response.ErrorListener {
                layoutReset.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                VolleyLog.e("Error::::", "/post request fail! Error: ${it.message}")
                Toast.makeText(this@ResetActivity, it.message, Toast.LENGTH_SHORT).show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "28c1eaad0887fc"
                    return headers
                }
            }
        queue.add(jsonObjectRequest)
    }
}

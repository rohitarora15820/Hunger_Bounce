package com.rohit.hungerbounce.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.rohit.hungerbounce.R
import com.rohit.hungerbounce.utility.ConnectionManager
import com.rohit.hungerbounce.utility.SessionManager
import com.rohit.hungerbounce.utility.Validations
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    lateinit var editText1Login: EditText
    lateinit var editText2Login: EditText
    lateinit var buttonLogin: Button
    lateinit var txtForgotLogin:TextView
    lateinit var txtRegisterLogin:TextView
    lateinit var sharedPreferences: SharedPreferences

    lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editText1Login=findViewById(R.id.editText1Login)
        editText2Login=findViewById(R.id.editText2Login)
        buttonLogin=findViewById(R.id.buttonLogin)
        txtForgotLogin=findViewById(R.id.txtforgotLogin)
        txtRegisterLogin=findViewById(R.id.txtsignupLogin)
        txtForgotLogin.setOnClickListener {
            startActivity(Intent(this@LoginActivity,
                ForgotActivity::class.java))

        }
        txtRegisterLogin.setOnClickListener {
            startActivity(Intent(this@LoginActivity,
                RegisterActivity::class.java))

        }
        sessionManager= SessionManager(this)
        sharedPreferences=this.getSharedPreferences(sessionManager.PREF_NAME,sessionManager.PRIVATE_MODE)
        buttonLogin.setOnClickListener {
            buttonLogin.visibility= View.INVISIBLE

            if(Validations.validateMobile(editText1Login.text.toString()) && Validations.validatePasswordLength(editText2Login.text.toString())){
if(ConnectionManager().checkConnectivity(this@LoginActivity)){
    val queue= Volley.newRequestQueue(this@LoginActivity)
val jsonParams=JSONObject()
    jsonParams.put("mobile_number",editText1Login.text.toString())
    jsonParams.put("password",editText2Login.text.toString())
    val url="http://13.235.250.119/v2/login/fetch_result/"
    val jsonObjectRequest=object: JsonObjectRequest(Method.POST,url,jsonParams, Response.Listener {
try{
val data=it.getJSONObject("data")
    val success=data.getBoolean("success")
    if(success){
val response=data.getJSONObject("data")
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
            Intent(this@LoginActivity, DashboardActivity::class.java))
        finish()
    } else {
        buttonLogin.visibility = View.VISIBLE
        txtForgotLogin.visibility = View.VISIBLE
        buttonLogin.visibility = View.VISIBLE
        val errorMessage = data.getString("errorMessage")
        Toast.makeText(
            this@LoginActivity,
            errorMessage,
            Toast.LENGTH_SHORT
        ).show()
    }
} catch (e: JSONException) {
    buttonLogin.visibility = View.VISIBLE
    txtForgotLogin.visibility = View.VISIBLE
    txtRegisterLogin.visibility = View.VISIBLE
    e.printStackTrace()
}
    },
        Response.ErrorListener {
            buttonLogin.visibility = View.VISIBLE
            txtForgotLogin.visibility = View.VISIBLE
            txtRegisterLogin.visibility = View.VISIBLE
            Log.e("Error::::", "/post request fail! Error: ${it.message}")
        }) {
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers["Content-type"] = "application/json"
            headers["token"] = "28c1eaad0887fc"
            return headers
        }
    }
    queue.add(jsonObjectRequest)
} else {
    buttonLogin.visibility = View.VISIBLE
    txtForgotLogin.visibility = View.VISIBLE
    txtRegisterLogin.visibility = View.VISIBLE
    Toast.makeText(this@LoginActivity, "No internet Connection", Toast.LENGTH_SHORT)
        .show()
}
            } else {
                buttonLogin.visibility = View.VISIBLE
                txtForgotLogin.visibility = View.VISIBLE
                txtRegisterLogin.visibility = View.VISIBLE
                Toast.makeText(this@LoginActivity, "Invalid Details", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }
}

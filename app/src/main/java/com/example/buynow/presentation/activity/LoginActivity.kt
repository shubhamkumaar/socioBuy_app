package com.example.buynow.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.buynow.R
import com.example.buynow.data.model.LoginData
import com.example.buynow.presentation.LoadingDialog
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    lateinit var signInEmail: String
    lateinit var signInPassword: String
    lateinit var signInBtn: Button
    lateinit var emailEt: EditText
    lateinit var passEt: EditText

    lateinit var loadingDialog: LoadingDialog

    lateinit var emailError: TextView
    lateinit var passwordError: TextView
    private val TAG = "LoginActivity"

    private val TOKEN_EXPIRATION_MILLIS = TimeUnit.MINUTES.toMillis(1440)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity created.")

        // --- Check for existing token and its expiration before setting content view ---
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val authToken = sharedPref.getString("auth_token", null)
        val tokenTimestamp = sharedPref.getLong("auth_token_timestamp", 0L)

        Log.d(TAG, "onCreate: Retrieved authToken: $authToken")
        Log.d(TAG, "onCreate: Retrieved tokenTimestamp: $tokenTimestamp")

        if (authToken != null && tokenTimestamp != 0L) {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - tokenTimestamp

            Log.d(TAG, "onCreate: Current time: $currentTime")
            Log.d(TAG, "onCreate: Elapsed time since token issue: $elapsedTime ms")
            Log.d(TAG, "onCreate: Token expiration set to: $TOKEN_EXPIRATION_MILLIS ms")

            if (elapsedTime < TOKEN_EXPIRATION_MILLIS) {
                // Token is still valid (client-side check), navigate directly to HomeActivity
                Log.d(TAG, "onCreate: Token is valid. Redirecting to HomeActivity.")
                startActivity(Intent(this, HomeActivity::class.java))
                finish() // Finish LoginActivity
                return // Prevent further execution of onCreate
            } else {
                // Token has expired client-side, clear it and force login
                Log.d(TAG, "onCreate: Token expired. Clearing token and showing login UI.")
                clearAuthToken()
                Toast.makeText(
                    applicationContext,
                    "Session expired. Please log in again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Log.d(TAG, "onCreate: No valid token found or timestamp missing. Showing login UI.")
        }

        setContentView(R.layout.activity_login)
        initializeLoginUi() // Initialize UI elements after setContentView
    }

    private fun initializeLoginUi() {
        Log.d(TAG, "initializeLoginUi: Initializing UI elements.")
        val signUpTv = findViewById<TextView>(R.id.signUpTv)
        signInBtn = findViewById(R.id.loginBtn)
        emailEt = findViewById(R.id.emailEt)
        passEt = findViewById(R.id.PassEt)
        emailError = findViewById(R.id.emailError)
        passwordError = findViewById(R.id.passwordError)

        textAutoCheck()

        loadingDialog = LoadingDialog(this)

        signUpTv.setOnClickListener {
            intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        signInBtn.setOnClickListener {
            checkInput()
        }
    }

    private fun textAutoCheck() {
        emailEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (emailEt.text.isEmpty()) {
                    emailEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                } else if (Patterns.EMAIL_ADDRESS.matcher(emailEt.text).matches()) {
                    emailEt.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.ic_check
                        ),
                        null
                    )
                    emailError.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                emailEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (Patterns.EMAIL_ADDRESS.matcher(emailEt.text).matches()) {
                    emailEt.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.ic_check
                        ),
                        null
                    )
                    emailError.visibility = View.GONE
                }
            }
        })

        passEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (passEt.text.isEmpty()) {
                    passEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                } else if (passEt.text.length > 4) {
                    passEt.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.ic_check
                        ),
                        null
                    )
                }
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                passEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                passwordError.visibility = View.GONE
                if (count > 4) {
                    passEt.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.ic_check
                        ),
                        null
                    )
                }
            }
        })
    }

    private fun checkInput() {
        if (emailEt.text.isEmpty()) {
            emailError.visibility = View.VISIBLE
            emailError.text = "Email Can't be Empty"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailEt.text).matches()) {
            emailError.visibility = View.VISIBLE
            emailError.text = "Enter Valid Email"
            return
        }
        if (passEt.text.isEmpty()) {
            passwordError.visibility = View.VISIBLE
            passwordError.text = "Password Can't be Empty"
            return
        }

        if (passEt.text.isNotEmpty() && emailEt.text.isNotEmpty()) {
            emailError.visibility = View.GONE
            passwordError.visibility = View.GONE
            signInUser()
        }
    }

    private fun signInUser() {
        loadingDialog.startLoadingDialog()
        signInEmail = emailEt.text.toString().trim()
        signInPassword = passEt.text.toString().trim()
        Log.d(TAG, "LoginIn start")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Make the API call using RetrofitInstance and ApiInterface
                val response: LoginData = RetrofitInstance.apiInterface.loginUser(
                    username = signInEmail,
                    password = signInPassword
                )
                Log.d(TAG, response.toString())
                withContext(Dispatchers.Main) {
                    loadingDialog.dismissDialog()
                    if (response.success) {
                        Toast.makeText(applicationContext, response.message, Toast.LENGTH_SHORT)
                            .show()
                        response.accessToken?.let { token ->
                            val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("auth_token", token)
                                putString("user_email", response.email)
                                putString("user_name", response.name)
                                putString("user_phone", response.phone)
                                putLong(
                                    "auth_token_timestamp",
                                    System.currentTimeMillis()
                                )
                                apply() // Apply changes asynchronously
                            }
                            Log.d(TAG, "signInUser: Token stored successfully: $token")
                            Log.d(
                                TAG,
                                "signInUser: Timestamp stored: ${System.currentTimeMillis()}"
                            )
                            Toast.makeText(applicationContext, "Token stored!", Toast.LENGTH_SHORT)
                                .show()
                        }

                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(applicationContext, response.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismissDialog() // Dismiss dialog on error
                    Toast.makeText(
                        applicationContext,
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace() // Log the error for debugging
                }
            }
        }
    }

    private fun clearAuthToken() {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("auth_token")
            remove("auth_token_timestamp") // Also remove the timestamp
            apply()
        }
    }
}

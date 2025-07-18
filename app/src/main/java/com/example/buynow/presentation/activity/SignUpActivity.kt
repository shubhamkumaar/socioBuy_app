package com.example.buynow.presentation.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.buynow.R
import com.example.buynow.data.model.LoginData
import com.example.buynow.data.model.SignUpRequest
import com.example.buynow.utils.Extensions.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {
    private val TAG = "SignupActivity"

    private lateinit var fullName: EditText
    private lateinit var emailEt: EditText
    private lateinit var phoneIn: EditText
    private lateinit var passEt: EditText
    private lateinit var CpassEt: EditText

    lateinit var progressDialog: ProgressDialog

    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val signUpBtn = findViewById<Button>(R.id.signUpBtn_signUpPage)
        fullName = findViewById(R.id.nameEt_signUpPage)
        emailEt = findViewById(R.id.emailEt_signUpPage)
        phoneIn = findViewById(R.id.phone_signUpPage)
        passEt = findViewById(R.id.PassEt_signUpPage)
        CpassEt = findViewById(R.id.cPassEt_signUpPage)
        val signInTv = findViewById<TextView>(R.id.signInTv_signUpPage)

        progressDialog = ProgressDialog(this)

        textAutoCheck()

        signInTv.setOnClickListener {
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        signUpBtn.setOnClickListener {
            checkInput()
        }
    }

    private fun textAutoCheck() {
        fullName.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (fullName.text.isEmpty()) {
                    fullName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                } else if (fullName.text.length >= 4) {
                    fullName.setCompoundDrawablesWithIntrinsicBounds(
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
                fullName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (count >= 4) {
                    fullName.setCompoundDrawablesWithIntrinsicBounds(
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

        emailEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (emailEt.text.isEmpty()) {
                    emailEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                } else if (emailEt.text.matches(emailPattern.toRegex())) {
                    emailEt.setCompoundDrawablesWithIntrinsicBounds(
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
                emailEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (emailEt.text.matches(emailPattern.toRegex())) {
                    emailEt.setCompoundDrawablesWithIntrinsicBounds(
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

        passEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (passEt.text.isEmpty()) {
                    passEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                } else if (passEt.text.length > 5) {
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
                if (count > 5) {
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

        CpassEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                if (CpassEt.text.isEmpty()) {
                    CpassEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                } else if (CpassEt.text.toString() == passEt.text.toString()) {
                    CpassEt.setCompoundDrawablesWithIntrinsicBounds(
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
                CpassEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (CpassEt.text.toString() == passEt.text.toString()) {
                    CpassEt.setCompoundDrawablesWithIntrinsicBounds(
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
        if (fullName.text.isEmpty()) {
            toast("Name can't empty!")
            return
        }
        if (emailEt.text.isEmpty()) {
            toast("Email can't empty!")
            return
        }

        if (!emailEt.text.matches(emailPattern.toRegex())) {
            toast("Enter Valid Email")
            return
        }
        if (passEt.text.isEmpty()) {
            toast("Password can't empty!")
            return
        }
        if (passEt.text.toString() != CpassEt.text.toString()) {
            toast("Password not Match")
            return
        }

        signIn()
    }

    private fun signIn() {
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Creating Account")
        progressDialog.show()

        val emailV: String = emailEt.text.toString()
        val passV: String = passEt.text.toString()
        val phone: String = phoneIn.text.toString().trim()
        val fullname: String = fullName.text.toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: LoginData = RetrofitInstance.apiInterface.register(
                    SignUpRequest(
                        fullname,
                        emailV,
                        phone,
                        passV
                    )
                )
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
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
                            Log.d(TAG, "signInUser: Timestamp stored: ${System.currentTimeMillis()}")
                            Toast.makeText(applicationContext, "Token stored!", Toast.LENGTH_SHORT).show()
                        }

                        startActivity(Intent(this@SignUpActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(applicationContext, response.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss() // Dismiss dialog on error
                    Toast.makeText(
                        applicationContext,
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace() // Log the error for debugging
                }
            }
//        firebaseAuth.createUserWithEmailAndPassword(emailV,passV)
//
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        progressDialog.setMessage("Save User Data")
//
//
//                        val user = User(fullname,"",firebaseAuth.uid.toString(),emailV,"","")
//
//                        storeUserData(user)
//
//                        val intent = Intent(this, HomeActivity::class.java)
//                        startActivity(intent)
//                        finish()
//                    } else {
//                        progressDialog.dismiss()
//                        toast("failed to Authenticate !")
//                    }
//                }
        }

//    private fun sendEmailVerification() {
//        progressDialog.setMessage("Send Verification")
//        firebaseUser?.let {
//            it.sendEmailVerification().addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    progressDialog.dismiss()
//                    val intent = Intent(this, EmailVerifyActivity::class.java)
//                    intent.putExtra("EmailAddress", emailEt.text.toString().trim())
//                    intent.putExtra("loginPassword", passEt.text.toString().trim())
//                    startActivity(intent)
//                    finish()
//                }
//            }
//                .addOnFailureListener {
//                    progressDialog.dismiss()
//                    toast("Verification Link Send failed")
//                }
//        }
    }
}

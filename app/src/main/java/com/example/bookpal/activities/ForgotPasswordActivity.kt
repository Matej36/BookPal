package com.example.bookpal.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.bookpal.MyApplication
import com.example.bookpal.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {


    private lateinit var binding: ActivityForgotPasswordBinding


    private lateinit var firebaseAuth: FirebaseAuth


    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        binding.backFab.setOnClickListener {
            onBackPressed()
        }


        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }


    private var email = ""
    private fun validateData() {

        email = binding.emailEt.text.toString().trim()


        if (email.isEmpty()) {
            MyApplication.toast(this, "Enter email...")
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            MyApplication.toast(this, "Invalid email pattern...")
        } else {
            recoverPassword()
        }
    }

    private fun recoverPassword() {

        progressDialog.setMessage("Sending password reset instructions to $email")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {

                progressDialog.dismiss()
                MyApplication.toast(this, "Instructions sent to \n$email")
            }
            .addOnFailureListener { e ->

                progressDialog.dismiss()
                MyApplication.toast(this, "Failed to send due to ${e.message}")
            }
    }

}
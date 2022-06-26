package com.example.bookpal.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.bookpal.MyApplication
import com.example.bookpal.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySignUpBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        binding.backFab.setOnClickListener {
            onBackPressed()
        }


        binding.signUpBtn.setOnClickListener {
            validateData()
        }

    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()


        if (name.isEmpty()){
            MyApplication.toast(this, "Enter your name...")
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            MyApplication.toast(this, "Invalid Email Pattern...")
        }
        else if (password.isEmpty()){
            MyApplication.toast(this, "Enter password...")
        }
        else if (cPassword.isEmpty()){
            MyApplication.toast(this, "Confirm password...")
        }
        else if (password != cPassword){
            MyApplication.toast(this, "Password doesn't match...")
        }
        else{
            createUserAccount()
        }
    }

    private fun createUserAccount() {

        progressDialog.setMessage("Creating Account...")
        progressDialog.show()


        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                updateUserInfo()

            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                MyApplication.toast(this, "Failed creating account due to ${e.message}")
            }
    }

    private fun updateUserInfo() {


        progressDialog.setMessage("Saving user info...")


        val timestamp = System.currentTimeMillis()


        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["timestamp"] = timestamp
        hashMap["profileImage"] = ""


        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {

                progressDialog.dismiss()
                MyApplication.toast(this, "Account created...")
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                MyApplication.toast(this, "Failed saving user info due to ${e.message}")
            }
    }
}
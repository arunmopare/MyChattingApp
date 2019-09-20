package com.example.mychattingapp.registerlogin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.mychattingapp.R
import com.example.mychattingapp.messeges.LatestMessegesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener {
            perforLogin()
        }
            back_to_registration_textview.setOnClickListener {
                finish()
            }
        }
    private fun perforLogin() {
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "please enter text in email or password", Toast.LENGTH_SHORT)
                .show()
            return
        }
        Log.d("Login", "Attempt to login with email/pw: $email/***")


        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                // Log.d("Login", "Successfully logged in: ${it.result?.user?.uid}")
                val intent = Intent(this, LatestMessegesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to log in: ${it.message}", Toast.LENGTH_SHORT).show()
                //empy mail or password crash
            }
    }
}






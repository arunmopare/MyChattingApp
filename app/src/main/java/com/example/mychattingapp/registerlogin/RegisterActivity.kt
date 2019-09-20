package com.example.mychattingapp.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.mychattingapp.R
import com.example.mychattingapp.messeges.LatestMessegesActivity
import com.example.mychattingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        register_button_register.setOnClickListener {
            performRegister()
        }

        already_have_an_account_text_view.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login acivity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        selectphoto_button_register.setOnClickListener {
            Log.d("RegisterActivity", "try to show photo")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }
    }

    var selectedPhotoUri: Uri? = null;

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegistratonActivity", "Photo was seleted")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

           selectphoto_imageview_register.setImageBitmap(bitmap)
            selectphoto_button_register.alpha=0f
           // val bitmapDrawable = BitmapDrawable(bitmap)
           // selectphoto_button_register.setBackgroundDrawable(bitmapDrawable)

        }
    }

    private fun performRegister() {
        val email = email_edittext_registration.text.toString()
        val password = password_edittext_registration.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "please enter text in email or password", Toast.LENGTH_SHORT)
                .show()
            return
        }

        Log.d("RegisterActivity", "Email is :" + email)
        Log.d("RegisterActivity", "Password $password")
        //firebase authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                //else
                Log.d("RegisterActivity", "Successfully created user: ")
                uploadImageToFirebaseStorege()

            }
            .addOnFailureListener {
                Log.d("RegistrationActivity", "Failed to create new user: ${it.message}")
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorege() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegistrationActivity", "Successfully uploaded image:${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {

                    Log.d("RegisterActivity","File Location: $it" )

                    saveUserToDatabase(it.toString())
                }
            }
            .addOnFailureListener{
             //add some logging here
            }


    }

    private fun saveUserToDatabase(profileImageUrl: String){
        val uid= FirebaseAuth.getInstance().uid ?: ""
        val ref= FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(
            uid,
            username_edittext_registration.text.toString(),
            profileImageUrl
        )
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Finally Saved User To database")
                val intent = Intent( this , LatestMessegesActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)



            }
    }
}



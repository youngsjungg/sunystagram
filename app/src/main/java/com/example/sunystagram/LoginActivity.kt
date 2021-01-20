package com.example.sunystagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
    }
    fun signAndSignup(){
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(),
            password_edittext.text.toString())
            ?.addOnCompleteListener {
                task ->
                    if (task.isSuccessful){

                    }
            }
    }

}
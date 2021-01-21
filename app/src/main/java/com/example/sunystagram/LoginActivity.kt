package com.example.sunystagram

import android.content.Intent
import  androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        email_login_botton.setOnClickListener {
            signinAndSignup()
        }
    }
    fun signinAndSignup(){
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(),//이메일 입력부분
            password_edittext.text.toString())//password입력부분
            ?.addOnCompleteListener {//회원가입 결과값을 가져옴
                task ->
                    if (task.isSuccessful){//id 생성시
                        moveMainPage(task.result?.user)
                    }else if(task.exception?.message.isNullOrEmpty()){
                        //login 실패시 메시지, error
                        Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                    }else{
                       // 회원가입도 에러도 아니면 로그인으로 빠짐
                        signinEmail()
                    }
            }
    }
   //로그인
    fun signinEmail() {
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(),//이메일 입력부분
                password_edittext.text.toString())//password입력부분
                ?.addOnCompleteListener {//회원가입 결과값을 가져옴
                    task ->
                    if (task.isSuccessful){//id 생성시
                        moveMainPage(task.result?.user)

                    }else{
                        // 로그인 실패
                        Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                    }
                }
    }

    fun moveMainPage(user:FirebaseUser?){ //로그인 성공시 다음 페이지로 넘어감로
        if (user != null){
            startActivity(Intent(this, MainActivity::class.java))//firebase 유저상태를 넘기고 담 페이지로, ㅡmainactivity
        }

    }

}
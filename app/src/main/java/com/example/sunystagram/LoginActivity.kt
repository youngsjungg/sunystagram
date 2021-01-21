package com.example.sunystagram

import android.content.Intent
import  androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient?= null
    var GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        email_login_botton.setOnClickListener {
            signinAndSignup()
        }
        google_sign_in_button.setOnClickListener {
            //First
            googleLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) //663565438978-o772lrkp2tptci9gv25odlrotntg5dli.apps.googleusercontent.com
            .requestEmail() //입력한 아이디를 받아옴
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }
    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)//로그인 결과값을 받아옴
            if (result!!.isSuccess) { //.을 수정
                var account = result.signInAccount  //성공시 값을 firebase에 넘겨줌
                //second
                firebaseAuthWithGoogle(account)
            }
        }
    }
    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        //accout안 토큰 아이디를 넘겨줌
        auth?.signInWithCredential(credential)
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
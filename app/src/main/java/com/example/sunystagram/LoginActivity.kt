package com.example.sunystagram

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.GET_SIGNATURES
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient?= null
    var GOOGLE_LOGIN_CODE = 9001
    var callbackManager : CallbackManager? = null //facebook 로그인 결과를 가져옴

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
        facebook_login_button.setOnClickListener {
            //first
            facebookLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) //663565438978-o772lrkp2tptci9gv25odlrotntg5dli.apps.googleusercontent.com
            .requestEmail() //입력한 아이디를 받아옴
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
//        printHashKey()
        //YrZXZDWKpJzAppzNByEFUzWlOt8=
        callbackManager =  CallbackManager.Factory.create()

    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }
    fun printHashKey() {
        try {
            val info: PackageInfo = packageManager.getPackageInfo(packageName, GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("Tag", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("Tag", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("Tag", "printHashKey()", e)
        }
    }
    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)

    }
    fun facebookLogin(){
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile","email"))//페이스북에서 받을 권한 요청


        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {//성공시 정보를 firebase에 넘김
                    //second
                    handleFacebookAccessToken(result?.accessToken)
                }
                override fun onCancel() {

                }
                override fun onError(error: FacebookException?) {

                }//페이스북 로그인 성공시 넘어오는 부분

            })

        }

    fun handleFacebookAccessToken(token: AccessToken?) {
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {//회원가입 결과값을 가져옴
                    task ->
                if (task.isSuccessful){//id 생성시
                    //thrid
                    moveMainPage(task.result?.user)

                }else{
                    // 로그인 실패
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode,resultCode,data)//callbackmanager을 넘겨줌
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
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        //accout안 토큰 아이디를 넘겨줌
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {//회원가입 결과값을 가져옴
                task ->
                if (task.isSuccessful){//id 생성시
                    moveMainPage(task.result?.user)

                }else{
                    // 로그인 실패
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
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
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
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
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
    }

    fun moveMainPage(user: FirebaseUser?){ //로그인 성공시 다음 페이지로 넘어감로
        if (user != null){
            startActivity(Intent(this, MainActivity::class.java))//firebase 유저상태를 넘기고 담 페이지로, ㅡmainactivity
            finish()  //loginactivity가 꺼지면 mainactivity가 켜짐
        }

    }

}
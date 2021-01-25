package com.example.sunystagram

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sunystagram.navigation.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        setToolbarDefault()

        when (p0.itemId) {
            R.id.action_home -> {
                var detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, detailViewFragment).commit()
                return true
            }
            R.id.action_search -> {
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()
                return true
            }
            R.id.action_add_photo-> {
                 //호출권한을 줌
                //외부 권한이 있는지 확인
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
                     startActivity(Intent(this, AddPhotoActivity::class.java))
                }
//                else{
//                    Toast.makeText(this, "스토리지 읽기 권한이 없습니다. ",Toast.LENGTH_LONG).show()
//                }
                return true
            }
            R.id.action_favorite_alarm -> {
                var alarmFragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, alarmFragment).commit()
                return true
            }
            R.id.action_account -> {
                var userFragment = UserFragment()

                //uid 넘겨주기
                var bundle = Bundle()
                var uid  = FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("destinationUid",uid)
                userFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.main_content, userFragment).commit()
                return true
            }

        }
        return false //어떤 조건도 만족하지 않을때

    }
    fun  setToolbarDefault(){
        toolbar_username.visibility = View.GONE
        toolbar_btn_back.visibility = View.GONE
        toolbar_title_image.visibility = View.VISIBLE

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        //사진을 가져올 수 있는 권한
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

        //set default screen
        bottom_navigation.selectedItemId = R.id.action_home
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //사진선택 시 처리
        if (requestCode == UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var storageRef = FirebaseStorage.getInstance().reference.child("userprofileImages").child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask {
                return@continueWithTask storageRef.downloadUrl
            }.addOnCompleteListener { uri -> //userprofile 을 선택 , database에 업로드
                var map = HashMap<String, Any>()
                map["image"] = uri.toString()
                FirebaseFirestore.getInstance().collection("profileimages").document(uid).set(map)

            }

        }
    }

}
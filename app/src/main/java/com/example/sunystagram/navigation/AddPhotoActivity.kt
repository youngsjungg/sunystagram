 package com.example.sunystagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bolts.Task
import com.example.sunystagram.R
import com.example.sunystagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*


 class  AddPhotoActivity : AppCompatActivity() {
     var PICK_IMAGE_FROM_ALBUM = 0
     var storage : FirebaseStorage? = null
     var photoUri : Uri? = null
     var auth : FirebaseAuth? = null
     var firestore : FirebaseFirestore?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //스토리지 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //실행시 바로 화면 열림
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        //버튼 이벤트
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }
     //선택한 이미지를 받음
     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (requestCode == PICK_IMAGE_FROM_ALBUM){
             if (resultCode == Activity.RESULT_OK){
                 //결과가 사진을 선택 = resultok일때 넘어오는 이미지의 경로
                 photoUri = data?.data
                 addphoto_image.setImageURI(photoUri)

             }else{
                 // 취소를 눌렀을 때
                 finish()
             }
         }
     }

     fun contentUpload(){
         //파일 이름을 만들어줌 중복 값이 없도록 날짜로 만듬
         var timestamp  = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
         var imageFileName = "IMAGE_" + timestamp + "_.png "

         //이미지 업로드  폴더명 :images
         var storageRef = storage?.reference?.child("images")?.child(imageFileName)
         //             Toast.makeText(this,getString(R.string .upload_success),Toast.LENGTH_LONG).show() //결과 성공시 메시지


         //Primise method
         storageRef?.putFile(photoUri!!)?.continueWithTask { task: com.google.android.gms.tasks.Task<UploadTask.TaskSnapshot> ->
             return@continueWithTask storageRef.downloadUrl
         }?.addOnSuccessListener { uri ->
             var contentDTO = ContentDTO()

             contentDTO.imageUrl = uri.toString()
//
                 contentDTO.uid = auth?.currentUser?.uid

                 contentDTO.UserId = auth?.currentUser?.email

                 contentDTO.explain = addphoto_edit_explain.text.toString()

                 contentDTO.timestamp  = System.currentTimeMillis()

                 firestore?.collection("images")?.document()?.set(contentDTO)

                 setResult(Activity.RESULT_OK)

                finish()
         }


         //Callback Method
//         storageRef?.putFile(photoUri!!)?.addOnSuccessListener {


//             storageRef.downloadUrl.addOnSuccessListener { uri ->
//                 var contentDTO = ContentDTO()
//
//                 //insert down of image
//                 contentDTO.imageUrl = uri.toString()
//
//                 contentDTO.uid = auth?.currentUser?.uid
//
//                 contentDTO.UserId = auth?.currentUser?.email
//
//                 contentDTO.explain = addphoto_edit_explain.text.toString()
//
//                 contentDTO.timestamp  = System.currentTimeMillis()
//
//                 firestore?.collection("images")?.document()?.set(contentDTO)
//
//                 setResult(Activity.RESULT_OK)
//
//             }

         }

     }


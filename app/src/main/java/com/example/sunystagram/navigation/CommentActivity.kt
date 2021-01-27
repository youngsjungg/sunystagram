package com.example.sunystagram.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sunystagram.R
import com.example.sunystagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {
    var contentUid :String?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        contentUid = intent.getStringExtra("contentUid")

        comment_btn_send.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.uerId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_edit_message.text.toString()//보낼 메세지 입력
            comment.timestamp =System.currentTimeMillis() //현재 시간 입력

            //디비에 넣기
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comments").document().set(comment)

            comment_edit_message.setText("") //초기화

        }
    }
}
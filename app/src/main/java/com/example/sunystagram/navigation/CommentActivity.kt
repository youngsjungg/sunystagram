package com.example.sunystagram.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.sunystagram.R
import com.example.sunystagram.navigation.model.AlarmDTO
import com.example.sunystagram.navigation.model.ContentDTO
import com.google.api.Billing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentActivity<view> : AppCompatActivity() {
    var contentUid: String? = null
    var destinnationUid :String?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        contentUid = intent.getStringExtra("contentUid")
        destinnationUid = intent.getStringExtra("destinnationUid")

        comment_recyclerview.adapter = CommentRecyclerViewAdpter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)

        comment_btn_send.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.uerId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_edit_message.text.toString()//보낼 메세지 입력
            comment.timestamp = System.currentTimeMillis() //현재 시간 입력

            //디비에 넣기
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                    .collection("comments").document().set(comment)
            commentAlarm(destinnationUid!!,comment_edit_message.text.toString())
            comment_edit_message.setText("")

            comment_edit_message.setText("") //초기화

        }
    }
    fun commentAlarm(destinnationUid: String?, message: String) {
        var  alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinnationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }


    //입력된 메시지를 출력하는 RecyclerView
    inner class CommentRecyclerViewAdpter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var comments: ArrayList<ContentDTO.Comment> = arrayListOf()//초기화

        init {
            FirebaseFirestore.getInstance()     //firebase 데이터를 읽어옴
                    .collection("images")
                    .document(contentUid!!)
                    .collection("comments")
                    .orderBy("timestamp")    //시간순으로 읽어옴
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        comments.clear()    //중복시
                        if (querySnapshot == null) return@addSnapshotListener    //null이면 return

                        for (snapshot in querySnapshot.documents!!) {
                            comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                        }
                        notifyDataSetChanged()

                    }

        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_comment, p0, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) //RecyclerView.ViewHolder상속


        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var view = p0.itemView//서버에서 온 아이디랑 메시지 매핑
            view.commentviewitem_imageview_comment.text = comments[p1].comment
            view.commentviewitem_textview_profile.text = comments[p1].uerId

            //프로필 사진 매핑
            FirebaseFirestore.getInstance()
                    .collection("profileImages")
                    .document(comments[p1].uid!!)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            var uri = task.result!!["image"]
                            Glide.with(p0.itemView.context).load(uri).apply(RequestOptions().circleCrop())
                                    .into(view.commentviewitem_imageview_profile)
                        }

//uri변수에 이미지 주소 받아옴

                    }
        }
    }
}
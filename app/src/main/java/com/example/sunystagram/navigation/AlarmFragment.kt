package com.example.sunystagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.sunystagram.R
import com.example.sunystagram.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm, container, false)
        view.alarmfragment_recyclerview.adapter = AlarmRecyclerviewAdapter()
        view.alarmfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var alarmDTOList: ArrayList<AlarmDTO> = arrayListOf()//알람저장list

        init {
            var uid = FirebaseAuth.getInstance().currentUser?.uid

            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                alarmDTOList.clear()
                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents){
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                    notifyDataSetChanged()

            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1e: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_comment, p0, false)//재활용

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var view = p0.itemView //종류에 따라 메시지를 다르게 표시하기

            //상대방 이미지 주소 받아오기
            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[p1].uid!!).get().addOnCompleteListener {task->
                var  url = task.result!!["image"]
                Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewitem_imageview_profile)

            }

            when (alarmDTOList[p1].kind) {
                0 -> { //좋아여 이벤트 알람
                    var str_0 = alarmDTOList[p1].userId + getString(R.string.alarm_favorite) //    alaremDTOList에서 userid를 받아옴
                    view.commentviewitem_textview_profile.text = str_0 //좋아여 알람
                }
                1 -> { //comment 이벤트 알람
                    var str_0 = alarmDTOList[p1].userId + ""+getString(R.string.alarm_comment) +"of"+alarmDTOList[p1].message//    alaremDTOList에서 userid를 받아옴
                    view.commentviewitem_textview_profile.text = str_0     //comment 알람

                }
                2 -> { //follow 이벤트 알람
                    var str_0 = alarmDTOList[p1].userId +""+ getString(R.string.alarm_follow) //    alaremDTOList에서 userid를 받아옴
                    view.commentviewitem_textview_profile.text = str_0     //follow  알람


                }
            }
            view.commentviewitem_imageview_comment.visibility= View.INVISIBLE

        }
    }

}
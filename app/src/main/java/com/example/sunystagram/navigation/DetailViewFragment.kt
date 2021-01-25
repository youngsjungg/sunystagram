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
import com.example.sunystagram.R
import com.example.sunystagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var uid : String?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)


        return view
    }
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contntUidLIst: ArrayList<String> = arrayListOf()

        init { //시간순으로 받아옴

            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear() //초기화
                contntUidLIst.clear() //초기화
                if (querySnapshot == null)
                    return@addSnapshotListener
                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)//contentDTO 방식으로 캐스팅
                    contentDTOs.add(item!!)
                    contntUidLIst.add(snapshot.id)
                }

                notifyDataSetChanged()//값 새로고침
            }
        }

            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
               var  view = LayoutInflater.from(p0.context).inflate(R.layout.item_detail,p0,false)
                return CustomViewHolder(view)
            }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        }
        override fun getItemCount(): Int {

               return contentDTOs.size // 넘겨줌
             }

            override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
                var  viewHolder = (p0 as CustomViewHolder).itemView   //서버에서 넘어온 데이터 매핑

                //Userid
                viewHolder.detailviewitem_profile_textview.text = contentDTOs!![p1].UserId

                //Image uri주소 받아옴
               Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl).into(viewHolder.detailviewitem_imageview_content)

                //설명글 매핑    Explain of content
                viewHolder.detailviewitem_explain_textview.text = contentDTOs!![p1].explain

                //likes
                viewHolder.detailviewitem_favoritecount_textview.text = "Likes" + contentDTOs!![p1].favoriteCount

                //ProfileImage
                Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl).into(viewHolder.detailviewitem_profile_image)

                //button is clicked
                viewHolder.detailviewitem_favorite_imageview.setOnClickListener {
                    favoriteEvent(p1)
                }
                //하트 채워지게
                if (contentDTOs!![p1].favorites.containsKey(uid)) { //uid 포함시 = 좋아요 클릭시
                    viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
                }else{ //반대
                    viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)

                }

                //profileimage 이미지 누르면 상대방 유저 정보로 이동
                viewHolder.detailviewitem_profile_image.setOnClickListener {
                    var fragment = UserFragment()
                    var bundle = Bundle()
                    bundle.putString("destinationUid",contentDTOs[p1].uid)
                    bundle.putString("userId",contentDTOs[p1].UserId)
                    fragment.arguments = bundle
                    activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
                }
             }
             //선택한 이미지의 uid를 받아와 좋아요
                fun favoriteEvent(position : Int) {
                    var tsDoc = firestore?.collection("images")?.document(contntUidLIst[position])
                    firestore?.runTransaction {
                        transaction ->
                        var uid = FirebaseAuth.getInstance().currentUser?.uid
                        var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)//dto로 캐스팅

                        //이미 좋아요가 클릭시,  아닐시
                        if (contentDTO!!.favorites.containsKey(uid)){
                            //눌림 ,눌린거 취소
                            contentDTO?.favoriteCount = contentDTO?.favoriteCount -1
                            contentDTO?.favorites.remove(uid)
                        } else{//안눌림 클릭 이벤트
                            contentDTO?.favoriteCount = contentDTO?.favoriteCount +1
                            contentDTO?.favorites[uid!!] = true


                        }

                        transaction.set(tsDoc,contentDTO)

                    }
                }



        }
    }
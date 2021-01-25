package com.example.sunystagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.sunystagram.LoginActivity
import com.example.sunystagram.MainActivity
import com.example.sunystagram.R
import com.example.sunystagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*


class   UserFragment : Fragment() {
    var fragmentView : View?= null
    var firestore : FirebaseFirestore? = null
    var uid : String?= null
    var auth : FirebaseAuth?=  null
    var currentUserUid : String?= null
    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        uid = arguments?.getString("destinationUid")//이전에서 넘어온 값을 받아옴
        firestore = FirebaseFirestore.getInstance()//초기화
        auth = FirebaseAuth.getInstance() //초기화
        currentUserUid = auth?.currentUser?.uid

        if (uid == currentUserUid){
            //my page
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity,LoginActivity::class.java))
                auth?.signOut() //firebase에 보냄

            }
        }else{
            //OtherUserPage
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity)
            mainactivity?.toolbar_username?.text = arguments?.getString("userId")

            //뒤로가기 Event
            mainactivity?.toolbar_btn_back?.setOnClickListener {
                mainactivity.bottom_navigation.selectedItemId = R.id.action_home
            }
                //이미지뷰 로고 숨김
                mainactivity?.toolbar_title_image?.visibility = View.GONE
                mainactivity?.toolbar_username?.visibility = View.VISIBLE
                mainactivity?.toolbar_btn_back?.visibility = View.VISIBLE
        }



        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!,3) //칸에 3개씩 뜨도록

        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
        }
        getProfileimage()
        return fragmentView
    }
    //올린 이미지를 다운받음
    fun  getProfileimage(){
        firestore?.collection("prifileimage")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (documentSnapshot ==null ) return@addSnapshotListener //코드 안전성을 위함
            if (documentSnapshot.data != null) { // 값이 있으면 이미지주소를 받아옴
                var url = documentSnapshot?.data!!["image"]
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.account_iv_profile!!) //이미지 다운
            }
         }
    }
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            //내 uid만 검색
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener { querySnapshot, firebaseFirestore ->
                if (querySnapshot ==null)//안전성을 위해 querySnapshot가 null값일시 바로 종료
                    return@addSnapshotListener
                for (snapshot in querySnapshot.documents){ //아니면 데이터를 담기
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!) //!! = nullsafety를 제거
                }
                fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                notifyDataSetChanged()  //recyclerview 새로고침

            }
        }
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var  width =resources.displayMetrics.widthPixels / 3  //화면 폭 가져오기 폭의 1/3 가져와서 이미지뷰에  넣어주기


            var imageview = ImageView(p0.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHolder(imageview)

        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }

        override fun getItemCount(): Int {
            return contentDTOs.size

        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var imageview = (p0 as CustomViewHolder).imageview
            Glide.with(p0.itemView.context).load(contentDTOs[p1].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }

    }
}


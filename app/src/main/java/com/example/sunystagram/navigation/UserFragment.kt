package com.example.sunystagram.navigation

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
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
import com.example.sunystagram.navigation.model.FollowDTO
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
            fragmentView ?.account_btn_follow_signout?.setOnClickListener {
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
                fragmentView?.account_btn_follow_signout?.setOnClickListener {
                    requestFollow()
                }
        }

        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!,3) //칸에 3개씩 뜨도록

        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
        }
        getProfileimage()
        getFollowerAndFollowing()
        return fragmentView
    }
    fun getFollowerAndFollowing(){//화면에 카운터 변환하기

        //내 uid은 내 uid, 상대방 uid클릭은 상대 uid
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (documentSnapshot ==null)return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if (followDTO?.followingCount != null) {
            //조건 충족시 값 출력
                fragmentView?.account_tv_follower_count?.text = followDTO?.followingCount?.toString()
            }
            if (followDTO?.followerCount != null) {
                //조건 충족시 값 출력
                fragmentView?.account_tv_follower_count?.text = followDTO?.followerCount?.toString()
                if (followDTO?.followers?.containsKey(currentUserUid!!)){//내가 팔로워시 버튼변환
                 fragmentView?.account_btn_follow_signout?.text =getString(R.string.follow_cancel)//내 uid가 있을 경우
                 fragmentView?.account_btn_follow_signout?.background?.setcolorFilter(ContextCompat.getColor(activity!!, R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)

                }else{
                    if (uid != currentUserUid){
                        fragmentView?.account_btn_follow_signout?.text =getString(R.string.follow) //내 uid가 없을 경우
                        fragmentView?.account_btn_follow_signout?.background?.colorFilter =null
                    }

                }
            }
        }



    }
    fun requestFollow() {
        var tsDocFollowing = firestore!!.collection("users").document(currentUserUid!!) //내가 상대방 누가 팔로우하는지
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[uid!!] = true  //중복 팔로우 방지

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction //데이터 모델을 넣어주면 데이터가 db에 담김
            }
            if (followDTO.followings.containsKey(uid)) {//팔로우 한 상태 : 팔로잉취소
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followers?.remove(uid)

            } else {//팔로잉
                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO?.followers[uid!!] = true
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction

        }
        //상대방 계정에 타인이 팔로우
        var  tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
           var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java) //followDTO값을 읽어옴

            if (followDTO == null){//값이 없으면 데이터 모델을 만듬 최초값이라 1
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true//상대방 계정에 내 uid를 넣어줌

                transaction.set(tsDocFollower,followDTO!!)//DB에 값을 넣어줌
                return@runTransaction

            }
            //아닐경우
             if (followDTO!!.followers.containsKey(currentUserUid)) {//상대방계정에 내가 팔로우 했을 때
               followDTO!!.followerCount=followDTO!!.followerCount -1  //팔로우 취소 코드
               followDTO!!.followers.remove(currentUserUid!!)  //내 uid 제거
             }else{//팔로우를 안했을 경우
                 followDTO!!.followerCount=followDTO!!.followerCount +1
                 followDTO!!.followers[currentUserUid!!]= true//나의 uid 추가
             }
              transaction.set(tsDocFollower,followDTO!!)//DB에 값 저장
              return@runTransaction

        }

    }


    //올린 이미지를 다운받음
    fun  getProfileimage(){
        firestore?.collection("profileimages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
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


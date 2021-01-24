package com.example.sunystagram.navigation.model

data class ContentDTO  ( var explain : String? = null,//content 설명 관리
                    var imageUrl : String? = null,//이미지 관리
                    var uid : String?= null,//어느 유저가 올렸는지 관리
                    var UserId : String?= null,//올린 유저의 이미지 관리
                    var timestamp : Long?= null, //언제 컨텐츠를 올렷는지 관리
                    var favoriteCount : Int =0 ,//좋아요 관리
                    var favorites :MutableMap<String,Boolean> = HashMap())//중복 좋아요 방지  {

{
    data class Comment(
        var uid: String? = null,
        var uerId: String? = null,
        var comment: String? = null,
        var timestamp: Long? = null
    )
}









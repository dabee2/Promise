package com.dabee.promise

//리사이클러 뷰가 보여줄 Item 1개 뷰안에 보여줄 값들을 저장할 데이터용 클래스
// 내용이 없다면 클래스의 {}도 생략가능함.
// data class : 일반 class 와 다르게 자동으로 .equals() 메소드를 오버라이드 하여
//              객체간에 주소값을 비교하지 않고 멤버값을 비교해주도록 설계되는 특별한 class
data class Item constructor(var title:String, var place:String, var time:String, var groupName:String,var setLineup:String)
data class PromiseItem constructor(var title:String, var place:String,var date:String, var time:String, var note:String, var groupName: String, var setLineup:String)
data class FriendsItem constructor(var name:String, var img:String, var id:String)
data class FriendsItem2 constructor(var name:String, var img:String, var id:String, var isJoin:Boolean)
data class GroupsItem constructor(var groupName:String)
data class GroupsItem2 constructor(var groupName:String, var groupId:String)
data class Memo constructor(var name:String, var img:String, var memo:String,var date:String, var groupName:String,var promiseName:String,var userId:String ,var time:String)
data class LatLon constructor(var lat:String, var lon:String,var userId:String)

data class UserCheckStatus(val position: Int, var isChecked: Boolean)
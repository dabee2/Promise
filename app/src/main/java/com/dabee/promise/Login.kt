package com.dabee.promise

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.dabee.promise.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import com.kakao.util.maps.helper.Utility
import java.text.SimpleDateFormat
import java.util.*

class Login : AppCompatActivity() {

//    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater)}
    lateinit var binding: ActivityLoginBinding

    var isData:Boolean= false
    var isKaKaologin:Boolean= false
    var isImgChange:Boolean= false


    lateinit var imgUri:Uri
    lateinit var imgUrl:String
    lateinit var userId:String
    lateinit var userName:String
    lateinit var userEmail:String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 다크모드제거
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        Log.d(TAG, "keyhash : ${Utility.getKeyHash(this)}")
        KakaoSdk.init(this, "a885b205f3f68ff7b70039992bb3ba34")


        loadData()

        if (isData) {
            binding.tvNickname.setText(userName)
            Glide.with(this).load(imgUrl).error(R.drawable.images).into(binding.civProfile)
        }else{
            // 로그인버튼 비활성
            binding.btn.visibility =View.INVISIBLE
        }







        // 최종 로그인 버튼 리스너
        binding.btn.setOnClickListener {

            saveData()
            friendLoad()
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 카카오 로그인 리스너
        binding.ivKakao.setOnClickListener {  login()   }

        // 프로필이미지 클릭리스너
        binding.civProfile.setOnClickListener{
            if (isKaKaologin or isData){
                //갤러리 or 사진 앱을 실행
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                resultLauncher.launch(intent)
            }



        }



    }// onCreate

    //SharedPreference에 저장되어 있는 닉네임과 프로필url 읽어오는 기능
    private fun loadData() {
        val pref = getSharedPreferences("account", MODE_PRIVATE)
        userName= pref.getString("userName", null).toString()
        imgUrl = pref.getString("userImgUrl", null).toString()
        userId = pref.getString("userId", null).toString()
        userEmail = pref.getString("userEmail", null).toString()
        isData= pref.getBoolean("isData",false)
    }


    private fun saveData(){
        userName = binding.tvNickname.text.toString()


        //1. 서버 Firebase Firestore Database 에 닉네임과 프로필 url을 저장
        val firebaseFirestore = FirebaseFirestore.getInstance()
        // 'profiles' 라는 이름의 Collection 참조객체 (없으면 생성, 있으면 참조)
        val userRef = firebaseFirestore.collection("users")

        // 유저 고유 ID 생성
//        if(!isData) userId = userRef.document().id


        //우선 이미지 파일 Firebase Storage(저장소)에 업로드부터 해야함.
        //서버에 저장될 파일명이 중복되지 않도록 날짜를 이용하기

        //우선 이미지 파일 Firebase Storage(저장소)에 업로드부터 해야함.
        //서버에 저장될 파일명이 중복되지 않도록 날짜를 이용하기
        val sdf = SimpleDateFormat("yyyyMMddhhmmss")
        val fileName = sdf.format(Date()) + ".png"


        //Firebase Cloud Storage에 파일 업로드
        val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
        val imgRef: StorageReference = firebaseStorage.getReference("profile/$userId/${userId}.png")




        fun saveUserData(){
            //유저 정보 DB저장
            var profile: MutableMap<String, String> = HashMap()
            profile["userName"] = userName
            profile["userImgUrl"] = imgUrl
            profile["userId"] = userId
            profile["userEmail"] =userEmail
            userRef.document(userId).update(profile as MutableMap<String, Any>)
//            if(!isData){
//                userRef.document(userId).set(profile)
//            }else


            // 앱을 처음 실행할때 한번만 닉네임과 사진을 입력하도록 phone 에 닉네임과 프로필 url을 저장
            //2. SharedPreferences 에 저장
            val pref = getSharedPreferences("account", MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString("userName", userName)
            editor.putString("userImgUrl", imgUrl)
            editor.putString("userId", userId)
            editor.putString("userEmail", userEmail)
            editor.putBoolean("isData",true)
            editor.commit()

        }



        //이미지파일이 변경되었으면 파이어 Storage 업로드
        if(isImgChange){
           imgRef.putFile(imgUri).addOnSuccessListener {

                //업로드된 파일의 [다운로드 URL]을 얻어오기(서버에 있는 이미지의 인터넷경로 URL)
                imgRef.downloadUrl.addOnSuccessListener { uri -> //파라미터 uri : 다운로드 URL
                    // 다운로드 URL을 문자열로 변환하여 얻어오기
                    imgUrl= uri.toString()
//                Toast.makeText(this, "프로필 이미지 저장 완료${imgUrl}", Toast.LENGTH_SHORT).show()

                  saveUserData()
                }
            }

        }else{ // 이미지 변경 안되었으면 바로 저장
            saveUserData()
        }



    }



    var resultLauncher =
        registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_CANCELED) {

                imgUri = result.data!!.data!!
                isImgChange=true

                Glide.with(this).load(imgUri).error(R.drawable.images).into(binding.civProfile)
            }
        }


    fun login(){

        // 권장 : 카카오톡 로그인을 먼저 시도하고 불가능할 경우 카카오계정 로그인을 시도.

        // 로그인 요청결과에 반응하는 콜백함수
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if(token != null){
//                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                loadUserInfo() //사용자 정보 읽어오기
            }
        }

        // 디바이스에 카톡이 설치되어 있는지 확인..
        if( UserApiClient.instance.isKakaoTalkLoginAvailable(this) ){

            //카카오톡 로그인요청
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)

        }else{

            //카카오계정 로그인요청
            UserApiClient.instance.loginWithKakaoAccount(this, callback= callback)

        }

    }

    // 로그인 성공했을때 사용자 정보 얻어오기는 기능메소드
    fun loadUserInfo(){

        UserApiClient.instance.me { user, error ->
            if( user != null ){
                Log.e(TAG, "사용자 정보 요청 성공 : $user")
                binding.tvNickname.setText(user.kakaoAccount?.profile?.nickname)
                Glide.with(this).load(user.kakaoAccount?.profile?.profileImageUrl).error(R.drawable.images).into(binding.civProfile)
                userEmail = user.kakaoAccount?.email.toString()

                userId= user.id.toString()

                imgUrl = user.kakaoAccount?.profile?.profileImageUrl.toString()
                isKaKaologin=true
                binding.btn.visibility =View.VISIBLE

            }else if(error != null) Log.e(TAG, "사용자 정보 요청 실패 $error")
        }

    }




    private fun friendLoad(){
        // 데이터베이스에서 내정보 불러오기
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val userRef = firebaseFirestore.collection("users")
        val pref = this.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()


        userRef.document(userId).collection("friends").get().addOnSuccessListener { result ->

            for (document in result){

                // 데이터가 바뀐 친구 갱신신
                var code = document["id"]as String
                userRef.document(code).get().addOnSuccessListener {
                    userRef.document(userId).collection("friends").document(it.get("userId") as String).set(it)
                }


            }

        }


    }

//    fun login(){
//
//        // 이메일 로그인 콜백
//        val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
//            if (error != null) {
//                Log.e(TAG, "로그인 실패 $error")
//            } else if (token != null) {
//                Log.e(TAG, "로그인 성공 ${token.accessToken}")
//            }
//        }
//
//
//
//        // 카카오톡 설치 확인
//        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
//            // 카카오톡 로그인
//            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
//                // 로그인 실패 부분
//                if (error != null) {
//                    Log.e(TAG, "로그인 실패 $error")
//                    // 사용자가 취소
//                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled ) {
//                        return@loginWithKakaoTalk
//                    }
//                    // 다른 오류
//                    else {
//                        UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallback) // 카카오 이메일 로그인
//                    }
//                }// 로그인 성공 부분
//                else if (token != null) {
//                    Log.e(TAG, "로그인 성공 ${token.accessToken}")
//                }
//            }
//        } else {
//            UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallback) // 카카오 이메일 로그인
//        }
//        if (AuthApiClient.instance.hasToken()) {
//            UserApiClient.instance.accessTokenInfo { _, error ->
//                if (error != null) {
//                    if (error is KakaoSdkError && error.isInvalidTokenError() == true) {
//                        //로그인 필요
//                    }
//                    else {
//                        //기타 에러
//                    }
//                }
//                else {
//                    //토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
//                }
//            }
//        }
//        else {
//            //로그인 필요
//
//        }
//        UserApiClient.instance.me { user, error ->
//            if (error != null) {
//                Log.e(TAG, "사용자 정보 요청 실패 $error")
//            } else if (user != null) {
//                Log.e(TAG, "사용자 정보 요청 성공 : $user")
//                binding.tvNickname.setText(user.kakaoAccount?.profile?.nickname)
//                Glide.with(this).load(user.kakaoAccount?.profile?.profileImageUrl).error(R.drawable.images).into(binding.civProfile)
//                userEmail = user.kakaoAccount?.email.toString()
//
//
//                imgUrl = user.kakaoAccount?.profile?.profileImageUrl.toString()
//                isKaKaologin=true
//                binding.btn.visibility =View.VISIBLE
//            }
//        }
//    }
//



}
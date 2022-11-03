package com.dabee.promise

import android.content.ContentValues.TAG
import android.content.Intent
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
import java.text.SimpleDateFormat
import java.util.*

class Login : AppCompatActivity() {

//    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater)}
    lateinit var binding: ActivityLoginBinding

    var islogin:Boolean= false
    lateinit var imgUri:Uri
    lateinit var userId:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 다크모드제거
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 로그인버튼 비활성
        binding.btn.visibility =View.INVISIBLE


//        Log.d(TAG, "keyhash : ${Utility.getKeyHash(this)}")
        KakaoSdk.init(this, "a885b205f3f68ff7b70039992bb3ba34")

        // 최종 로그인 버튼 리스너
        binding.btn.setOnClickListener {

            saveData()
//
//            val intent = Intent(this,MainActivity::class.java)
//            startActivity(intent)
//            finish()
        }

        // 카카오 로그인 리스너
        binding.ivKakao.setOnClickListener {  login()   }

        // 프로필이미지 클릭리스너
        binding.civProfile.setOnClickListener{
            if (islogin==true){
                //갤러리 or 사진 앱을 실행
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                resultLauncher.launch(intent)
            }

        }



    }// onCreate

    private fun saveData(){
        var userName:String=binding.tvNickname.text.toString()

        //우선 이미지 파일 Firebase Storage(저장소)에 업로드부터 해야함.
        //서버에 저장될 파일명이 중복되지 않도록 날짜를 이용하기

        //우선 이미지 파일 Firebase Storage(저장소)에 업로드부터 해야함.
        //서버에 저장될 파일명이 중복되지 않도록 날짜를 이용하기
        val sdf = SimpleDateFormat("yyyyMMddhhmmss")
        val fileName = sdf.format(Date()) + ".png"

        //Firebase Cloud Storage에 파일 업로드

        //Firebase Cloud Storage에 파일 업로드
        val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
        val imgRef: StorageReference = firebaseStorage.getReference("profile/IMG_$fileName")

        //이미지파일 업로드

        //이미지파일 업로드
        imgRef.putFile(imgUri).addOnSuccessListener {
            //업로드에 성공하였으니
            Toast.makeText(this, "s", Toast.LENGTH_SHORT).show()
            //업로드된 파일의 [다운로드 URL]을 얻어오기(서버에 있는 이미지의 인터넷경로 URL)
            imgRef.downloadUrl.addOnSuccessListener { uri -> //파라미터 uri : 다운로드 URL
                // 다운로드 URL을 문자열로 변환하여 얻어오기
                val imgUrl= uri.toString()
                Toast.makeText(this, "프로필 이미지 저장 완료${imgUrl}", Toast.LENGTH_SHORT).show()

                //1. 서버 Firebase Firestore Database 에 닉네임과 프로필 url을 저장
                val firebaseFirestore = FirebaseFirestore.getInstance()
                // 'profiles' 라는 이름의 Collection 참조객체 (없으면 생성, 있으면 참조)
                val userRef = firebaseFirestore.collection("users")

                userId = userRef.document().id

                //유저 정보 DB저장
                val profile: MutableMap<String, String> = HashMap()
                profile["userName"] = userName
                profile["userimgUrl"] = imgUrl
                profile["userimgUri"] = imgUri.toString()
                profile["userid"] = userId
//                profile["userEmail"] =

                userRef.document(userId).set(profile)

                // 앱을 처음 실행할때 한번만 닉네임과 사진을 입력하도록 phone 에 닉네임과 프로필 url을 저장
                //2. SharedPreferences 에 저장
                val pref = getSharedPreferences("account", MODE_PRIVATE)
                val editor = pref.edit()
                editor.putString("nickName", userName)
                editor.putString("profileUrl", imgUrl)
                editor.putString("userId", userId)
//                editor.putString("userId", imgUrl)
                editor.commit()

            }
        }


    }



    var resultLauncher =
        registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_CANCELED) {

                imgUri = result.data!!.data!!

                Glide.with(this).load(imgUri).error(R.drawable.images).into(binding.civProfile)
            }
        }


    fun login(){
        // 카카오톡 설치 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            // 카카오톡 로그인
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                // 로그인 실패 부분
                if (error != null) {
                    Log.e(TAG, "로그인 실패 $error")
                    // 사용자가 취소
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled ) {
                        return@loginWithKakaoTalk
                    }
                    // 다른 오류
                    else {
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallback) // 카카오 이메일 로그인
                    }
                }
                // 로그인 성공 부분
                else if (token != null) {
                    Log.e(TAG, "로그인 성공 ${token.accessToken}")
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallback) // 카카오 이메일 로그인
        }


        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error != null) {
                    if (error is KakaoSdkError && error.isInvalidTokenError() == true) {
                        //로그인 필요
                    }
                    else {
                        //기타 에러
                    }
                }
                else {
                    //토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
                }
            }
        }
        else {
            //로그인 필요
        }

        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패 $error")
            } else if (user != null) {
                Log.e(TAG, "사용자 정보 요청 성공 : $user")
                binding.tvNickname.setText(user.kakaoAccount?.profile?.nickname)
                Glide.with(this).load(user.kakaoAccount?.profile?.profileImageUrl).error(R.drawable.images).into(binding.civProfile)

                islogin=true
                binding.btn.visibility =View.VISIBLE
            }
        }
    }


    // 이메일 로그인 콜백
    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "로그인 실패 $error")
        } else if (token != null) {
            Log.e(TAG, "로그인 성공 ${token.accessToken}")
        }
    }




}
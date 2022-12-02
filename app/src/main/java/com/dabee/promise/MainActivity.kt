package com.dabee.promise

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dabee.promise.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kakao.sdk.auth.network.AccessTokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater)}
    var promiseItems:MutableList<Item8> = mutableListOf()
    var memoryItems:MutableList<Item8> = mutableListOf()
    val firebaseFirestore = FirebaseFirestore.getInstance()
    val userRef = firebaseFirestore.collection("users")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)





        val bnv_main = binding.bnv
        val myAnim :Animation = AnimationUtils.loadAnimation(this, R.anim.rotate_open);
        supportFragmentManager.beginTransaction().add(R.id.fragment, MyFragment(promiseItems,memoryItems)).commit()



        val ss = "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidLandFcst?serviceKey=Zb2rfa2mmu%2BbKTIpNHoc4ao2gs09wedtsqFnGyAzTeFcRsbBPYaiLzCrVD6El0paOABWq5%2FFuVfwFpls8uns2Q%3D%3D&numOfRows=10&pageNo=1&dataType=JSON&regId=11B00000&tmFc=202211300600"



        // Retrofit을 이용하여 HTTP 통신 시작
        val retrofit:Retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/1360000/MidFcstInfoService/")
            .addConverterFactory(ScalarsConverterFactory.create())  // 순서 중요 Scalars 먼저!
            .build()

        val retrofitService = retrofit.create(RetrofitService::class.java)
        val serviceKey = "Zb2rfa2mmu+bKTIpNHoc4ao2gs09wedtsqFnGyAzTeFcRsbBPYaiLzCrVD6El0paOABWq5/FuVfwFpls8uns2Q=="
        val regId = "11B00000"
        val dataType= "JSON"
        var tmFc = "202212020600"

        retrofitService.searchData(serviceKey, dataType, regId, tmFc).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                val apiResponse: String? = response.body()

                Toast.makeText(this@MainActivity, "${apiResponse}", Toast.LENGTH_SHORT).show()
                AlertDialog.Builder(this@MainActivity).setMessage("${apiResponse}").show()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@MainActivity, "error : ${t.message}", Toast.LENGTH_SHORT).show()
                AlertDialog.Builder(this@MainActivity).setMessage("error : ${t.message}").show()
            }

        })



        bnv_main.setOnItemSelectedListener { item ->


            changeFragment(
                when (item.itemId) {
                    R.id.menu_bnv_group -> {
                        binding.fab.supportBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity,R.color.my_color))
                        binding.fab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity,R.color.my_color))
                        bnv_main.itemIconTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.nav_item2)
                        bnv_main.itemTextColor = ContextCompat.getColorStateList(this@MainActivity, R.color.nav_item2)
                        GroupFragment()


                    }
                    R.id.menu_bnv_my -> {
                        binding.fab.supportBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity,R.color.my_color3))
                        binding.fab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity,R.color.my_color3))
//                        bnv_main.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_item)
//                        bnv_main.itemTextColor = ContextCompat.getColorStateList(this, R.color.nav_item)
                        MyFragment(promiseItems,memoryItems)


                    }
                    R.id.menu_bnv_membership -> {
                        binding.fab.supportBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity,R.color.my_color))
                        binding.fab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity,R.color.my_color))
                        bnv_main.itemIconTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.nav_item2)
                        bnv_main.itemTextColor = ContextCompat.getColorStateList(this@MainActivity, R.color.nav_item2)
                        MembershipFragment()

                    }
                    else -> {
//                        bnv_main.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_item)
//                        bnv_main.itemTextColor = ContextCompat.getColorStateList(this, R.color.nav_item)
                        MyFragment(promiseItems,memoryItems)

                    }
                }

            )
            true
        }



        binding.fab.setOnClickListener { item ->

            binding.fab.supportBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity,R.color.my_color3))
            binding.fab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity,R.color.my_color3))
            bnv_main.selectedItemId = R.id.menu_bnv_my

            changeFragment(
                MyFragment(promiseItems,memoryItems)
            )
        }
        true

        binding.bnv.selectedItemId = R.id.menu_bnv_my



    }

    override fun onResume() {
        super.onResume()
        promiseLoad()
    }






    private fun promiseLoad(){
        var today = SimpleDateFormat("yyyyMMddHHmm").format(Date())

        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()


        userRef.document(userId).collection("groups").get().addOnSuccessListener { result->
            promiseItems.clear()
            memoryItems.clear()
            for (doc in result){
                userRef.document(userId).collection("groups").document(doc.id).collection("promise").get().addOnCompleteListener { result2->
                    for (doc2 in result2.result){

                        var date = doc2.get("setLineup")as String

                        // 계획된 약속
                        if(today.toLong()<date.toLong()){
                            val item= Item8(doc2.get("title")as String,doc2.get("place")as String,"${doc2.get("date")as String} ${doc2.get("time")as String}",doc.id,doc2.get("setLineup")as String)
                            promiseItems.add(item)
                        }else{  // 지난 약속
                            val item= Item8(doc2.get("title")as String,doc2.get("place")as String,"${doc2.get("date")as String} ${doc2.get("time")as String}",doc.id,doc2.get("setLineup")as String)
                            memoryItems.add(item)

                        }

                    }

                }

            }

        }

    }






    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction().detach(fragment).attach(fragment)
            .replace(R.id.fragment, fragment)
            .commit()
    }





}





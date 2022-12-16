package com.dabee.promise

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dabee.promise.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater)}
    var promiseItems:MutableList<Item8> = mutableListOf()
    var memoryItems:MutableList<Item8> = mutableListOf()
    val firebaseFirestore = FirebaseFirestore.getInstance()
    val userRef = firebaseFirestore.collection("users")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()
        userRef.document(userId).get().addOnSuccessListener {
           var userAddr = it.get("userAddress").toString()
            if(userAddr.equals("null")){
                var addr: MutableMap<String, String> = HashMap()
                addr["userAddress"] = ""
                userRef.document(userId).update(addr as MutableMap<String, Any>)
            }

        }




        val bnv_main = binding.bnv
        val myAnim :Animation = AnimationUtils.loadAnimation(this, R.anim.rotate_open);
        supportFragmentManager.beginTransaction().add(R.id.fragment, MyFragment(promiseItems,memoryItems)).commit()





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
            var num = 0
            promiseItems.clear()
            memoryItems.clear()
            for (doc in result){
                userRef.document(userId).collection("groups").document(doc.id).collection("promise").get().addOnCompleteListener { result2->
                    for (doc2 in result2.result){

                        var date = doc2.get("setLineup")as String



                        // 계획된 약속
                        if(today.toLong()<date.toLong()){
                            num++
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





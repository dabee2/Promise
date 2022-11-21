package com.dabee.promise

import android.content.res.ColorStateList
import android.location.Geocoder
import android.location.LocationProvider
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dabee.promise.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater)}
    var promiseItems:MutableList<Item> = mutableListOf()
    var memoryItems:MutableList<Item> = mutableListOf()
    val firebaseFirestore = FirebaseFirestore.getInstance()
    val userRef = firebaseFirestore.collection("users")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        Handler().postDelayed(Runnable {
            //딜레이 후 시작할 코드 작성
            changeFragment(
                MyFragment(promiseItems,memoryItems)
            )

        }, 1000) // 0.6초 정도 딜레이를 준 후 시작


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
        var today = SimpleDateFormat("yyyyMMddhhmm").format(Date())

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
                            val item= Item(doc2.get("title")as String,doc2.get("place")as String,"${doc2.get("date")as String} ${doc2.get("time")as String}",doc.id,doc2.get("setLineup")as String)
                            promiseItems.add(item)
                        }else{  // 지난 약속
                            val item= Item(doc2.get("title")as String,doc2.get("place")as String,"${doc2.get("date")as String} ${doc2.get("time")as String}",doc.id,doc2.get("setLineup")as String)
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



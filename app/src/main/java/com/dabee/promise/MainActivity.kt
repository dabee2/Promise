package com.dabee.promise

import android.content.res.ColorStateList
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
    var items:MutableList<Item> = mutableListOf()
    val firebaseFirestore = FirebaseFirestore.getInstance()
    val userRef = firebaseFirestore.collection("users")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        Handler().postDelayed(Runnable {
            //딜레이 후 시작할 코드 작성
            changeFragment(
                MyFragment(items)
            )

        }, 1000) // 0.6초 정도 딜레이를 준 후 시작


        val bnv_main = binding.bnv
        val myAnim :Animation = AnimationUtils.loadAnimation(this, R.anim.rotate_open);
        supportFragmentManager.beginTransaction().add(R.id.fragment, MyFragment(items)).commit()



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
                        MyFragment(items)


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
                        MyFragment(items)

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
                MyFragment(items)
            )
        }
        true

        binding.bnv.selectedItemId = R.id.menu_bnv_my



    }

    override fun onResume() {
        super.onResume()

        promiseLoad()


    }
    private fun promiseSet(){


        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()


        userRef.document(userId).collection("groups").get().addOnSuccessListener { result->
            items.clear()
            for (doc in result){
                userRef.document(userId).collection("groups").document(doc.id).collection("promise").get().addOnCompleteListener { result2->
                    for (doc2 in result2.result){

                        val item= Item(doc2.get("title")as String,doc2.get("place")as String,"${doc2.get("date")as String} ${doc2.get("time")as String}",doc.id)
                        items.add(item)


                    }

                }

            }

//            var date = SimpleDateFormat("yyyyMMdd").format(items[])
//            items.sortWith(compareBy<Int> { it.date }.thenBy { it.time })

        }

    }


    private fun promiseLoad(){


        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()


        userRef.document(userId).collection("groups").get().addOnSuccessListener { result->
            items.clear()
            for (doc in result){
                userRef.document(userId).collection("groups").document(doc.id).collection("promise").get().addOnCompleteListener { result2->
                    for (doc2 in result2.result){

                        val item= Item(doc2.get("title")as String,doc2.get("place")as String,"${doc2.get("date")as String} ${doc2.get("time")as String}",doc.id)
                        items.add(item)

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



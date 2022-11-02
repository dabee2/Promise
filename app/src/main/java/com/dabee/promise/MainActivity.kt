package com.dabee.promise

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import com.dabee.promise.databinding.ActivityLoginBinding
import com.dabee.promise.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var vv:Boolean? = null
        val bnv_main = binding.bnv
        val myAnim :Animation = AnimationUtils.loadAnimation(this, R.anim.rotate_open);
        supportFragmentManager.beginTransaction().add(R.id.fragment, MyFragment()).commit()



        bnv_main.setOnItemSelectedListener { item ->
            changeFragment(
                when (item.itemId) {
                    R.id.menu_bnv_group -> {
                        binding.fab.supportBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.my_color))
                        binding.fab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.my_color))
                        bnv_main.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_item2)
                        bnv_main.itemTextColor = ContextCompat.getColorStateList(this, R.color.nav_item2)
                        GroupFragment()

                    }
                    R.id.menu_bnv_my -> {
                        binding.fab.supportBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.my_color2))
                        binding.fab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.my_color2))
//                        bnv_main.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_item)
//                        bnv_main.itemTextColor = ContextCompat.getColorStateList(this, R.color.nav_item)
                        MyFragment()


                    }
                    R.id.menu_bnv_membership -> {
                        binding.fab.supportBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.my_color))
                        binding.fab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.my_color))
                        bnv_main.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_item3)
                        bnv_main.itemTextColor = ContextCompat.getColorStateList(this, R.color.nav_item3)
                        MembershipFragment()
                    }
                    else -> {
//                        bnv_main.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_item)
//                        bnv_main.itemTextColor = ContextCompat.getColorStateList(this, R.color.nav_item)
                        MyFragment()
                    }
                }

            )
            true
        }

        binding.fab.setOnClickListener { item ->

            binding.fab.supportBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.my_color))
            binding.fab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this,R.color.my_color2))
            bnv_main.selectedItemId = R.id.menu_bnv_my

            changeFragment(
                MyFragment()
            )
        }
        true



        bnv_main.selectedItemId = R.id.menu_bnv_my


    }


    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment, fragment)
            .commit()
    }




}



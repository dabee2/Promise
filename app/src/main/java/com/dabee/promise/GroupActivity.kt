package com.dabee.promise

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dabee.promise.databinding.ActivityGroupBinding

class GroupActivity : AppCompatActivity() {

    val binding by lazy { ActivityGroupBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }
}
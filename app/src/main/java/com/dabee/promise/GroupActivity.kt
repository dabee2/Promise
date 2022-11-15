package com.dabee.promise

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dabee.promise.databinding.ActivityGroupBinding
import net.daum.mf.map.api.MapView

class GroupActivity : AppCompatActivity() {

    val binding by lazy { ActivityGroupBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent = intent
        val groupName: String = intent.getStringExtra("groupName").toString()
        binding.tvTitle.text =groupName
        binding.iv.setOnClickListener { finish() }

        val mapView = MapView(this)

        val mapViewContainer = binding.mapView
//        val mapViewContainer = findViewById<View>(R.id.map_view) as ViewGroup
        mapViewContainer.addView(mapView)

    }
}
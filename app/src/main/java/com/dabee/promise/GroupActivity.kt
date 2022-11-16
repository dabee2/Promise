package com.dabee.promise

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dabee.promise.databinding.ActivityGroupBinding
import com.dabee.promise.databinding.CustomCalloutBalloonBinding
import com.google.firebase.firestore.FirebaseFirestore
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class GroupActivity : AppCompatActivity() {

    val binding by lazy { ActivityGroupBinding.inflate(layoutInflater) }

    val firebaseFirestore = FirebaseFirestore.getInstance()
    val userRef = firebaseFirestore.collection("users")
    lateinit var groupName:String
    var members:MutableList<FriendsItem2> = mutableListOf()
    var promiseItems:MutableList<PromiseItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent = intent
        groupName= intent.getStringExtra("groupName").toString()

        binding.tvTitle.text =groupName
        binding.iv.setOnClickListener { finish() }

        mapLoad()
        friendLoad()
        promiseLoad()
        binding.btnPromiseAdd.setOnClickListener {
            val intent2:Intent= Intent(this,GroupActivityPromiseAdd::class.java)
            intent2.putExtra("groupName",groupName)
            intentActivityResultLauncher.launch(intent2)
        }



    }

    override fun onResume() {
        super.onResume()

        friendLoad()
        promiseLoad()
    }

    private fun promiseLoad(){

        binding.rvPromis.adapter = RecyclerAdapterMini(this,promiseItems)

        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()
        userRef.document(userId).collection("groups").document(groupName).collection("promise").get().addOnSuccessListener { result ->
            promiseItems.clear()
            for (doc in result){

                val item = PromiseItem(doc.get("title")as String,doc.get("place")as String,doc.get("date")as String,doc.get("time")as String,doc.get("note")as String,groupName)
                promiseItems.add(item)
            }

            binding.rvPromis.adapter?.notifyDataSetChanged()

        }

    }

    // 액티비티를 실행시켜주는 객체 생성- 멤버변수 위치.
    var intentActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        // 결과주는 Activity가 종료되면 실행되는 메소드

        // 돌려보낸 결과가 OK인지 .. 확인
        if (result.resultCode == Activity.RESULT_OK) {
            promiseLoad()

        } else {

        }
    }

    private fun mapLoad(){
        val mapView = MapView(this)

        val mapViewContainer = binding.mapView
        mapViewContainer.addView(mapView)

        var lat:Double = 37.5866169
        var lon:Double = 127.0977436

        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(lat, lon), 0, true)

        val marker = MapPOIItem()
        marker.itemName = groupName
        //        marker.setTag(3);
        marker.mapPoint = MapPoint.mapPointWithGeoCoord(lat, lon)
        marker.markerType = MapPOIItem.MarkerType.RedPin // 기본으로 제공하는 BluePin 마커 모양.

        marker.selectedMarkerType = MapPOIItem.MarkerType.BluePin // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        marker.isCustomImageAutoscale = false
        marker.setCustomImageAnchor(0.5f, 1.0f)

        mapView.setCalloutBalloonAdapter(CustomCalloutBalloonAdapter())

        mapView.addPOIItem(marker)

        mapView.selectPOIItem(marker, true)


    }


    private fun friendLoad(){
        binding.rvMembers.adapter = RecyclerAdapterGroupChild(this,members)
        // 데이터베이스에서 내정보 불러오기

        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()

        userRef.document(userId).collection("groups").document(groupName).collection("members").get().addOnSuccessListener { result->
            members.clear()
            for (doc in result){
                var isJoin: Boolean = doc.get("isJoin") as Boolean
                userRef.document(doc.id).get().addOnSuccessListener {
                    if (isJoin) {
                        var isJoin: MutableMap<String, Boolean> = HashMap()
                        isJoin["isJoin"] = true
                        userRef.document(userId).collection("groups").document(groupName).collection("members").document(doc.id).set(it)
                        userRef.document(userId).collection("groups").document(groupName).collection("members").document(doc.id).update(isJoin as Map<String, Any>)
                    }

                }
                if(isJoin){
                    var datas: MutableMap<String, String> = doc["data"] as MutableMap<String, String> // 해시맵

                    val item = FriendsItem2(datas["userName"]as String,datas["userImgUrl"]as String,datas["userId"]as String,doc.get("isJoin") as Boolean)

                    members.add(item)
                }

            }
            binding.rvMembers.adapter?.notifyDataSetChanged()
        }
   }
    inner class CustomCalloutBalloonAdapter : CalloutBalloonAdapter {

        val binding by lazy { CustomCalloutBalloonBinding.inflate(layoutInflater) }

        var name = ""//secondIntent.getStringExtra("name")
        var str = ""//secondIntent.getStringExtra("addr")
        var sel = 0//secondIntent.getIntExtra("sel", 0)
        private val mCalloutBalloon: View
        override fun getCalloutBalloon(poiItem: MapPOIItem): View {


//            if (sel == 1) (mCalloutBalloon.findViewById<View>(R.id.badge) as ImageView).setImageResource(
//                R.drawable.free_icon_animal_clinic1
//            )
//            if (sel == 2) (mCalloutBalloon.findViewById<View>(R.id.badge) as ImageView).setImageResource(
//                R.drawable.free_icon_animal_clinic3
//            )

            (mCalloutBalloon.findViewById<View>(R.id.title) as TextView).text = name
            (mCalloutBalloon.findViewById<View>(R.id.desc) as TextView).text = str
            return mCalloutBalloon
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem): View {
            return mCalloutBalloon
        }

        init {
            mCalloutBalloon = LayoutInflater.from(this@GroupActivity).inflate(R.layout.custom_callout_balloon, null)

        }

    }


}


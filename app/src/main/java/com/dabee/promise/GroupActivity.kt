package com.dabee.promise

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scaleMatrix
import com.dabee.promise.databinding.ActivityGroupBinding
import com.dabee.promise.databinding.CustomCalloutBalloonBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import net.daum.mf.map.api.*
import java.text.SimpleDateFormat
import java.util.*


class GroupActivity : AppCompatActivity() {

    val binding by lazy { ActivityGroupBinding.inflate(layoutInflater) }

    val firebaseFirestore = FirebaseFirestore.getInstance()
    val userRef = firebaseFirestore.collection("users")
    lateinit var groupName:String
    var members:MutableList<FriendsItem2> = mutableListOf()
    var promiseItems:MutableList<PromiseItem> = mutableListOf()
    var userMarkers:MutableList<FriendsItem> = mutableListOf()
    var userLatLon:MutableList<LatLon> = mutableListOf()
    val userMarker:MutableList<MapPOIItem> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent = intent
        groupName= intent.getStringExtra("groupName").toString()

        binding.tvTitle.text =groupName
        binding.iv.setOnClickListener { finish() }

        Handler().postDelayed(Runnable {
            mapLoad()

        }, 1200) // 0.6초 정도 딜레이를 준 후 시작



        binding.btnPromiseAdd.setOnClickListener {
            val intent2:Intent= Intent(this,GroupActivityPromiseAdd::class.java)
            intent2.putExtra("groupName",groupName)
            intentActivityResultLauncher.launch(intent2)
        }

        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()



        userRef.document(userId).collection("groups").document(groupName).collection("promise").orderBy("setLineup", Query.Direction.DESCENDING).limit(1).addSnapshotListener{s,e ->
            promiseLoad()
        }


    }

    override fun onResume() {
        super.onResume()

        friendLoad()
        promiseLoad()

    }


    private fun promiseLoad(){

        var today = SimpleDateFormat("yyyyMMddHHmm").format(Date())

        binding.rvPromis.adapter = RecyclerAdapterMini(this,promiseItems)

        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()
        userRef.document(userId).collection("groups").document(groupName).collection("promise").get().addOnSuccessListener { result ->
            promiseItems.clear()
            for (doc in result){
                var date = doc.get("setLineup")as String

                if(today.toLong()<date.toLong()){
                    val item = PromiseItem(doc.get("title")as String,doc.get("place")as String,doc.get("date")as String,doc.get("time")as String,doc.get("note")as String,groupName,doc.get("setLineup")as String)
                    promiseItems.add(item)
                }

            }
            if (promiseItems.size==0) binding.tvPromise2.visibility = View.VISIBLE
            else binding.tvPromise2.visibility = View.INVISIBLE

            promiseItems.sortWith(compareBy { it.setLineup.toLong() })
            binding.rvPromis.adapter?.notifyDataSetChanged()

        }

    }


    // 액티비티를 실행시켜주는 객체 생성- 멤버변수 위치.
    var intentActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        // 결과주는 Activity가 종료되면 실행되는 메소드

        // 돌려보낸 결과가 OK인지 .. 확인
        if (result.resultCode == Activity.RESULT_OK) {


        } else {

        }
    }



    fun midPoint(lat1: Double, lon1: Double, lat2: Double, lon2: Double): LatLon {
        var lat1 = lat1
        var lon1 = lon1
        var lat2 = lat2
        val dLon = Math.toRadians(lon2 - lon1)

        //convert to radians
        lat1 = Math.toRadians(lat1)
        lat2 = Math.toRadians(lat2)
        lon1 = Math.toRadians(lon1)
        val Bx = Math.cos(lat2) * Math.cos(dLon)
        val By = Math.cos(lat2) * Math.sin(dLon)
        val lat3 = Math.atan2(
            Math.sin(lat1) + Math.sin(lat2),
            Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By)
        )
        val lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx)

        val midLatLon = LatLon(Math.toDegrees(lat3).toString(),Math.toDegrees(lon3).toString(),"midPoint")
        return midLatLon

    }

    //위도 경도로 주소 구하는 Reverse-GeoCoding
    private fun getAddress(position: LatLon): String {
        val geoCoder = Geocoder(this, Locale.KOREA)
        var addr = "주소 오류"

        //GRPC 오류? try catch 문으로 오류 대처
        try {
            addr = geoCoder.getFromLocation(position.lat.toDouble(), position.lon.toDouble(), 1).first().getAddressLine(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return addr
    }



    private fun mapLoad(){

        var midPoint = LatLon(userLatLon[0].lat,userLatLon[0].lon,"midPoint")




        val mapView = MapView(this)

        val mapViewContainer = binding.mapView
        mapViewContainer.addView(mapView)




        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(midPoint.lat.toDouble(),midPoint.lon.toDouble()), 8, true)
        mapView.setCalloutBalloonAdapter(CustomCalloutBalloonAdapter())
        userMarker.clear()

        for (i in 0..userLatLon.size-1){


            val marker = MapPOIItem()
            userMarker.add(i,marker)
            userMarker[i].itemName = i.toString()//userLatLon[i].userId
            marker.setTag(i);

            userMarker[i].mapPoint = MapPoint.mapPointWithGeoCoord(userLatLon[i].lat.toDouble(),userLatLon[i].lon.toDouble())
            userMarker[i].markerType = MapPOIItem.MarkerType.RedPin // 기본으로 제공하는 BluePin 마커 모양.

            userMarker[i].selectedMarkerType = MapPOIItem.MarkerType.YellowPin // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

            userMarker[i].isCustomImageAutoscale = false
            userMarker[i].setCustomImageAnchor(0.5f, 1.0f)

            mapView.addPOIItem(userMarker[i])


        }


        for (i in 0..userLatLon.size-1){
            midPoint = midPoint(midPoint.lat.toDouble(),midPoint.lon.toDouble(),userLatLon[i].lat.toDouble(),userLatLon[i].lon.toDouble())
        }
        var midAddr = getAddress(midPoint)
        binding.tvMidAddr.text = midAddr

        val marker3 = MapPOIItem()
        marker3.apply {
            customImageResourceId = R.drawable.noun_my_location_red
            customSelectedImageResourceId = R.drawable.noun_my_location_blue
            isCustomImageAutoscale = true
        }
        userMarkers.add(FriendsItem("중간위치","","$midAddr"))
        marker3.itemName = "중간위치"
        marker3.setTag(userLatLon.size);
        marker3.mapPoint = MapPoint.mapPointWithGeoCoord(midPoint.lat.toDouble(),midPoint.lon.toDouble())
        marker3.markerType = MapPOIItem.MarkerType.CustomImage // 기본으로 제공하는 BluePin 마커 모양.


        marker3.selectedMarkerType = MapPOIItem.MarkerType.CustomImage // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.


        marker3.setCustomImageAnchor(0.5f, 1.0f)


        mapView.addPOIItem(marker3)
        mapView.selectPOIItem(marker3, false)




    }





    private fun friendLoad(){
        binding.rvMembers.adapter = RecyclerAdapterGroupChild(this,members)
        // 데이터베이스에서 내정보 불러오기

        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()

        userRef.document(userId).collection("groups").document(groupName).collection("members").get().addOnSuccessListener { result->
            members.clear()
            userLatLon.clear()
            userMarkers.clear()

            for (doc in result){
                var isJoin: Boolean = doc.get("isJoin") as Boolean
                userRef.document(doc.id).get().addOnSuccessListener {
                    if (isJoin) {
                        var isJoin: MutableMap<String, Boolean> = HashMap()
                        isJoin["isJoin"] = true
                        userRef.document(userId).collection("groups").document(groupName).collection("members").document(doc.id).set(it)
                        userRef.document(userId).collection("groups").document(groupName).collection("members").document(doc.id).update(isJoin as Map<String, Any>)

                        if(it.get("userAddress") != null){
                            userMarkers.add(FriendsItem(it.get("userName") as String,it.get("userImgUrl") as String,it.get("userAddress") as String))
                            userLatLon.add(LatLon(it.get("lat") as String,it.get("lon") as String,it.get("userId") as String))
                        }


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



    inner class CustomCalloutBalloonAdapter() : CalloutBalloonAdapter {

        val binding by lazy { CustomCalloutBalloonBinding.inflate(layoutInflater) }

        override fun getCalloutBalloon(poiItem: MapPOIItem): View {

            val item: FriendsItem = userMarkers[poiItem.tag]
            binding.tvTitleCcb.text = item.name
            binding.tvAddrCcb.text = ""


            return binding.root
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem): View {
            val item: FriendsItem = userMarkers[poiItem.tag]
            binding.tvTitleCcb.text = ""
            binding.tvAddrCcb.text = item.id

            return binding.root
        }


    }




}


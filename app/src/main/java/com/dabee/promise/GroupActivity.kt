package com.dabee.promise

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dabee.promise.databinding.ActivityGroupBinding
import com.dabee.promise.databinding.ActivityGroupBinding.inflate
import com.dabee.promise.databinding.ActivityJoinGroupBinding.inflate
import com.dabee.promise.databinding.CustomCalloutBalloonBinding
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
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
        }, 1000) // 0.6초 정도 딜레이를 준 후 시작



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

        var today = SimpleDateFormat("yyyyMMddhhmm").format(Date())

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
            promiseItems.sortWith(compareBy { it.setLineup.toLong() })

            Handler().postDelayed(Runnable {
                //딜레이 후 시작할 코드 작성

                binding.rvPromis.adapter?.notifyDataSetChanged()
            }, 500) //0.5초 후 시작
            Handler().postDelayed(Runnable {
                //딜레이 후 시작할 코드 작성

                binding.rvPromis.adapter?.notifyDataSetChanged()
            }, 1000) //1초 후 시작


            Handler().postDelayed(Runnable {
                //딜레이 후 시작할 코드 작성

                binding.rvPromis.adapter?.notifyDataSetChanged()
            }, 1500) //1.5초 후 시작


        }

    }

    // 액티비티를 실행시켜주는 객체 생성- 멤버변수 위치.
    var intentActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        // 결과주는 Activity가 종료되면 실행되는 메소드

        // 돌려보낸 결과가 OK인지 .. 확인
        if (result.resultCode == Activity.RESULT_OK) {
            onResume()

        } else {

        }
    }

//    var mp = midPoint(LatLng(-43.95139, -176.56111), LatLng(-36.397816, 174.663496))
//    fun midPoint(SW: LatLng?, NE: LatLng?): LatLng {
//        var bounds = LatLngBounds(SW, NE)
//        Log.d("BAD!", bounds.toString().toString() + " CENTRE: " + bounds.getCenter().toString())
//        bounds = LatLngBounds.builder().include(SW).include(NE).build()
//        Log.d("GOOD", bounds.toString().toString() + " CENTRE: " + bounds.getCenter().toString())
//        return bounds.getCenter()
//    }

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

//        println(Math.toDegrees(lat3).toString() + " " + Math.toDegrees(lon3))

    }



    private fun mapLoad(){

        val mapView = MapView(this)

        val mapViewContainer = binding.mapView
        mapViewContainer.addView(mapView)

        var midlatlon = midPoint(userLatLon[0].lat.toDouble(),userLatLon[0].lon.toDouble(),userLatLon[1].lat.toDouble(),userLatLon[1].lon.toDouble())

        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(midlatlon.lat.toDouble(),midlatlon.lon.toDouble()), 8, true)
        mapView.setCalloutBalloonAdapter(CustomCalloutBalloonAdapter())
        userMarker.clear()

        for (i in 0..userLatLon.size-1){


            val marker = MapPOIItem()
            userMarker.add(i,marker)
            userMarker[i].itemName = i.toString()//userLatLon[i].userId
            marker.setTag(i);

            userMarker[i].mapPoint = MapPoint.mapPointWithGeoCoord(userLatLon[i].lat.toDouble(),userLatLon[i].lon.toDouble())
            userMarker[i].markerType = MapPOIItem.MarkerType.RedPin // 기본으로 제공하는 BluePin 마커 모양.

            userMarker[i].selectedMarkerType = MapPOIItem.MarkerType.BluePin // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

            userMarker[i].isCustomImageAutoscale = false
            userMarker[i].setCustomImageAnchor(0.5f, 1.0f)

            mapView.addPOIItem(userMarker[i])


        }





        val marker3 = MapPOIItem()
        userMarkers.add(FriendsItem("중간위치","https://firebasestorage.googleapis.com/v0/b/promise-c6321.appspot.com/o/profile%2F2506781891%2F2506781891.png?alt=media&token=267e1dac-147e-4a6f-b395-368db2d92396","중간위치"))
        marker3.itemName = 0.toString()//midlatlon.userId
        marker3.setTag(userLatLon.size);
        marker3.mapPoint = MapPoint.mapPointWithGeoCoord(midlatlon.lat.toDouble(),midlatlon.lon.toDouble())
        marker3.markerType = MapPOIItem.MarkerType.RedPin // 기본으로 제공하는 BluePin 마커 모양.

        marker3.selectedMarkerType = MapPOIItem.MarkerType.BluePin // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        marker3.isCustomImageAutoscale = false
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

//                        userLatLon.add(LatLon(it.get("lat") as String,it.get("lon") as String,it.get("userId") as String))

                    }

                }
                if(isJoin){
                    var datas: MutableMap<String, String> = doc["data"] as MutableMap<String, String> // 해시맵

                    val item = FriendsItem2(datas["userName"]as String,datas["userImgUrl"]as String,datas["userId"]as String,doc.get("isJoin") as Boolean)
                    val item3 = FriendsItem(datas["userName"]as String,datas["userImgUrl"]as String,datas["userAddress"]as String)
                    val item2 = LatLon(datas["lat"]as String,datas["lon"]as String,datas["userId"] as String)

                    userLatLon.add(item2)
                    members.add(item)
                    userMarkers.add(item3)
                }

            }
            binding.rvMembers.adapter?.notifyDataSetChanged()
        }

   }

//     커스텀 말풍선 클래스
//    inner class CustomCalloutBalloonAdapter(inflater: LayoutInflater): CalloutBalloonAdapter {
//
//        val mCalloutBalloon: View = inflater.inflate(R.layout.custom_callout_balloon, null)
//        val title: TextView = mCalloutBalloon.findViewById(R.id.tv_title_ccb)
//        val address: TextView = mCalloutBalloon.findViewById(R.id.tv_addr_ccb)
//        val civ: CircleImageView = mCalloutBalloon.findViewById(R.id.civ_ccb)
//
//        override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
//            // 마커 클릭 시 나오는 말풍선
//            val item: FriendsItem2 = members[poiItem!!.itemName.toInt()]
//            title.text = item.name
//            address.text = item.id
//            Glide.with(this@GroupActivity).load(item.img).error(R.drawable.images).into(civ)
//
//
//
//
//            return mCalloutBalloon
//        }
//
//        override fun getPressedCalloutBalloon(poiItem: MapPOIItem?): View {
//            // 말풍선 클릭 시
//            address.text = "getPressedCalloutBalloon"
//            return mCalloutBalloon
//        }
//    }


    inner class CustomCalloutBalloonAdapter() : CalloutBalloonAdapter {

        val binding by lazy { CustomCalloutBalloonBinding.inflate(layoutInflater) }

        override fun getCalloutBalloon(poiItem: MapPOIItem): View {
            var s = poiItem.tag

            val item: FriendsItem = userMarkers[0]
            binding.tvTitleCcb.text = item.name
            binding.tvAddrCcb.text = item.id
//            Glide.with(this@GroupActivity).load(item.img).error(R.drawable.images).into(binding.civCcb)




//            Glide.with(this@GroupActivity).load(item.img).error(R.drawable.images).into(binding.civCcb)


            return binding.root
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem): View {



            return binding.root
        }


    }


//    inner class CustomCalloutBalloonAdapter : CalloutBalloonAdapter {
//
//        private val mCalloutBalloon: View
//        override fun getCalloutBalloon(poiItem: MapPOIItem): View {
//            var s = poiItem.tag
//
//            val item: FriendsItem = userMarkers[s]
////
////            if (poiItem.tag == 1) (mCalloutBalloon.findViewById<View>(R.id.civ_ccb) as ImageView).setImageResource(
////                R.drawable.ic_baseline_notifications_24
////            )
////            if (poiItem.tag == 2) (mCalloutBalloon.findViewById<View>(R.id.civ_ccb) as ImageView).setImageResource(
////                R.drawable.ic_baseline_notifications_active_24
////            )
//            Glide.with(this@GroupActivity).load(item.img).error(R.drawable.images).into(mCalloutBalloon.findViewById<View>(R.id.civ_ccb) as CircleImageView)
//            (mCalloutBalloon.findViewById<View>(R.id.tv_title_ccb) as TextView).text = item.name
//            (mCalloutBalloon.findViewById<View>(R.id.tv_addr_ccb) as TextView).text = item.id
//            return mCalloutBalloon
//        }
//
//        override fun getPressedCalloutBalloon(poiItem: MapPOIItem): View {
//            return mCalloutBalloon
//        }
//
//        init {
//            mCalloutBalloon = layoutInflater.inflate(R.layout.custom_callout_balloon,null)
//        }
//    }


}


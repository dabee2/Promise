package com.dabee.promise

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dabee.promise.databinding.FragmentMyChild1Binding
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyFragmentChild1.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyFragmentChild1 constructor(var items:MutableList<Item8>) : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val binding by lazy {  FragmentMyChild1Binding.inflate(layoutInflater)}
    val firebaseFirestore = FirebaseFirestore.getInstance()
    val userRef = firebaseFirestore.collection("users")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (items.size == 0){
            onResume()
        }else binding.tv.visibility = View.INVISIBLE



    }

    override fun onResume() {
        super.onResume()

        if(items.size==0){
            binding.tv.visibility = View.VISIBLE

            Handler().postDelayed(Runnable {
                items.sortWith(compareBy { it.setLineup.toLong()})
                binding.recycler.adapter = RecyclerAdapter(binding.root.context,items)
                onResume()
            }, 300)

        }else{
            items.sortWith(compareBy { it.setLineup.toLong()})
            binding.recycler.adapter = RecyclerAdapter(binding.root.context,items)
            binding.tv.visibility = View.INVISIBLE
            alarm()
            getWether()
        }


    }
    private fun alarm(){
        for (i in 0..items.size-1){


            val dateFormat2 = SimpleDateFormat("yyyyMMddHHmm")
            val endDate = items[i].setLineup.toLong()
            val pDate = dateFormat2.parse(endDate.toString()).time


            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.MINUTE, 1)
            val today = dateFormat2.format(calendar.time)
            val today2 = dateFormat2.parse(today).time
            var dday= "${(pDate - today2)/ (24 * 60 * 60 * 1000)}"

            if (dday != "0"){
                setAlarm(items[i],i)
            }


        }


    }

    private fun setAlarm(item:Item8,num:Int) {

        var notificationManager = requireActivity().getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager

        var alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        var mCalender = GregorianCalendar()
        //AlarmReceiver에 값 전달
        val receiverIntent = Intent(context, AlarmRecevier::class.java)
        receiverIntent.putExtra("title",item.title)
        receiverIntent.putExtra("place",item.place)
        receiverIntent.putExtra("group",item.groupName)
        receiverIntent.putExtra("date",item.time)
        receiverIntent.putExtra("setLineup",item.setLineup)
        receiverIntent.putExtra("num",num)
        val pendingIntent = PendingIntent.getBroadcast(context, num, receiverIntent, FLAG_UPDATE_CURRENT)
        var from = item.setLineup
            from = "${item.setLineup}00"

        //날짜 포맷을 바꿔주는 소스코드

//        val strFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(from).toString()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val newDtFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var datetime: Date? = null
        try {
            val strFormat = SimpleDateFormat("yyyyMMddHHmmss")
            val formatDate: Date = strFormat.parse(from)
            val strNewDtFormat: String = newDtFormat.format(formatDate)
            datetime = dateFormat.parse(strNewDtFormat)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val calendar = Calendar.getInstance()
        calendar.time = datetime
        calendar.add(Calendar.DATE, -1)
        alarmManager.set(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
    }

    private fun getWether(){
        for (i in 0..items.size-1){
            val item:Item8 = items.get(i)

            var dday =  item.setLineup.substring(0,8)
            val dateFormat = SimpleDateFormat("yyyyMMdd")
            val endDate = dateFormat.parse(dday).time
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time.time

            var dday3= "${(endDate - today) / (24 * 60 * 60 * 1000)}"

            if (dday3.toInt()>=3 && dday3.toInt()<=10){
                var sun = item.time.substring(14,16)
                retrofit(i,dday3.toInt(),sun)
            }


        }

        //        val ss = "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidLandFcst?serviceKey=Zb2rfa2mmu%2BbKTIpNHoc4ao2gs09wedtsqFnGyAzTeFcRsbBPYaiLzCrVD6El0paOABWq5%2FFuVfwFpls8uns2Q%3D%3D&numOfRows=10&pageNo=1&dataType=JSON&regId=11B00000&tmFc=202211300600"





    }

    private fun retrofit(index:Int,dday:Int,sun:String){
        val pref = requireContext().getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()


        // Retrofit을 이용하여 HTTP 통신 시작
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/1360000/MidFcstInfoService/")
            .addConverterFactory(ScalarsConverterFactory.create())  // 순서 중요 Scalars 먼저!
            .build()

        val retrofitService = retrofit.create(RetrofitService::class.java)
        val serviceKey = "Zb2rfa2mmu+bKTIpNHoc4ao2gs09wedtsqFnGyAzTeFcRsbBPYaiLzCrVD6El0paOABWq5/FuVfwFpls8uns2Q=="
        val regId = "11B00000"
        val dataType= "JSON"
//        var tmFc = "202212020600"
        var tmFc = SimpleDateFormat("yyyyMMdd").format(Date()).toString()
        var time = SimpleDateFormat("HHmm").format(Date()).toString()


        if(time.toInt() < 600){
            tmFc = (tmFc.toLong() - 1).toString()
            tmFc += "1800"
        }else if(time.toInt() >= 600 && time.toInt() <1800){
            tmFc += "0600"
        }else if(time.toInt() >= 1800){
            tmFc += "1800"
        }


        retrofitService.searchData(serviceKey, dataType, regId, tmFc).enqueue(object :
            Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                val apiResponse: String? = response.body()
                var index2 =0

                val str1 = apiResponse?.indexOf("wf3Am")
                val str2 = apiResponse?.indexOf("pageNo")
                var str3 = apiResponse?.substring((str1!!.toInt()-1),(str2!!.toInt()-5))
                str3 = str3?.replace(":",",")
                var map = str3?.split(",")
                var weatherlist: MutableList<String> = mutableListOf()
                if (dday==3) index2 = 0
                weatherlist.add(map?.get(1).toString())
                weatherlist.add(map?.get(3).toString())
                if (dday==4) index2 = 2
                weatherlist.add(map?.get(5).toString())
                weatherlist.add(map?.get(7).toString())
                if (dday==5) index2 = 4
                weatherlist.add(map?.get(9).toString())
                weatherlist.add(map?.get(11).toString())
                if (dday==6) index2 = 6
                weatherlist.add(map?.get(13).toString())
                weatherlist.add(map?.get(15).toString())
                if (dday==7) index2 = 8
                weatherlist.add(map?.get(17).toString())
                weatherlist.add(map?.get(19).toString())
                if (dday==8) index2 = 10
                weatherlist.add(map?.get(21).toString())
                if (dday==9) index2 = 11
                weatherlist.add(map?.get(23).toString())
                if (dday==10) index2 = 12
                weatherlist.add(map?.get(25).toString())
                if (dday<=7 && sun=="오후"){
                    index2 += 1
                }




                var weatherMap:MutableMap<String,String> = HashMap()
                weatherMap["weather"] = weatherlist[index2].replace("\"","")
                userRef.document(userId).collection("groups").document(items[index].groupName).collection("promise").document("${items[index].title}${items[index].setLineup}").update(weatherMap as MutableMap<String, Any>)

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(context, "서버 오류 날씨 정보를 불러오지 못하였습니다.", Toast.LENGTH_SHORT).show()

            }

        })


    }




}
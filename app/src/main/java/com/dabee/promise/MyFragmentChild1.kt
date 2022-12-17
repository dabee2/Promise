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
import okhttp3.internal.notify
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.log
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
            getWeather()
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

    private fun getWeather(){
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

        // "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidLandFcst?serviceKey=Zb2rfa2mmu%2BbKTIpNHoc4ao2gs09wedtsqFnGyAzTeFcRsbBPYaiLzCrVD6El0paOABWq5%2FFuVfwFpls8uns2Q%3D%3D&numOfRows=10&pageNo=1&dataType=JSON&regId=11B00000&tmFc=202211300600"

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
        val regIdTemp = "11B10101"
        val regIdWeather = "11B00000"
        val dataType= "JSON"
        var tmFc = SimpleDateFormat("yyyyMMdd").format(Date()).toString()  // "202212020600"
        var time = SimpleDateFormat("HHmm").format(Date()).toString()


        if(time.toInt() < 600){
            tmFc = (tmFc.toLong() - 1).toString()
            tmFc += "1800"
        }else if(time.toInt() >= 600 && time.toInt() <1800){
            tmFc += "0600"
        }else if(time.toInt() >= 1800){
            tmFc += "1800"
        }





        retrofitService.searchTemperature(serviceKey, dataType, regIdTemp, tmFc).enqueue(object :
            Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                val apiResponse: String? = response.body()


                var taMin3:String = apiResponse?.substring((apiResponse?.indexOf("taMin3")!!+8),(apiResponse?.indexOf("taMin3Low"))!!-2).toString()
                var taMax3:String = apiResponse?.substring((apiResponse?.indexOf("taMax3")!!+8),(apiResponse?.indexOf("taMax3Low"))!!-2).toString()

                var taMin4:String = apiResponse?.substring((apiResponse?.indexOf("taMin4")!!+8),(apiResponse?.indexOf("taMin4Low"))!!-2).toString()
                var taMax4:String = apiResponse?.substring((apiResponse?.indexOf("taMax4")!!+8),(apiResponse?.indexOf("taMax4Low"))!!-2).toString()

                var taMin5:String = apiResponse?.substring((apiResponse?.indexOf("taMin5")!!+8),(apiResponse?.indexOf("taMin5Low"))!!-2).toString()
                var taMax5:String = apiResponse?.substring((apiResponse?.indexOf("taMax5")!!+8),(apiResponse?.indexOf("taMax5Low"))!!-2).toString()

                var taMin6:String = apiResponse?.substring((apiResponse?.indexOf("taMin6")!!+8),(apiResponse?.indexOf("taMin6Low"))!!-2).toString()
                var taMax6:String = apiResponse?.substring((apiResponse?.indexOf("taMax6")!!+8),(apiResponse?.indexOf("taMax6Low"))!!-2).toString()

                var taMin7:String = apiResponse?.substring((apiResponse?.indexOf("taMin7")!!+8),(apiResponse?.indexOf("taMin7Low"))!!-2).toString()
                var taMax7:String = apiResponse?.substring((apiResponse?.indexOf("taMax7")!!+8),(apiResponse?.indexOf("taMax7Low"))!!-2).toString()

                var taMin8:String = apiResponse?.substring((apiResponse?.indexOf("taMin8")!!+8),(apiResponse?.indexOf("taMin8Low"))!!-2).toString()
                var taMax8:String = apiResponse?.substring((apiResponse?.indexOf("taMax8")!!+8),(apiResponse?.indexOf("taMax8Low"))!!-2).toString()

                var taMin9:String = apiResponse?.substring((apiResponse?.indexOf("taMin9")!!+8),(apiResponse?.indexOf("taMin9Low"))!!-2).toString()
                var taMax9:String = apiResponse?.substring((apiResponse?.indexOf("taMax9")!!+8),(apiResponse?.indexOf("taMax9Low"))!!-2).toString()

                var taMin10:String = apiResponse?.substring((apiResponse?.indexOf("taMin10")!!+9),(apiResponse?.indexOf("taMin10Low"))!!-2).toString()
                var taMax10:String = apiResponse?.substring((apiResponse?.indexOf("taMax10")!!+9),(apiResponse?.indexOf("taMax10Low"))!!-2).toString()


                var tempMap:MutableMap<String,String> = HashMap()
                tempMap.clear()
                if (dday==3) tempMap["temperature"] = "$taMin3˚/$taMax3˚"
                if (dday==4) tempMap["temperature"] = "$taMin4˚/$taMax4˚"
                if (dday==5) tempMap["temperature"] = "$taMin5˚/$taMax5˚"
                if (dday==6) tempMap["temperature"] = "$taMin6˚/$taMax6˚"
                if (dday==7) tempMap["temperature"] = "$taMin7˚/$taMax7˚"
                if (dday==8) tempMap["temperature"] = "$taMin8˚/$taMax8˚"
                if (dday==9) tempMap["temperature"] = "$taMin9˚/$taMax9˚"
                if (dday==10) tempMap["temperature"] = "$taMin10˚/$taMax10˚"


                if(tempMap.size !=0 && items.size !=0){
                    userRef.document(userId).collection("groups").document(items[index].groupName).collection("promise").document("${items[index].title}${items[index].setLineup}").update(tempMap as MutableMap<String, Any>)
                }else {
                    onResume()
                    return
                }






            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(context, "서버 오류 기온 정보를 불러오지 못하였습니다.", Toast.LENGTH_SHORT).show()

            }

        })



        retrofitService.searchWeather(serviceKey, dataType, regIdWeather, tmFc).enqueue(object :
            Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                val apiResponse: String? = response.body()


                var wf3Am:String = apiResponse?.substring((apiResponse?.indexOf("wf3Am")!!+8),(apiResponse?.indexOf("wf3Pm"))!!-3).toString()
                var wf3Pm:String = apiResponse?.substring((apiResponse?.indexOf("wf3Pm")!!+8),(apiResponse?.indexOf("wf4Am"))!!-3).toString()

                var wf4Am:String = apiResponse?.substring((apiResponse?.indexOf("wf4Am")!!+8),(apiResponse?.indexOf("wf4Pm"))!!-3).toString()
                var wf4Pm:String = apiResponse?.substring((apiResponse?.indexOf("wf4Pm")!!+8),(apiResponse?.indexOf("wf5Am"))!!-3).toString()

                var wf5Am:String = apiResponse?.substring((apiResponse?.indexOf("wf5Am")!!+8),(apiResponse?.indexOf("wf5Pm"))!!-3).toString()
                var wf5Pm:String = apiResponse?.substring((apiResponse?.indexOf("wf5Pm")!!+8),(apiResponse?.indexOf("wf6Am"))!!-3).toString()

                var wf6Am:String = apiResponse?.substring((apiResponse?.indexOf("wf6Am")!!+8),(apiResponse?.indexOf("wf6Pm"))!!-3).toString()
                var wf6Pm:String = apiResponse?.substring((apiResponse?.indexOf("wf6Pm")!!+8),(apiResponse?.indexOf("wf7Am"))!!-3).toString()

                var wf7Am:String = apiResponse?.substring((apiResponse?.indexOf("wf7Am")!!+8),(apiResponse?.indexOf("wf7Pm"))!!-3).toString()
                var wf7Pm:String = apiResponse?.substring((apiResponse?.indexOf("wf7Pm")!!+8),(apiResponse?.indexOf("wf8"))!!-3).toString()

                var wf8:String = apiResponse?.substring((apiResponse?.indexOf("wf8")!!+6),(apiResponse?.indexOf("wf9"))!!-3).toString()
                var wf9:String = apiResponse?.substring((apiResponse?.indexOf("wf9")!!+6),(apiResponse?.indexOf("wf10"))!!-3).toString()
                var wf10:String = apiResponse?.substring((apiResponse?.indexOf("wf10")!!+7),(apiResponse?.indexOf("}]},\"pageNo"))!!-1).toString()


                var weatherMap:MutableMap<String,String> = HashMap()
                weatherMap.clear()
                if (dday==3 && sun=="오전") weatherMap["weather"] = "$wf3Am"
                if (dday==3 && sun=="오후") weatherMap["weather"] = "$wf3Pm"
                if (dday==4 && sun=="오전") weatherMap["weather"] = "$wf4Am"
                if (dday==4 && sun=="오후") weatherMap["weather"] = "$wf4Pm"
                if (dday==5 && sun=="오전") weatherMap["weather"] = "$wf5Am"
                if (dday==5 && sun=="오후") weatherMap["weather"] = "$wf5Pm"
                if (dday==6 && sun=="오전") weatherMap["weather"] = "$wf6Am"
                if (dday==6 && sun=="오후") weatherMap["weather"] = "$wf6Pm"
                if (dday==7 && sun=="오전") weatherMap["weather"] = "$wf7Am"
                if (dday==7 && sun=="오후") weatherMap["weather"] = "$wf7Pm"
                if (dday==8) weatherMap["weather"] = "$wf8"
                if (dday==9) weatherMap["weather"] = "$wf9"
                if (dday==10) weatherMap["weather"] = "$wf10"

                if (weatherMap.size != 0 && items.size !=0){
                    userRef.document(userId).collection("groups").document(items[index].groupName).collection("promise").document("${items[index].title}${items[index].setLineup}").update(weatherMap as MutableMap<String, Any>)
                }else {
                    onResume()
                    return
                }



            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(context, "서버 오류 날씨 정보를 불러오지 못하였습니다.", Toast.LENGTH_SHORT).show()

            }

        })


    }




}
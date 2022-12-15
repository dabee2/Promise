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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
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




}
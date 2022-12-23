package com.dabee.promise


import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*


class AlarmRecevier : BroadcastReceiver() {
    var manager: NotificationManager? = null
    var builder: NotificationCompat.Builder? = null
    override fun onReceive(context: Context, intent: Intent?) {


        var title = intent!!.getStringExtra("title")
        var place = intent!!.getStringExtra("place")
        var group = intent!!.getStringExtra("group")
        var date = intent!!.getStringExtra("date")
        var setLineup = intent!!.getStringExtra("setLineup")

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        builder = null
        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager!!.createNotificationChannel(
                NotificationChannel(
                    "channel1",
                    "channel1",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            NotificationCompat.Builder(context, "channel1")
        } else {
            NotificationCompat.Builder(context)
        }

        //알림창 클릭 시 activity 화면 부름
        val intent2 = Intent(context, GroupActivityPromise::class.java)
        var dday =  setLineup!!.substring(0,8)
        val dateFormat = SimpleDateFormat("yyyyMMdd")
        val endDate = dateFormat.parse(dday).time
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time.time

        var dday3= "${(today - endDate) / (24 * 60 * 60 * 1000)}"

        if (dday3.toInt() == 0){
            dday3 = "Today"
        }else if (dday3.toInt() > 0){
            dday3 = "D+$dday3"
        } else {
            dday3 = "D$dday3"
        }

        intent2.putExtra("groupName",group)
        intent2.putExtra("promise","${title}${setLineup}")
        intent2.putExtra("dday",dday3)

        val pendingIntent = PendingIntent.getActivity(context, 101, intent2, PendingIntent.FLAG_UPDATE_CURRENT)

        var pTime =  date!!.substring(13)
        //알림창 제목
        builder!!.setContentTitle("${group}의 ${title} 일정")
        builder!!.setContentText("$place 내일$pTime")
        //알림창 아이콘
        builder!!.setSmallIcon(R.drawable.ic_icon5_playstore)
        //알림창 터치시 자동 삭제
        builder!!.setAutoCancel(true)
        builder!!.setContentIntent(pendingIntent)
        val notification: Notification = builder!!.build()
        manager!!.notify(0, notification)
    }

    companion object {
        //오레오 이상은 반드시 채널을 설정해줘야 Notification이 작동함
        private const val CHANNEL_ID = "channel1"
        private const val CHANNEL_NAME = "Channel1"
    }
}
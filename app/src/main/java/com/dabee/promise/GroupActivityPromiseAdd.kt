package com.dabee.promise




import android.os.Bundle
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dabee.promise.databinding.ActivityGroupPromiseAddBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*


class GroupActivityPromiseAdd : AppCompatActivity() {

    //	Zb2rfa2mmu%2BbKTIpNHoc4ao2gs09wedtsqFnGyAzTeFcRsbBPYaiLzCrVD6El0paOABWq5%2FFuVfwFpls8uns2Q%3D%3D
    val binding by lazy { ActivityGroupPromiseAddBinding.inflate(layoutInflater) }
    private var setYear:Int = 0
    private var setMonth:Int = 0
    private var setDay:Int = 0
    private var setHour:Int = 0
    private var setMinute:Int = 0
    lateinit var setLineup:String

    val firebaseFirestore = FirebaseFirestore.getInstance()
    val userRef = firebaseFirestore.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent = intent

        binding.tvGroupName.text=intent.getStringExtra("groupName")

        dateSet()
        binding.iv.setOnClickListener { finish() }
        binding.tvDate.setOnClickListener { setupDate() }
        binding.tvTime.setOnClickListener { setupTime() }
        binding.btnSave.setOnClickListener { saveData() }


        binding.etNote.setOnFocusChangeListener { v, hasFocus ->

            if(hasFocus){
                binding.nsScroll.postDelayed(Runnable {
                    kotlin.run {
                        binding.nsScroll.smoothScrollBy(0,300)
                    }
                },100)
            }
        }
        binding.etTitle.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.nsScroll.postDelayed(Runnable {
                    kotlin.run {
                        binding.nsScroll.smoothScrollBy(0,-300)
                    }
                },100)
            }
        }

    }////on create

    private fun saveData(){
        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()


        val title = binding.etTitle.text
        val place = binding.etPlace.text
        val date = binding.tvDate.text
        val time = binding.tvTime.text
        val note = binding.etNote.text
        val groupName = intent.getStringExtra("groupName")

        setLineup = "$time"
        setLineup = setLineup.replace(" ", "")
        setLineup =setLineup.replace("??????","")
        setLineup =setLineup.replace("???","")
        setLineup =setLineup.replace("???","")
        if(setLineup.indexOf("??????")!=-1){
            setLineup = setLineup.replace("??????","")
            var ss:Int = setLineup.toInt()
            if (setLineup.length == 3){
                var buf = StringBuffer(setLineup)
                setLineup= buf.insert(0,"0").toString()
            }
            setLineup = (ss+1200).toString()

        }else if (setLineup.length == 3){
            var buf = StringBuffer(setLineup)
            setLineup= buf.insert(0,"0").toString()
        }

        setLineup = "$date$setLineup"
        setLineup = setLineup.replace(" ", "")
        setLineup =setLineup.replace("???", "")
        setLineup =setLineup.replace("???","")
        setLineup =setLineup.replace("???","")




        if(title.isBlank()){
            Toast.makeText(this, "?????? ????????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
            return
        }
        if(place.isBlank()){
            Toast.makeText(this, "????????? ????????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
            return
        }


        var promise: MutableMap<String, String> = HashMap()
        promise["title"] = title.toString()
        promise["place"] = place.toString()
        promise["date"] = date.toString()
        promise["time"] = time.toString()
        promise["note"] = note.toString()
        promise["groupName"] = groupName!!
        promise["setLineup"] = setLineup
        promise["docName"] = "$title,$setLineup"




        AlertDialog.Builder(this).setTitle("$title").setMessage("\n??????????????? : $place\n\nDate : $date $time\n").setPositiveButton("??????") { d, w ->

            userRef.document(userId).collection("groups").document(groupName!!).collection("members").get().addOnSuccessListener { result->
                for (doc in result){
                    var isJoin = doc.get("isJoin") as Boolean
                    if (isJoin){
                        userRef.document(doc.id).collection("groups").document(groupName!!).collection("promise").document("$title$setLineup").set(promise)
                    }
                }
            }
            val intent = intent
            setResult(RESULT_OK, intent)

            finish()


        }.setNegativeButton("??????"){d,w-> return@setNegativeButton }.show()






    }

   private fun dateSet(){


       val mcurrentTime = Calendar.getInstance()
       var hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
       var minute = mcurrentTime.get(Calendar.MINUTE)
       var min:String = minute.toString()

       var amPm:String = "??????"
       if (hour>11) {
           amPm = "??????"
           hour -= 12
       }
       if (minute<10) min = "0$minute"

       binding.tvTime.text = "$amPm ${hour}??? ${min}???"

       var date = SimpleDateFormat("yyyy??? MM??? dd???").format(Date())
       binding.tvDate.text = date


       val now = Calendar.getInstance()

       setYear = now[Calendar.YEAR]
       setMonth = now[Calendar.MONTH]
       setDay = now[Calendar.DAY_OF_MONTH]

       setHour = now[Calendar.HOUR_OF_DAY]
       setMinute = now[Calendar.MINUTE]

   }


    private fun setupTime(){

        val now = Calendar.getInstance()

        val picker: TimePickerDialog = TimePickerDialog.newInstance(TimePickerDialog.OnTimeSetListener{ d, h, m, s ->

            setHour=h
            setMinute=m

            var minute:String = m.toString()
            var hour:Int = h
            var amPm:String = "??????"
            if (h>11) {
                amPm = "??????"
                hour -= 12
            }
            if (m<10) minute = "0$minute"




            binding.tvTime.text = "$amPm ${hour}??? ${minute}???"
        },setHour,setMinute,false)
        picker.version = TimePickerDialog.Version.VERSION_2
        picker.show(supportFragmentManager, "TimePickerDialog")


    }

    private fun setupDate() {

        val picker:DatePickerDialog = DatePickerDialog.newInstance(DatePickerDialog.OnDateSetListener{dialog,y,m,d->

            setYear = y
            setMonth = m
            setDay = d

            val calendar = GregorianCalendar(y, m, d)
            var date = SimpleDateFormat("yyyy??? MM??? dd???").format(calendar.time)
            binding.tvDate.text = date


        },setYear,setMonth,setDay)
        picker.version = DatePickerDialog.Version.VERSION_2
        picker.show(supportFragmentManager,"DatePickerDialog")



    }





}







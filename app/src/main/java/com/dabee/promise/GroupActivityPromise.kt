package com.dabee.promise

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.Display
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.dabee.promise.databinding.ActivityGroupPromiseBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.w3c.dom.Text
import java.security.AccessController.getContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class GroupActivityPromise : AppCompatActivity() {
    val binding by lazy { ActivityGroupPromiseBinding.inflate(layoutInflater) }

    val firebaseFirestore = FirebaseFirestore.getInstance()
    val userRef = firebaseFirestore.collection("users")

    var membersItem:MutableList<FriendsItem> = mutableListOf()
    var memoItem:MutableList<Memo> = mutableListOf()


    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()
        intent =intent
        var groupName = intent.getStringExtra("groupName")
        var promiseName = intent.getStringExtra("promise")
        var dday = intent.getStringExtra("dday")
        if (dday == "Today"){
            binding.tvDday.setTextColor(Color.parseColor("#FFFF0000"))
        }else {
            binding.tvDday.setTextColor(Color.parseColor("#FF000000"))
        }
        binding.tvDday.text = dday


        binding.iv.setOnClickListener { finish() }
        binding.linearMembers.setOnClickListener { showMembers() }

        binding.linearMemo.setOnClickListener { writeMemo() }

        dataSet()
        userRef.document(userId).collection("groups").document(groupName!!).collection("promise").document(promiseName!!).collection("memo").orderBy("date", Query.Direction.DESCENDING)
            .limit(1).addSnapshotListener{s,e ->
            readMemo()
        }

    }

    override fun onResume() {
        super.onResume()

        readMemo()

    }

    private fun dataSet(){
        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()
        intent =intent
        var groupName = intent.getStringExtra("groupName")
        var promiseName = intent.getStringExtra("promise")



        userRef.document(userId).collection("groups").document(groupName!!).collection("promise").document(promiseName!!).get().addOnSuccessListener {

            binding.tvGroupName.text = it["groupName"] as String
            binding.tvTitle.text = it["title"] as String
            binding.tvPlace.text = it["place"] as String
            binding.tvDate.text = it["date"] as String
            binding.tvTime.text = it["time"] as String
            binding.tvNote.text = it["note"] as String

            
        }

    }

    private fun readMemo(){

        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()
        intent =intent
        var groupName = intent.getStringExtra("groupName")
        var promiseName = intent.getStringExtra("promise")





        binding.rvMemo.adapter = RecyclerAdapterMemo(this,memoItem)

        userRef.document(userId).collection("groups").document(groupName!!).collection("promise").document(promiseName!!).collection("memo").get().addOnSuccessListener { result ->
            memoItem.clear()
            for(doc in result){
                val item = Memo(doc.get("userName") as String,doc.get("userImgUrl") as String,doc.get("memo") as String,doc.get("date") as String,groupName,promiseName,doc.get("userId") as String,doc.get("time") as String)
                memoItem.add(item)



            }

            memoItem.sortWith(compareBy { it.date.toLong()})
            binding.rvMemo.adapter?.notifyDataSetChanged()
            binding.rvMemo.scrollToPosition(memoItem.size-1)


        }


    }


    private fun writeMemo(){

        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()
        intent =intent
        var groupName = intent.getStringExtra("groupName")
        var promiseName = intent.getStringExtra("promise")


        val builder = AlertDialog.Builder(binding.root.context)
        val tvCode = TextView(binding.root.context)
        tvCode.text = ""
        tvCode.textSize = 4F
        val etMemo = EditText(binding.root.context)
        etMemo.hint = " 메모를 입력해 주세요"
        etMemo.isSingleLine = false
        val mLayout = LinearLayout(binding.root.context)
        mLayout.orientation = LinearLayout.VERTICAL
        mLayout.setPadding(80)
        mLayout.addView(tvCode)
        tvCode.marginTop
        mLayout.addView(etMemo)
        builder.setView(mLayout)
        builder.setTitle("메모")


        builder.setPositiveButton("추가") { dialog, which ->

            var memo:String = etMemo.text.toString()
            if (memo.length < 1) memo= " "

            var date = SimpleDateFormat("yyyyMMddHHmmss").format(Date())

            userRef.document(userId).get().addOnSuccessListener {

                // 타임스탬프를 한국 시간, 문자열로 바꿈
                val sf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA)
                sf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                val time = sf.format(Date())


                var set: MutableMap<String, String> = java.util.HashMap()
                set["userName"] = it["userName"] as String
                set["userImgUrl"] = it["userImgUrl"] as String
                set["memo"] = memo
                set["userId"] = userId
                set["time"] = time
                set["date"] = date



                userRef.document(userId).collection("groups").document(groupName!!).collection("members").get().addOnSuccessListener { result ->
                    for (doc in result){

                        val isJoin:Boolean = doc.get("isJoin") as Boolean
                        if (isJoin){
                            userRef.document(doc.id).collection("groups").document(groupName!!).collection("promise").document(promiseName!!).collection("memo").document("memo_$date").set(set)
                        }

                    }

                }

            }

            Toast.makeText(this, "메모 작성 완료", Toast.LENGTH_SHORT).show()


        }
        builder.setNegativeButton("취소") { dialog, which ->

        }
        builder.show()
    }

    private fun showMembers(){

        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bs_members)

        intent =intent
        var groupName = intent.getStringExtra("groupName")

        val recycler: RecyclerView? = bottomSheetDialog.findViewById<RecyclerView>(R.id.bs_rycycler)



        recycler?.adapter = RecyclerAdapterMembers(this,membersItem)
        userRef.document(userId).collection("groups").document(groupName!!).collection("members").get().addOnSuccessListener { result->
            membersItem.clear()
            for (doc in result){
                var isJoin: Boolean = doc.get("isJoin") as Boolean
                userRef.document(doc.id).get().addOnSuccessListener {
                    if (isJoin) {
                        var isJoin: MutableMap<String, Boolean> = HashMap()
                        isJoin["isJoin"] = true
                        userRef.document(userId).collection("groups").document(groupName!!).collection("members").document(doc.id).set(it)
                        userRef.document(userId).collection("groups").document(groupName!!).collection("members").document(doc.id).update(isJoin as Map<String, Any>)
                    }

                }
                if(isJoin){
                    var datas: MutableMap<String, String> = doc["data"] as MutableMap<String, String> // 해시맵

                    val item = FriendsItem(datas["userName"]as String,datas["userImgUrl"]as String,datas["userId"]as String)

                    membersItem.add(item)
                }

            }
            recycler?.adapter?.notifyDataSetChanged()
        }
        bottomSheetDialog.show()

    }





}
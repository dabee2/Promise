package com.dabee.promise

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabee.promise.databinding.ActivityAddGroupBinding
import com.google.firebase.firestore.FirebaseFirestore

class GroupAddActivity : AppCompatActivity() {

    val binding by lazy { ActivityAddGroupBinding.inflate(layoutInflater)}
    private var recyclerViewAdapter: RecyclerAdapterFriendsCB? = null
    val firebaseFirestore = FirebaseFirestore.getInstance()
    val userRef = firebaseFirestore.collection("users")
    lateinit var pref:SharedPreferences
    lateinit var userId:String

    var friends:MutableList<FriendsItem> = mutableListOf()

    // 데이터베이스에서 내정보 불러오기




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        pref = this@GroupAddActivity.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        userId= pref.getString("userId", null).toString()

        friendLoad()

        binding.btnSave.setOnClickListener { groupSave() }
        binding.ivBack.setOnClickListener { finish() }



    }



    private fun groupSave(){

        friends = recyclerViewAdapter?.checkedResult()!!
        var isJoin: MutableMap<String, Boolean> = java.util.HashMap()
        var groupNameSet: MutableMap<String, String> = java.util.HashMap()
        var str:StringBuffer = StringBuffer()

        if(friends.size==0){
            Toast.makeText(this, "그룹원 지정 안됨", Toast.LENGTH_SHORT).show()
            return
        }
        val groupName:String = binding.etGroupName.text.toString()
        groupName.trim()
        if (groupName.length < 1){
            Toast.makeText(this, "그룹명 지정 안됨", Toast.LENGTH_SHORT).show()
            return
        }

        for (i in friends) str.append("${i.name}, ")
        str.deleteCharAt(str.length-2)

        AlertDialog.Builder(this).setTitle("Group : $groupName").setMessage("Members\n\n$str").setPositiveButton("저장") { d, w ->
            groupNameSet["groupName"]=groupName
            userRef.document(userId).collection("groups").document(groupName).set(groupNameSet)
            for (i in friends){
                // 내 groups 에 그룹원들 추가
                userRef.document(i.id).get().addOnSuccessListener {
                    isJoin["isJoin"] = false
                    userRef.document(userId).collection("groups").document(groupName).collection("members").document(i.id).set(it)
                    userRef.document(userId).collection("groups").document(groupName).collection("members").document(i.id).update(isJoin as Map<String, Any>)
                    userRef.document(userId).get().addOnSuccessListener { it2 ->
                        isJoin["isJoin"]=true
                        userRef.document(i.id).collection("join").document(groupName).set(it2)
                        userRef.document(i.id).collection("groups").document(groupName).collection("members").document(userId).set(it2)
                        userRef.document(i.id).collection("groups").document(groupName).collection("members").document(userId).update(isJoin as Map<String, Any>)
                    }
                }
                val intent = intent
                setResult(RESULT_OK, intent)

                finish()

            }
        }.setNegativeButton("취소"){d,w-> return@setNegativeButton }.show()



    }

    private fun friendLoad(){



        binding.rycycler.apply {
            recyclerViewAdapter = RecyclerAdapterFriendsCB()
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(this@GroupAddActivity)
        }



        userRef.document(userId).collection("friends").get().addOnSuccessListener { result ->

            for (document in result){

                // 데이터가 바뀐 친구 갱신
                var code = document["id"]as String
                userRef.document(code).get().addOnSuccessListener {
                    userRef.document(userId).collection("friends").document(it.get("userId") as String).set(it)
                }



                var datas: MutableMap<String, String> = document["data"] as MutableMap<String, String> // 해시맵


                val item = FriendsItem(datas["userName"]as String,datas["userImgUrl"]as String,datas["userId"]as String)

                recyclerViewAdapter?.addUserItems(item)
            }


        }


    }


}
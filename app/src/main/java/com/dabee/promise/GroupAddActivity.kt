package com.dabee.promise

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabee.promise.databinding.ActivityAddGroupBinding
import com.google.firebase.firestore.FirebaseFirestore

class GroupAddActivity : AppCompatActivity() {

    val binding by lazy { ActivityAddGroupBinding.inflate(layoutInflater)}
    private var recyclerViewAdapter: RecyclerAdapterFriendsRadio? = null

    var friedns:MutableList<FriendsItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        friendLoad()

        binding.btnSave.setOnClickListener { groupSave() }




    }

    private fun groupSave(){
        recyclerViewAdapter
    }

    private fun friendLoad(){

        binding.rycycler.apply {
            recyclerViewAdapter = RecyclerAdapterFriendsRadio()
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(this@GroupAddActivity)
        }



        // 데이터베이스에서 내정보 불러오기
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val userRef = firebaseFirestore.collection("users")
        val pref = this.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()


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
package com.dabee.promise

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabee.promise.databinding.ActivityAddGroupBinding
import com.google.firebase.firestore.FirebaseFirestore

class GroupAddActivity : AppCompatActivity() {

    val binding by lazy { ActivityAddGroupBinding.inflate(layoutInflater)}
    private var recyclerViewAdapter: RecyclerAdapterFriendsCB? = null

    var friends:MutableList<FriendsItem> = mutableListOf()
    // 데이터베이스에서 내정보 불러오기




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        friendLoad()

        binding.btnSave.setOnClickListener {

            groupSave()

            val intent = intent
//            intent.putExtra("title", friends)

//            intent.putExtra("da", friends)
//            intent.putExtra("price", price) // Double


            // 결과를 주었다고 명시[이게 결과!]
//            setResult(RESULT_OK, intent)


            //작성완료 했으니 EditActivity를 종료
//            finish()


        }
        binding.ivBack.setOnClickListener { finish() }



    }



    private fun groupSave(){

        friends = recyclerViewAdapter?.resultChecked()!!
        if(friends.size==0) return
//        userRef.document(userId).collection("Group").get().addOnCompleteListener {
//
//        }


        val str:StringBuffer = StringBuffer()
        for (i in friends){
            str.append(i.name)
        }

        AlertDialog.Builder(this).setMessage(str).show()

    }

    private fun friendLoad(){



        binding.rycycler.apply {
            recyclerViewAdapter = RecyclerAdapterFriendsCB()
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(this@GroupAddActivity)
        }
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val userRef = firebaseFirestore.collection("users")
        val pref = this@GroupAddActivity.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
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
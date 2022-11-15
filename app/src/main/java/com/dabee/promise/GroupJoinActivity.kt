package com.dabee.promise

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.dabee.promise.databinding.ActivityJoinGroupBinding
import com.google.firebase.firestore.FirebaseFirestore

class GroupJoinActivity : AppCompatActivity() {
    val binding by lazy { ActivityJoinGroupBinding.inflate(layoutInflater) }

    var joins:MutableList<GroupsItem2> = mutableListOf()

    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val userRef = firebaseFirestore.collection("users")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        groupLoad()
        binding.iv.setOnClickListener {
            val intent = intent
            setResult(RESULT_OK, intent)

            finish()
            }


    }



    private fun groupLoad() {

        binding.recycler.adapter = RecyclerAdapterJoinGroups(this, joins)


        // 데이터베이스에서 내정보 불러오기

        val pref = getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId: String = pref.getString("userId", null).toString()


        userRef.document(userId).collection("join").get().addOnSuccessListener { result ->

            joins.clear()
            //그룹 불러오기
            for (group in result) {

                var groupId:String = group.get("id") as String
                val item2 = GroupsItem2(group.id,groupId)
                joins.add(item2)

            }

            binding.recycler.adapter?.notifyDataSetChanged()


        }

    }



}
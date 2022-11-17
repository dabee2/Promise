package com.dabee.promise

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.dabee.promise.databinding.RecyclerItemGroupBinding
import com.google.firebase.firestore.FirebaseFirestore


class RecyclerAdapterJoinGroups constructor(val context:Context, var items:MutableList<GroupsItem2>): RecyclerView.Adapter<RecyclerAdapterJoinGroups.VH>(){
    var friends:MutableList<FriendsItem2> = mutableListOf()

    inner class VH constructor(itemView: View) : RecyclerView.ViewHolder(itemView){

        val binding:RecyclerItemGroupBinding = RecyclerItemGroupBinding.bind(itemView)

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)
        val itemView:View = layoutInflater.inflate(R.layout.recycler_item_group,parent,false)
        return VH(itemView)
    }



    override fun onBindViewHolder(holder: VH, position: Int) {

        // 데이터베이스에서 내정보 불러오기
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val userRef = firebaseFirestore.collection("users")
        val pref = context.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()


        val item:GroupsItem2 = items.get(position)
        holder.binding.tvGroupName2.text = item.groupName
        holder.binding.rycyclerRycycler.adapter = RecyclerAdapterGroupChild(context,friends)
        friends.clear()
        holder.binding.rycyclerRycycler.adapter?.notifyDataSetChanged()

        holder.binding.tvMembers2.setOnClickListener{
            holder.binding.rycyclerRycycler.visibility = View.VISIBLE
            holder.binding.tvMembers2.visibility = View.GONE

            userRef.document(item.groupId).collection("groups").document(item.groupName).collection("members").get().addOnSuccessListener { result ->
                friends.clear()
                for (doc in result){

                    userRef.document(doc.id).get().addOnSuccessListener {
                        var isJoin: Boolean = doc.get("isJoin") as Boolean
                        if (isJoin) {
                            var isJoin: MutableMap<String, Boolean> = HashMap()
                            isJoin["isJoin"] = true
                            userRef.document(userId).collection("groups").document(item.groupName).collection("members").document(doc.id).set(it)
                            userRef.document(userId).collection("groups").document(item.groupName).collection("members").document(doc.id).update(isJoin as Map<String, Any>)
                        }

                    }
                    var datas: MutableMap<String, String> = doc["data"] as MutableMap<String, String> // 해시맵
                    val item = FriendsItem2(datas["userName"]as String,datas["userImgUrl"]as String,datas["userId"]as String,doc.get("isJoin") as Boolean)

                    friends.add(item)



                }

                holder.binding.rycyclerRycycler.adapter?.notifyDataSetChanged()

            }


        }
        holder.binding.tvMembers.setOnClickListener{
            holder.binding.rycyclerRycycler.visibility = View.GONE
            holder.binding.tvMembers2.visibility = View.VISIBLE
            friends.clear()
            holder.binding.rycyclerRycycler.adapter?.notifyDataSetChanged()
        }





        holder.binding.iv2.setOnClickListener {

            AlertDialog.Builder(context).setTitle("그룹 초대").setMessage("\n${item.groupName} 초대를 받으시겠습니까?").setNegativeButton("거절"){ dialog, v->

                userRef.document(item.groupId).collection("groups").document(item.groupName).collection("members").document(userId).delete().addOnSuccessListener {
                    userRef.document(userId).collection("join").document(item.groupName).delete().addOnSuccessListener {  }
                }
                items.remove(item)
                notifyItemRemoved(position)

            }.setPositiveButton("수락"){ dialog, d->
                var groupNameSet: MutableMap<String, String> = java.util.HashMap()
                groupNameSet["groupName"]= item.groupName
                userRef.document(userId).collection("groups").document(item.groupName).set(groupNameSet)
                userRef.document(item.groupId).collection("groups").document(item.groupName).collection("members").document(userId).get().addOnSuccessListener {
                    var isJoin: MutableMap<String, Boolean> = HashMap()
                    isJoin["isJoin"] = true
                    userRef.document(item.groupId).collection("groups").document(item.groupName).collection("members").document(userId).update(isJoin as Map<String, Any>)
                    userRef.document(item.groupId).collection("groups").document(item.groupName).collection("members").get().addOnSuccessListener { result ->
                        for (doc in result){
                            userRef.document(doc.id).get().addOnSuccessListener {
                                var isJoin: Boolean = doc.get("isJoin") as Boolean
                                if (isJoin) {
                                    var isJoin: MutableMap<String, Boolean> = HashMap()
                                    isJoin["isJoin"] = true
                                    userRef.document(userId).collection("groups").document(item.groupName).collection("members").document(doc.id).set(it)
                                    userRef.document(userId).collection("groups").document(item.groupName).collection("members").document(doc.id).update(isJoin as Map<String, Any>)
                                }else if(!isJoin){
                                    var isJoin: MutableMap<String, Boolean> = HashMap()
                                    isJoin["isJoin"] = false
                                    userRef.document(userId).collection("groups").document(item.groupName).collection("members").document(doc.id).set(it)
                                    userRef.document(userId).collection("groups").document(item.groupName).collection("members").document(doc.id).update(isJoin as Map<String, Any>)
                                }
                            }
                        }
                    }
                }


                userRef.document(userId).collection("join").document(item.groupName).delete().addOnSuccessListener {  }
                items.remove(item)
                notifyItemRemoved(position)
            }.show()

//            return@setOnClickListener
        }


    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
//        holder.binding.rycyclerRycycler.adapter = RecyclerAdapterGroupChild(context,friends)
//        friends.clear()
//        holder.binding.rycyclerRycycler.adapter?.notifyDataSetChanged()

    }

    override fun getItemViewType(position: Int): Int {


        return position
    }


    //함수의 return 코드 단순화 [ 할당 연산자 = ]
    override fun getItemCount(): Int = items.size


}




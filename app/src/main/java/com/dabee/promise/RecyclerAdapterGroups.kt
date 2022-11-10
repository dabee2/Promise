package com.dabee.promise

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabee.promise.databinding.RecyclerItemGroupBinding
import com.dabee.promise.databinding.RecyclerItemMembershipBinding
import com.google.firebase.firestore.FirebaseFirestore


class RecyclerAdapterGroups constructor(val context:Context, var items:MutableList<GroupsItem>): RecyclerView.Adapter<RecyclerAdapterGroups.VH>(){

    var friends:MutableList<FriendsItem> = mutableListOf()

    inner class VH constructor(itemView: View) : RecyclerView.ViewHolder(itemView){

        val binding:RecyclerItemGroupBinding = RecyclerItemGroupBinding.bind(itemView)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)

        val itemView:View = layoutInflater.inflate(R.layout.recycler_item_group,parent,false)

        return VH(itemView)
    }



    override fun onBindViewHolder(holder: VH, position: Int) {


        val item:GroupsItem = items.get(position)
        holder.binding.tvGroupName.text = item.groupName


        fun friendLoad(){

            holder.binding.rycyclerRycycler.adapter = RecyclerAdapterGroupChild(context,friends)
            // 데이터베이스에서 내정보 불러오기
            val firebaseFirestore = FirebaseFirestore.getInstance()
            val userRef = firebaseFirestore.collection("users")
            val pref = context.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
            val userId:String= pref.getString("userId", null).toString()


            userRef.document(userId).collection("groups").document(item.groupName).collection("members").get().addOnSuccessListener { result ->
                friends.clear()
                for (doc in result){

                    // 데이터가 바뀐 친구 갱신
                    userRef.document(doc.id).get().addOnSuccessListener {
                        var isJoin:Boolean = doc.get("isJoin")as Boolean
                        if(isJoin!=false) userRef.document(userId).collection("groups").document(item.groupName).collection("members").document(doc.id).set(it)

                    }

                    var datas: MutableMap<String, String> = doc["data"] as MutableMap<String, String> // 해시맵
                    val item = FriendsItem(datas["userName"]as String,datas["userImgUrl"]as String,datas["userId"]as String)
                    friends.add(item)


                }
                holder.binding.rycyclerRycycler.adapter?.notifyDataSetChanged()
            }
        }
        friendLoad()





//        holder.itemView.setOnLongClickListener {
//
//            AlertDialog.Builder(context).setTitle("친구삭제").setMessage("\n${item.name} 님을 삭제하시겠습니까?").setNegativeButton("취소"){dialog,v->}.setPositiveButton("삭제"){dialog,d->
//
//                val firebaseFirestore = FirebaseFirestore.getInstance()
//                val userRef = firebaseFirestore.collection("users")
//                val pref = context.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
//                val userId:String= pref.getString("userId", null).toString()
//
//                userRef.document(userId).collection("friends").document(item.id).delete().addOnCompleteListener{
//
//                    if (it.isSuccessful) Toast.makeText(context,"삭제완료",Toast.LENGTH_SHORT).show()
//                    userRef.document(item.id).collection("friends").document(userId).delete().addOnSuccessListener { }
//
//                }
//
//                items.remove(FriendsItem(item.name,item.img,item.id))
////                notifyItemChanged(position)
//                notifyDataSetChanged()
//            }.show()
//
//            return@setOnLongClickListener true
//        }


    }


    //함수의 return 코드 단순화 [ 할당 연산자 = ]
    override fun getItemCount(): Int = items.size


}




package com.dabee.promise

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabee.promise.databinding.RecyclerItemMembershipBinding
import com.google.firebase.firestore.FirebaseFirestore

class RecyclerAdapterFriends constructor(val context:Context, var items:MutableList<FriendsItem>): RecyclerView.Adapter<RecyclerAdapterFriends.VH>(){

    inner class VH constructor(itemView: View) : RecyclerView.ViewHolder(itemView){

        val binding:RecyclerItemMembershipBinding = RecyclerItemMembershipBinding.bind(itemView)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)

        val itemView:View = layoutInflater.inflate(R.layout.recycler_item_membership,parent,false)

        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val item:FriendsItem = items.get(position)
        holder.binding.tvName.text = item.name
        Glide.with(context).load(item.img).error(R.drawable.images).into(holder.binding.civImg)


        holder.itemView.setOnLongClickListener {

            AlertDialog.Builder(context).setTitle("친구삭제").setMessage("\n${item.name} 님을 삭제하시겠습니까?").setNegativeButton("취소"){dialog,v->}.setPositiveButton("삭제"){dialog,d->

                val firebaseFirestore = FirebaseFirestore.getInstance()
                val userRef = firebaseFirestore.collection("users")
                val pref = context.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
                val userId:String= pref.getString("userId", null).toString()

                userRef.document(userId).collection("friends").document(item.id).delete().addOnCompleteListener{

                    if (it.isSuccessful) Toast.makeText(context,"삭제완료",Toast.LENGTH_SHORT).show()
                    userRef.document(item.id).collection("friends").document(userId).delete().addOnSuccessListener { }

                }

                items.remove(FriendsItem(item.name,item.img,item.id))
                notifyDataSetChanged()
            }.show()

            return@setOnLongClickListener true
        }


    }

    //함수의 return 코드 단순화 [ 할당 연산자 = ]
    override fun getItemCount(): Int = items.size


}




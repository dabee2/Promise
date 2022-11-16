package com.dabee.promise

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabee.promise.databinding.RecyclerItemBinding
import com.dabee.promise.databinding.RecyclerItemMiniBinding
import com.google.firebase.firestore.FirebaseFirestore

class RecyclerAdapterMini constructor(val context:Context, var items:MutableList<PromiseItem>): RecyclerView.Adapter<RecyclerAdapterMini.VH>(){

    inner class VH constructor(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding:RecyclerItemMiniBinding = RecyclerItemMiniBinding.bind(itemView)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)
        val itemView:View = layoutInflater.inflate(R.layout.recycler_item_mini,parent,false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val firebaseFirestore = FirebaseFirestore.getInstance()
        val userRef = firebaseFirestore.collection("users")
        val pref = context.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()


        val item:PromiseItem = items.get(position)
        holder.binding.tvTitle2.text = item.title
        holder.binding.tvPlace2.text = item.place
        holder.binding.tvDateTime2.text = "${item.date} ${item.time}"
        holder.binding.tvMemo.text = item.note


        holder.itemView.setOnLongClickListener {

            AlertDialog.Builder(context).setTitle("일정 삭제").setMessage("\n${item.title} 을(를) 삭제하시겠습니까?").setNegativeButton("취소"){ dialog, v->}.setPositiveButton("나가기"){ dialog, d->

                userRef.document(userId).collection("groups").document(item.groupName).collection("promise").document("${item.title}${item.date}${item.time}").delete().addOnSuccessListener {



                }
                items.remove(item)
                notifyItemRemoved(position)

            }.show()

            return@setOnLongClickListener true
        }



    }

    //함수의 return 코드 단순화 [ 할당 연산자 = ]
    override fun getItemCount(): Int = items.size


}
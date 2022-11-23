package com.dabee.promise

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RecyclerAdapter constructor(val context:Context, var items:MutableList<Item>): RecyclerView.Adapter<RecyclerAdapter.VH>(){

    inner class VH constructor(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding:RecyclerItemBinding = RecyclerItemBinding.bind(itemView)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)
        val itemView:View = layoutInflater.inflate(R.layout.recycler_item,parent,false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val firebaseFirestore = FirebaseFirestore.getInstance()
        val userRef = firebaseFirestore.collection("users")
        val pref = context.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()

        val item:Item = items.get(position)

        var dday =  item.setLineup.substring(0,8)

        var dday2 = SimpleDateFormat("yyyyMMdd").format(Date())
        var dday3 = (dday2.toInt() - dday.toInt()).toString()
        if (dday3.toInt() == 0){
            dday3 = "Today"
            holder.binding.tvDDay.setTextColor(Color.parseColor("#FFFF0000"))
        }else if (dday3.toInt() > 0){
            dday3 = "D+$dday3"
            holder.binding.tvDDay.setTextColor(Color.parseColor("#FF000000"))
        } else {
            dday3 = "D$dday3"
            holder.binding.tvDDay.setTextColor(Color.parseColor("##FF000000"))
        }


        holder.binding.tvTitle.text = item.title
        holder.binding.tvPlace2.text = item.place
        holder.binding.tvGroup.text = item.groupName
        holder.binding.tvTime.text = item.time
        holder.binding.tvDDay.text = dday3




        holder.itemView.setOnClickListener {

            var intent = Intent(context,GroupActivityPromise::class.java)
            intent.putExtra("groupName",item.groupName)
            intent.putExtra("promise","${item.title}${item.setLineup}")
            context.startActivity(intent)


        }


        holder.itemView.setOnLongClickListener {
            Toast.makeText(holder.binding.root.context, "$dday\n$dday2", Toast.LENGTH_SHORT).show()
            AlertDialog.Builder(context).setTitle("일정 삭제").setMessage("\n${item.title} 을(를) 삭제하시겠습니까?").setNegativeButton("취소"){ dialog, v->}.setPositiveButton("삭제"){ dialog, d->

                userRef.document(userId).collection("groups").document(item.groupName).collection("promise").document("${item.title}${item.setLineup}").delete().addOnSuccessListener {


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
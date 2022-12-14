package com.dabee.promise

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.dabee.promise.databinding.RecyclerItemBinding
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class RecyclerAdapter constructor(val context:Context, var items:MutableList<Item8>): RecyclerView.Adapter<RecyclerAdapter.VH>(){

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

        val item:Item8 = items.get(position)

        var dday =  item.setLineup.substring(0,8)
        val dateFormat = SimpleDateFormat("yyyyMMdd")
        val endDate = dateFormat.parse(dday).time
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time.time

        var dday3= "${(today - endDate) / (24 * 60 * 60 * 1000)}"

        if (dday3.toInt() == 0){
            dday3 = "Today"
            holder.binding.tvDDay.setTextColor(Color.parseColor("#FFFF0000"))
        }else if (dday3.toInt() > 0){
            dday3 = "D+$dday3"
            holder.binding.tvDDay.setTextColor(Color.parseColor("#FF000000"))
        } else {
            dday3 = "D$dday3"
            holder.binding.tvDDay.setTextColor(Color.parseColor("#FF000000"))
        }

        userRef.document(userId).collection("groups").document(item.groupName).collection("promise").document("${item.title}${item.setLineup}").get().addOnSuccessListener {
            var weather = it.get("weather")
            var temperature = it.get("temperature")
            if (weather != null){
                holder.binding.tvWeather.text = weather.toString()
            }else{
                holder.binding.tvWeather.text = ""
            }

            if (temperature != null){
                holder.binding.tvTemperature.text = temperature.toString()
            }else{
                holder.binding.tvTemperature.text = ""
            }


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
            intent.putExtra("dday",dday3)
            context.startActivity(intent)


        }


        holder.itemView.setOnLongClickListener {

            AlertDialog.Builder(context).setTitle("?????? ??????").setMessage("\n${item.title} ???(???) ?????????????????????????").setNegativeButton("??????"){ dialog, v->}.setPositiveButton("??????"){ dialog, d->

                userRef.document(userId).collection("groups").document(item.groupName).collection("promise").document("${item.title}${item.setLineup}").delete().addOnSuccessListener {


                }
                items.remove(item)
                notifyItemRemoved(position)


            }.show()

            return@setOnLongClickListener true
        }



    }

    //????????? return ?????? ????????? [ ?????? ????????? = ]
    override fun getItemCount(): Int = items.size






}
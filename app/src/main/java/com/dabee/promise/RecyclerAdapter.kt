package com.dabee.promise

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabee.promise.databinding.RecyclerItemBinding

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
        val item:Item = items.get(position)

//        holder.tvTitle.setText(item.title)
        //코틀린 언어는 setXXX()를 싫어함. 그냥 xxx를 변수처럼 = 대입연산자로 값 설정 권장함
//        holder.tvMsg.text = item.msg
        holder.binding.tvTitle.text = item.title
        holder.binding.tvPlace.text = item.place
        holder.binding.tvGroup.text = item.group
        holder.binding.tvTime.text = item.time


    }

    //함수의 return 코드 단순화 [ 할당 연산자 = ]
    override fun getItemCount(): Int = items.size


}
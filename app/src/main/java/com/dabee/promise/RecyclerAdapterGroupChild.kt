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
import com.dabee.promise.databinding.RecyclerItemGroupChildBinding
import com.dabee.promise.databinding.RecyclerItemMembershipBinding
import com.google.firebase.firestore.FirebaseFirestore

class RecyclerAdapterGroupChild constructor(val context:Context, var items:MutableList<FriendsItem>): RecyclerView.Adapter<RecyclerAdapterGroupChild.VH>(){

    inner class VH constructor(itemView: View) : RecyclerView.ViewHolder(itemView){

        val binding:RecyclerItemGroupChildBinding = RecyclerItemGroupChildBinding.bind(itemView)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)

        val itemView:View = layoutInflater.inflate(R.layout.recycler_item_group_child,parent,false)

        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val item:FriendsItem = items.get(position)
        holder.binding.tvName.text = item.name
        Glide.with(context).load(item.img).error(R.drawable.images).into(holder.binding.civImg)


    }

    //함수의 return 코드 단순화 [ 할당 연산자 = ]
    override fun getItemCount(): Int = items.size


}




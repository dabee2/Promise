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
import com.dabee.promise.databinding.RecyclerItemGroupAddBinding
import com.dabee.promise.databinding.RecyclerItemMembershipBinding
import com.google.firebase.firestore.FirebaseFirestore


class RecyclerAdapterFriendsRadio : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val userItems = arrayListOf<FriendsItem>()
    private val userCheckBoxStatus = arrayListOf<UserCheckStatus>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = UserItemViewHolder(RecyclerItemGroupAddBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserItemViewHolder)
            holder.bind(userItems[position], userCheckBoxStatus[position])


    }

    override fun getItemCount(): Int = if (userItems.isNullOrEmpty()) 0 else userItems.size

    fun addUserItems(friens: FriendsItem){
        userItems.add(friens)
        userCheckBoxStatus.add(UserCheckStatus(userItems.size - 1, false))
        notifyItemInserted(userItems.size-1)
    }

    inner class UserItemViewHolder(private val binding: RecyclerItemGroupAddBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(userItem: FriendsItem, userStatus: UserCheckStatus) = with(binding){
            tvName.text = userItem.name
            Glide.with(binding.root.context).load(userItem.img).error(R.drawable.images).into(civImg)
            checkBtn.isChecked = userStatus.isChecked




            checkBtn.setOnClickListener {
                userStatus.isChecked = checkBtn.isChecked
                notifyItemChanged(adapterPosition)
            }
            if(checkBtn.isChecked){

            }


        }
    }
}

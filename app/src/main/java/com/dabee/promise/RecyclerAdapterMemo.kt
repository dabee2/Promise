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
import com.dabee.promise.databinding.RecyclerItemMemoBinding
import com.google.firebase.firestore.FirebaseFirestore

class RecyclerAdapterMemo constructor(val context:Context, var items:MutableList<Memo>): RecyclerView.Adapter<RecyclerAdapterMemo.VH>(){

    inner class VH constructor(itemView: View) : RecyclerView.ViewHolder(itemView){

        val binding:RecyclerItemMemoBinding = RecyclerItemMemoBinding.bind(itemView)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutInflater:LayoutInflater = LayoutInflater.from(context)

        val itemView:View = layoutInflater.inflate(R.layout.recycler_item_memo,parent,false)

        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val firebaseFirestore = FirebaseFirestore.getInstance()
        val userRef = firebaseFirestore.collection("users")
        val pref = context.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()
        val userName:String= pref.getString("userName", null).toString()

        val item:Memo = items.get(position)
        holder.binding.tvName.text = item.name
        holder.binding.tvMemo.text = item.memo
        holder.binding.tvTime.text = item.time
        Glide.with(context).load(item.img).error(R.drawable.images).into(holder.binding.civ)



        holder.itemView.setOnLongClickListener {

            if (item.userId==userId){

                AlertDialog.Builder(context).setTitle("????????????").setMessage("\n${item.memo} ??? ?????????????????????????").setNegativeButton("??????"){dialog,v->}.setPositiveButton("??????"){dialog,d->


                    userRef.document(userId).collection("groups").document(item.groupName).collection("members").get().addOnSuccessListener { result ->
                        for (doc in result){

                            val isJoin:Boolean = doc.get("isJoin") as Boolean
                            if (isJoin){
                                userRef.document(doc.id).collection("groups").document(item.groupName).collection("promise").document(item.promiseName).collection("memo").document("memo_${item.date}").delete()
                            }

                        }

                    }


                    items.remove(items[position])
                    notifyDataSetChanged()
                }.show()



            }


            return@setOnLongClickListener true
        }


    }

    //????????? return ?????? ????????? [ ?????? ????????? = ]
    override fun getItemCount(): Int = items.size


}




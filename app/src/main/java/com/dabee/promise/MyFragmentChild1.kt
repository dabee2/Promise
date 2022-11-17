package com.dabee.promise

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.dabee.promise.databinding.FragmentMyChild1Binding
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyFragmentChild1.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyFragmentChild1 constructor(var items:MutableList<Item>) : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val binding by lazy {  FragmentMyChild1Binding.inflate(layoutInflater)}
    val firebaseFirestore = FirebaseFirestore.getInstance()
    val userRef = firebaseFirestore.collection("users")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)







    }

    override fun onResume() {
        super.onResume()



        binding.recycler.adapter = RecyclerAdapter(binding.root.context,items)



    }



    private fun promiseLoad(){


        binding.recycler.adapter = RecyclerAdapter(binding.root.context,items)


        val pref = requireContext().getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()


        userRef.document(userId).collection("groups").get().addOnSuccessListener { result->
            items.clear()
            for (doc in result){
                userRef.document(userId).collection("groups").document(doc.id).collection("promise").get().addOnCompleteListener { result2->
                    for (doc2 in result2.result){

                        val item= Item(doc2.get("title")as String,doc2.get("place")as String,"${doc2.get("date")as String} ${doc2.get("time")as String}",doc.id)
                        items.add(item)

                    }

                }

            }
            binding.recycler.adapter?.notifyDataSetChanged()
//            binding.recycler.adapter?.notifyItemRangeChanged(items.size-1,items.size)
        }

    }


}
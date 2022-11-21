package com.dabee.promise

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.dabee.promise.databinding.FragmentMyBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
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
 * Use the [MyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */





class MyFragment constructor(var promiseItems:MutableList<Item>,var memoryItems:MutableList<Item>) : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null



    var myFragmentChild1 = MyFragmentChild1(promiseItems)
    var myFragmentChild2 = MyFragmentChild2(memoryItems)
    val binding by lazy {  FragmentMyBinding.inflate(layoutInflater)}
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



        val pager:ViewPager2 = view.findViewById(R.id.pager)
        val tabLayout:TabLayout = view.findViewById(R.id.tab_layout)


        val fragmentList = listOf<Fragment>(MyFragmentChild1(promiseItems),MyFragmentChild2(memoryItems))
        val adapter = PagerAdapter(childFragmentManager,lifecycle)
        adapter.fragments = fragmentList
        pager.adapter=adapter
        TabLayoutMediator(tabLayout,pager){tab,position->
            when(position){
                0 -> tab.text = "계획된 약속"
                1 -> tab.text = "추억 리스트"
            }
        }.attach()




    }
    override fun onResume() {
        super.onResume()


    }






}



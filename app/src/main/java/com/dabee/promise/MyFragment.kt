package com.dabee.promise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.dabee.promise.databinding.FragmentMyBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


var myFragmentChild1 = MyFragmentChild1()
var myFragmentChild2 = MyFragmentChild2()


class MyFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    val binding by lazy {  FragmentMyBinding.inflate(layoutInflater)}


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
        val view:View = inflater.inflate(R.layout.fragment_my, container, false)



        return view
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pager:ViewPager2 = view.findViewById(R.id.pager)
        val tabLayout:TabLayout = view.findViewById(R.id.tab_layout)

        val fragmentList = listOf<Fragment>(MyFragmentChild1(),MyFragmentChild2())
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


    companion object {

        fun newInstance(param1: String, param2: String) =
            MyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun changeFragment(fragment: Fragment) {
        childFragmentManager
            .beginTransaction()
            .replace(R.id.pager, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }



}

private fun TabLayout.setupWithViewPager(pager: ViewPager2) {

}

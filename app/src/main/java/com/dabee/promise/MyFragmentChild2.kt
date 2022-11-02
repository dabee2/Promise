package com.dabee.promise

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabee.promise.databinding.FragmentMyChild1Binding
import com.dabee.promise.databinding.FragmentMyChild2Binding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyFragmentChild2.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyFragmentChild2 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val binding by lazy {  FragmentMyChild2Binding.inflate(layoutInflater)}

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
        return inflater.inflate(R.layout.fragment_my_child2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView: RecyclerView by lazy { view.findViewById(R.id.recycler) }

        // 대량의 데이터
        var items:MutableList<Item> = mutableListOf()
//    var items2:ArrayList<Item> = arrayListOf()
        items.add(Item("title","00동","10시","ㅁㅁㅁ"))
        items.add(Item("title","00동","10시","ㅁㅁㅁ"))
        items.add(Item("title","00동","10시","ㅁㅁㅁ"))
        items.add(Item("title","00동","10시","ㅁㅁㅁ"))
        items.add(Item("title","00동","10시","ㅁㅁㅁ"))
        items.add(Item("title","00동","10시","ㅁㅁㅁ"))
        items.add(Item("title","00동","10시","ㅁㅁㅁ"))

        //리사이클러뷰에 이미 아답터 프로퍼티(멤버변수)가 있어서 별도의 아답터 참조변수 없이 그냥 생성하여 대입해주면 끝.
        recyclerView.adapter = RecyclerAdapter(binding.root.context,items)

        //리사이클러뷰의 아이템뷰들의 배치방법을 정하는 매니저를 설정
        recyclerView.layoutManager= LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
    }

    override fun onResume() {
        super.onResume()

        //보통 이때 새롭게 화면데이터 갱신하는 자 ㄱ업을 ㅏㅁㄶ이함.
        // adapter 는 nullable type 이여서 사용할때 ?. 연산자 필요
        binding.recycler.adapter?.notifyDataSetChanged()

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyFragmentChild2.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyFragmentChild2().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
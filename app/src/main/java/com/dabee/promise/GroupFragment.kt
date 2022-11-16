package com.dabee.promise

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.ColorFilter
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabee.promise.databinding.FragmentGroupBinding
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val binding by lazy { FragmentGroupBinding.inflate(layoutInflater) }

    var groups:MutableList<GroupsItem> = mutableListOf()


    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val userRef = firebaseFirestore.collection("users")

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




        binding.iv.setOnClickListener {
            val intent = Intent(context,GroupAddActivity::class.java)
            intentActivityResultLauncher.launch(intent)
        }
        binding.ivNotification.setOnClickListener {
            val intent = Intent(context,GroupJoinActivity::class.java)
            intentActivityResultLauncher.launch(intent)
        }


    }



    override fun onResume() {
        super.onResume()

        joinNotice()
        groupLoad()

    }

    private fun joinNotice(){
        val pref = requireContext().getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()
                userRef.document(userId).collection("join").get().addOnSuccessListener { result ->
                    if( result.size() > 0){
                        binding.ivNotification.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context,R.color.my_color5))
                        binding.ivNotification.setImageResource(R.drawable.ic_baseline_notifications_active_24)
                    } else{
                        binding.ivNotification.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context,R.color.black))
                        binding.ivNotification.setImageResource(R.drawable.ic_baseline_notifications_24)
                    }
            }

    }


    private fun groupLoad() {

        binding.recycler.adapter = RecyclerAdapterGroups(requireContext(), groups)


        // 데이터베이스에서 내정보 불러오기

        val pref = requireContext().getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId: String = pref.getString("userId", null).toString()


        userRef.document(userId).collection("groups").get().addOnSuccessListener { result ->


            groups.clear()
            //그룹 불러오기
            for (group in result) {

                val item2 = GroupsItem(group.id)
                groups.add(item2)

                }

            binding.recycler.adapter?.notifyDataSetChanged()

            }

}






    // 액티비티를 실행시켜주는 객체 생성- 멤버변수 위치.
    var intentActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        // 결과주는 Activity가 종료되면 실행되는 메소드

        // 돌려보낸 결과가 OK인지 .. 확인
        if (result.resultCode == Activity.RESULT_OK) {


        } else {

//            Toast.makeText(context, "그룹 만들기 취소", Toast.LENGTH_SHORT).show()
        }
    }


        companion object {
            /**
             * Use this factory method to create a new instance of
             * this fragment using the provided parameters.
             *
             * @param param1 Parameter 1.
             * @param param2 Parameter 2.
             * @return A new instance of fragment MyFragment.
             */
            // TODO: Rename and change types and number of parameters
            @JvmStatic
            fun newInstance(param1: String, param2: String) =
                MyFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
        }
}

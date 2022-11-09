package com.dabee.promise

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.dabee.promise.databinding.FragmentGroupBinding

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
        val iv:ImageView = view.findViewById(R.id.iv)




        iv.setOnClickListener {

            val intent = Intent(context,GroupAddActivity::class.java)
            intentActivityResultLauncher.launch(intent)

        }



    }

    // 액티비티를 실행시켜주는 객체 생성- 멤버변수 위치.
    var intentActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        // 결과주는 Activity가 종료되면 실행되는 메소드


        // 돌려보낸 결과가 OK인지 .. 확인
        if (result.resultCode == Activity.RESULT_OK) {
            // 돌려보낸 택배기사(Intent)로 부터 Extra 데이터를 얻어오기
            val intent = result.data
            val title = intent!!.getStringExtra("title")
            val price = intent.getDoubleExtra("price", 0.0)
        } else {
            Toast.makeText(context, "글작성을 취소하셨습니다.", Toast.LENGTH_SHORT).show()
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
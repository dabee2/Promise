package com.dabee.promise

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.dabee.promise.databinding.FragmentMembershipBinding
import com.google.firebase.firestore.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */



class MembershipFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    lateinit var binding: FragmentMembershipBinding
    lateinit var userAddr:String
    var friends:MutableList<FriendsItem> = mutableListOf()

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
        binding= FragmentMembershipBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //SharedPreference에 저장되어 있는 userId 얻어오기
        val pref = requireContext().getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()
        userAddr= pref.getString("userAddr", "").toString()
        binding.tvAddr2.text=userAddr
        val editor = pref.edit()


        binding.btn.setOnClickListener { clickBtnChange() }
        binding.btnSave.setOnClickListener {
            clickBtnSave()
            editor.putString("userAddr",userAddr)
            editor.commit()
            view.clearFocus()
            hideKeyBoard()
        }

        // 데이터베이스에서 내정보 불러오기
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val userRef = firebaseFirestore.collection("users")


        userRef.document(userId).get().addOnSuccessListener {
            val userName=it.get("userName")
            val userImg =it.get("userImgUrl")
            binding.tvNickname2.text = userName.toString()
            Glide.with(this).load(userImg).error(R.drawable.images).into(binding.civProfile)
        }

        friendLoad()







        // 친구추가 버튼 다이알로그
        binding.ivAdd.setOnClickListener {
            val builder = AlertDialog.Builder(binding.root.context)
            val tvCode = TextView(binding.root.context)
            tvCode.text = ""
            tvCode.textSize = 4F
            val etCode = EditText(binding.root.context)
            etCode.hint = " 친구 코드를 입력해 주세요"
            etCode.isSingleLine = true
            val mLayout = LinearLayout(binding.root.context)
            mLayout.orientation = LinearLayout.VERTICAL
            mLayout.setPadding(80)
            mLayout.addView(tvCode)
            tvCode.marginTop
            mLayout.addView(etCode)
            builder.setView(mLayout)
            builder.setTitle("친구 추가")


            builder.setPositiveButton("추가") { dialog, which ->

                var code:String = etCode.text.toString()
                if (code.length < 1) code= " "

                userRef.document(code).get().addOnSuccessListener {

                    var id:String? = it.get("userId") as String?
                    if(id == code ){
                        val frName = it.get("userName")as String?
                        Toast.makeText(context, "$frName 님이 친구 추가 되었습니다", Toast.LENGTH_SHORT).show()
                        // DB 친구 저장
                        userRef.document(userId).collection("friends").document(it.get("userId") as String).set(it)
                        userRef.document(userId).get().addOnSuccessListener {
                            userRef.document(code).collection("friends").document(it.get("userId") as String).set(it)
                        }
                        friendLoad()

                    }else Toast.makeText(context, "잘못된 코드번호입니다", Toast.LENGTH_SHORT).show()

                }

            }
            builder.setNegativeButton("취소") { dialog, which ->

            }
            builder.show()

        }


    }




    override fun onResume() {
        super.onResume()
        binding.recycler.adapter?.notifyDataSetChanged()



    }

    private fun friendLoad(){
        binding.recycler.adapter = RecyclerAdapterFriends(requireContext(),friends)
        // 데이터베이스에서 내정보 불러오기
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val userRef = firebaseFirestore.collection("users")
        val pref = requireContext().getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()


        userRef.document(userId).collection("friends").get().addOnSuccessListener { result ->
            friends.clear()
            for (document in result){

                // 데이터가 바뀐 친구 갱신
                var code = document["id"]as String
                userRef.document(code).get().addOnSuccessListener {
                    userRef.document(userId).collection("friends").document(it.get("userId") as String).set(it)
                }



                var datas: MutableMap<String, String> = document["data"] as MutableMap<String, String> // 해시맵


                val item = FriendsItem(datas["userName"]as String,datas["userImgUrl"]as String,datas["userId"]as String)
                friends.add(item)

            }
            binding.recycler.adapter?.notifyDataSetChanged()
        }
    }



    private fun clickBtnChange(){

        binding.btn.visibility=View.INVISIBLE
        binding.btnSave.visibility=View.VISIBLE

        binding.etAddr2.visibility= View.VISIBLE
        binding.tvAddr2.visibility=View.INVISIBLE
        binding.etAddr2.setText(userAddr)



    }

    private fun clickBtnSave(){
        binding.btn.visibility=View.VISIBLE
        binding.btnSave.visibility=View.INVISIBLE

        binding.etAddr2.visibility= View.INVISIBLE
        binding.tvAddr2.visibility=View.VISIBLE
        binding.tvAddr2.text = binding.etAddr2.text
        userAddr = binding.etAddr2.text.toString()

    }

    private fun hideKeyBoard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)

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
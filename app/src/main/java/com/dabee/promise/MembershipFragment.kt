package com.dabee.promise

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
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
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.dabee.promise.databinding.FragmentMembershipBinding
import com.google.firebase.firestore.*
import java.io.IOException
import java.util.*


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
    val firebaseFirestore = FirebaseFirestore.getInstance()
    val userRef = firebaseFirestore.collection("users")

    val binding by lazy { FragmentMembershipBinding.inflate(layoutInflater) }
    lateinit var userAddr:String
    lateinit var lat:String
    lateinit var lon:String
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

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val geocoder = Geocoder(binding.root.context,Locale.KOREA)

        //SharedPreference??? ???????????? ?????? userId ????????????
        val pref = requireContext().getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()
        val userImg:String= pref.getString("userImgUrl", null).toString()
        val userName:String= pref.getString("userName", null).toString()
        userRef.document(userId).get().addOnSuccessListener {
            userAddr = it.get("userAddress").toString()
            if(userAddr.equals("null")){
                var addr: MutableMap<String, String> = HashMap()
                addr["userAddress"] = ""
                userAddr = userAddr.replace("null","")
                userRef.document(userId).update(addr as MutableMap<String, Any>)
            }
            binding.tvAddr2.text=userAddr
        }


        binding.tvNickname2.text = userName
        binding.tvId2.text = userId
        Glide.with(this).load(userImg).error(R.drawable.images).into(binding.civProfile)




        binding.btn.setOnClickListener { clickBtnChange() }
        binding.btnSave.setOnClickListener {
            clickBtnSave()
            var userAddrSet: MutableMap<String, String> = java.util.HashMap()


            view.clearFocus()
            hideKeyBoard()

            var addressList: List<Address?>? = null
            try {
                // editText??? ????????? ?????????(??????, ??????, ?????? ???)??? ?????? ????????? ????????? ??????
                addressList = geocoder.getFromLocationName(userAddr,1) // ?????? ?????? ?????? ??????

                lat = addressList[0]?.latitude.toString()
                lon = addressList[0]?.longitude.toString()
                userAddrSet["lat"]=lat
                userAddrSet["lon"]=lon
                userAddrSet["userAddress"]= userAddr
                userRef.document(userId).update(userAddrSet as Map<String, Any>)


            } catch (e: IOException) {
                e.printStackTrace()
            }
        }









        binding.civProfile.setOnClickListener {
            val clipboardManager: ClipboardManager? = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = ClipData.newPlainText("CODE",binding.tvId2.getText().toString().trim()
            ) //??????????????? ID?????? ???????????? id ?????? ???????????? ??????

            clipboardManager?.setPrimaryClip(clipData)

            //????????? ???????????? ?????????????????? ??????

            //????????? ???????????? ?????????????????? ??????
            Toast.makeText(binding.root.context, "ID ??? ?????????????????????.",Toast.LENGTH_SHORT).show()
        }







        // ???????????? ?????? ???????????????
        binding.ivAdd.setOnClickListener {



            val builder = AlertDialog.Builder(binding.root.context)
            val tvCode = TextView(binding.root.context)
            tvCode.text = ""
            tvCode.textSize = 4F
            val etCode = EditText(binding.root.context)
            etCode.hint = " ?????? ????????? ????????? ?????????"
            etCode.isSingleLine = true
            tvCode.marginTop
            val mLayout = LinearLayout(binding.root.context)
            mLayout.orientation = LinearLayout.VERTICAL
            mLayout.setPadding(80)
            mLayout.addView(tvCode)
            mLayout.addView(etCode)
            builder.setView(mLayout)
            builder.setTitle("?????? ??????")


            builder.setPositiveButton("??????") { dialog, which ->

                var code:String = etCode.text.toString()
                if (code == userId) {
                    Toast.makeText(context, "????????? ????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (code.length < 1) code= " "

                userRef.document(code).get().addOnSuccessListener {

                    var id:String? = it.get("userId") as String?
                    if(id == code ){
                        val frName = it.get("userName")as String?
                        userRef.document(userId).collection("friends").document(code).get().addOnSuccessListener { it2 ->
                            if (code == it2.get("id").toString()) {
                                Toast.makeText(context, "?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show()
                            }else Toast.makeText(context, "$frName ?????? ?????? ?????? ???????????????", Toast.LENGTH_SHORT).show()

                            // DB ?????? ??????
                            userRef.document(userId).collection("friends").document(it.get("userId") as String).set(it)
                            userRef.document(userId).get().addOnSuccessListener {
                                userRef.document(code).collection("friends").document(it.get("userId") as String).set(it)
                            }
                            friendLoad()

                        }




                    }else Toast.makeText(context, "????????? ?????????????????????", Toast.LENGTH_SHORT).show()

                }

            }
            builder.setNegativeButton("??????") { dialog, which ->

            }
            builder.show()
        }


    }




    override fun onResume() {
        super.onResume()

        friendLoad()



    }

    private fun friendLoad(){
        binding.recycler.adapter = RecyclerAdapterFriends(requireContext(),friends)
        // ???????????????????????? ????????? ????????????
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val userRef = firebaseFirestore.collection("users")
        val pref = requireContext().getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        val userId:String= pref.getString("userId", null).toString()


        userRef.document(userId).collection("friends").get().addOnSuccessListener { result ->
            friends.clear()
            for (document in result){

                // ???????????? ?????? ?????? ??????
                var code = document["id"]as String
                userRef.document(code).get().addOnSuccessListener {
                    userRef.document(userId).collection("friends").document(it.get("userId") as String).set(it)
                }



                var datas: MutableMap<String, String> = document["data"] as MutableMap<String, String> // ?????????


                val item = FriendsItem(datas["userName"]as String,datas["userImgUrl"]as String,datas["userId"]as String)
                friends.add(item)

            }
            binding.recycler.adapter?.notifyDataSetChanged()
        }
    }



    private fun hideKeyBoard() {
        val imm:InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE )as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etAddr2.windowToken,0)
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




}
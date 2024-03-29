package com.example.favour

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_process_request.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

@Suppress("DEPRECATION")
class ProcessRequestFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    companion object {
        lateinit var requestDTO: RequestDTO
        lateinit var s: String
        lateinit var requestID: String
        lateinit var ref: DatabaseReference
        private const val REQUEST_CODE = 42
        lateinit var listener: ValueEventListener
        var amount: Int = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        s = arguments?.getString("RequestObject")!!
        requestDTO = Gson().fromJson(s, RequestDTO::class.java)
        requestID = requestDTO.requestID
        ref =
            Session(requireContext()).databaseRoot()
                .child(Session(requireContext()).CURRENT_PROCESSING_REQUEST)
        return inflater.inflate(R.layout.fragment_process_request, container, false)
    }

    override fun onViewCreated(
        @NonNull view: View,
        @Nullable savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = Bundle()
        bundle.putString("Items", requestDTO.items)
        bundle.putInt("PhotoOrText", requestDTO.photoOrtext)
        bundle.putString("requestId", requestID)
        call.setOnClickListener(View.OnClickListener {
            if (askForPermissions()) {
                startActivity(
                    Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:" + requestID.take(10))
                    )
                )
            }
        })

        val frag = FragmentItemList()
        frag.arguments = bundle

        childFragmentManager.beginTransaction()
            .add(R.id.itemContainerProcess, frag).commit()

        if (requestDTO.shop_bor == 1) {
            itemDelivered.text = "Items Lended"
            process4(false)
        } else getRequestProcessDTO()
    }

    private fun getRequestProcessDTO() {
        var requestProcessDTO = RequestProcessDTO()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded) {
                    for (i in snapshot.children) {

                        requestProcessDTO = i.getValue(RequestProcessDTO::class.java)!!
                        if (requestProcessDTO.requestID == requestID) break
                    }
                    when {
                        requestProcessDTO.amount == 0 -> process1()
                        requestProcessDTO.purchased -> process4(false)
//                    requestProcessDTO.amountApproved -> process3(false)
                        else -> process2()
                    }
                }
            }
        })
//        return requestProcessDTO
    }

    private fun isPermissionsAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun askForPermissions(): Boolean {
        if (!isPermissionsAllowed()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    android.Manifest.permission.CALL_PHONE
                )
            ) {
                Permssions(requireContext()).showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.CALL_PHONE),
                    Companion.REQUEST_CODE
                )
            }
            return false
        }
        return true
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Companion.REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // +
                    // permission is granted, you can perform your operation here
                } else {
                    // permission is denied, you can ask for permission again, if you want
                    //  askForPermissions()
                }
                return
            }
        }
    }

    private fun removeAllLayouts() {
        process1Layout.visibility = View.GONE
        process2Layout.visibility = View.GONE
        process3Layout.visibility = View.GONE
        process4Layout.visibility = View.GONE
    }

    private fun process1() {
        removeAllLayouts()
        process1Layout.visibility = View.VISIBLE
        editAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.isNotEmpty() && p0.toString().toInt() > 0) {
                    SendButton.setBackgroundColor(resources.getColor(R.color.Green))
                } else {
                    p0.clear()
                    SendButton.setBackgroundColor(resources.getColor(R.color.buttonUnselected))
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


        })

        SendButton.setOnClickListener(View.OnClickListener {

            if (editAmount.text.isNotEmpty()) {
                AlertDialog.Builder(requireContext()).setTitle("Amount Confirmation!")
                    .setMessage("Are you sure, you want to quote INR ${editAmount.text} as final amount?")
                    .setPositiveButton(
                        "Continue",
                        DialogInterface.OnClickListener { dialogInterface, _ ->


                            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {

                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (i in snapshot.children) {
                                        val r = i.getValue(RequestProcessDTO::class.java)
                                        if (r!!.requestID == requestID) {
                                            val m = HashMap<String, Int>()
                                            m["amount"] = editAmount.text.toString().toInt()
                                            i.ref.updateChildren(m as Map<String, Any>)
                                            Permssions(requireContext()).sendNotifications(
                                                requestDTO.userUid,
                                                R.mipmap.app_icon,
                                                "Confirm the quoted amount to get your favour done!",
                                                "Amount Approval"
                                            )
                                            process2()
                                            break
                                        }

                                    }
                                }

                            })


                            dialogInterface.dismiss()
                        })
                    .setNegativeButton("Change", null).show()
            }
        })
    }


    private fun process2() {
        removeAllLayouts()
        process2Layout.visibility = View.VISIBLE
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {

                    val requestProcessDTO = i.getValue(RequestProcessDTO::class.java)
                    if (requestProcessDTO!!.requestID == requestID) {
                        amount = requestProcessDTO.amount
                        amountDisplay.text = amount.toString()
                        break
                    }

                }
            }
        })

        listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    val requestProcessDTO = i.getValue(RequestProcessDTO::class.java)
                    if ((requestProcessDTO!!.requestID == requestID) && (requestProcessDTO.amountApproved)) {
//                        process3(true)
                        process4(true)
                        break
                    }

                }
            }
        })

    }

//    private fun process3(b: Boolean) {
//        if (b) ref.removeEventListener(listener)
//        removeAllLayouts()
//        process3Layout.visibility = View.VISIBLE
//        ref.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(error: DatabaseError) {
//            }
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (i in snapshot.children) {
//
//                    val requestProcessDTO = i.getValue(RequestProcessDTO::class.java)
//                    if (requestProcessDTO!!.requestID == requestID) {
//                        amount = requestProcessDTO.amount
//                        amountDisplay.text = amount.toString()
//                        break
//                    }
//
//                }
//            }
//        })
//        purchased.setOnClickListener(View.OnClickListener {
//
//            ref.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onCancelled(error: DatabaseError) {
//                }
//
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    for (i in snapshot.children) {
//                        val requestProcessDTO = i.getValue(RequestProcessDTO::class.java)
//                        if (requestProcessDTO!!.requestID == requestID && requestProcessDTO.amountApproved) {
//                            val m = HashMap<String, Boolean>()
//                            m["purchased"] = true
//                            i.ref.updateChildren(m as Map<String, Any>)
//                            break
//                        }
//
//                    }
//                }
//            })
//
//            process4()
//        })
//
//    }

    private fun process4(b: Boolean) {
        if (b) ref.removeEventListener(listener)
        removeAllLayouts()
        process4Layout.visibility = View.VISIBLE

        Session(requireContext()).databaseRoot().child("Users").child(requestDTO.userUid)
            .child("address").addListenerForSingleValueEvent((object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userAddress.text = snapshot.getValue(String::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }))


        listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val r = snap.getValue(RequestProcessDTO::class.java)
                    if (isAdded) {
                        if (r!!.requestID == requestDTO.requestID && r.completed) {
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.containerProcess, FragmentFavourCompleted())
                                .commit()

                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        itemDelivered.setOnClickListener(View.OnClickListener {
            Permssions(requireContext()).sendNotifications(
                requestDTO.userUid,
                R.mipmap.app_icon,
                "Items delivered by the user. Please confirm to complete the favour.",
                "Delivery Confirmation"
            )
            Toast.makeText(
                requireContext(),
                "Notification sent for delivery approval. You can also call to remind.",
                Toast.LENGTH_LONG
            ).show()
        })
    }


}
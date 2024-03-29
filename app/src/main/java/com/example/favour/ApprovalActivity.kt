package com.example.favour

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.approval_activity.*
import kotlinx.android.synthetic.main.approval_activity.call
import kotlinx.android.synthetic.main.approval_activity.itemDelivered
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ApprovalActivity : NavigationDrawer() {

    private val REQUEST_CODE = 42
    var mobile: String? = null
    lateinit var ref: DatabaseReference
    lateinit var requestDTO: RequestDTO
    lateinit var requestProcessDTO: RequestProcessDTO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.approval_activity)

        val s: String = intent.extras!!.get("Request_Object").toString()
        requestDTO = Gson().fromJson(s, RequestDTO::class.java)

        BackButtonToHome.setOnClickListener(View.OnClickListener {
            onBackPressed()
            finish()
        })

        requestType.text = requestDTO.categories
        if (!requestDTO.urgent) requestUrgent.visibility = View.GONE

//        fillDetails()


        call.setOnClickListener(View.OnClickListener {
            if (askForPermissions() && mobile != null) {
                startActivity(
                    Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:$mobile")
                    )
                )
            }
        })




        ref = Session(this).databaseRoot()

        if (requestDTO.shop_bor == 0) shoppingApproval()
        else borrowingApproval()

        itemDelivered.setOnClickListener(View.OnClickListener {
            completeFavour()
            approvalRoot.removeAllViews()
            supportFragmentManager.beginTransaction()
                .replace(R.id.approvalRoot, FragmentFavourCompleted())
                .commit()

        })
    }

    private fun fillDetails() {

    }

    private fun borrowingApproval() {
        waitingLayout.visibility = View.GONE
        approveAmount.visibility = View.GONE
        itemDelivered.visibility = View.VISIBLE

        favourer.text = "Lender"
        ref.child(Session(this).CURRENT_PROCESSING_REQUEST)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {
                        val j = snap.getValue(RequestProcessDTO::class.java)
                        if (j!!.requestID == requestDTO.requestID) {
                            getFavourerName(j.favourerUID)
                            getPhone(j.favourerUID)
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })


    }

    private fun shoppingApproval() {
        ref.child(Session(this).CURRENT_PROCESSING_REQUEST)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        requestProcessDTO = i.getValue(RequestProcessDTO::class.java)!!
                        if (requestProcessDTO.requestID == requestDTO.requestID) {
                            getPhone(requestProcessDTO.favourerUID)
                            getFavourerName(requestProcessDTO.favourerUID)
                        }
                        if (requestProcessDTO.requestID == requestDTO.requestID) {
                            if (requestProcessDTO.amount != 0 && !requestProcessDTO.amountApproved) {
                                amountLayout.visibility = View.VISIBLE
                                quotedAmount.text = requestProcessDTO.amount.toString()
                                approveAmount.visibility = View.VISIBLE
                                approveAmount.setBackgroundColor(resources.getColor(R.color.black))
                                waitingLayout.visibility = View.GONE
                            } else if (requestProcessDTO.amountApproved) {
                                itemDelivered.visibility = View.VISIBLE
                            } else {
                                waitingLayout.visibility = View.VISIBLE
                                approveAmount.visibility = View.VISIBLE
                            }
                            break
                        }

                    }
                }
            })




        approveAmount.setOnClickListener(View.OnClickListener {
            if (amountLayout.visibility == View.VISIBLE) {

                ref.child(Session(this).CURRENT_PROCESSING_REQUEST)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (i in snapshot.children) {
                                val r = i.getValue(RequestProcessDTO::class.java)
                                if (r!!.requestID == requestDTO.requestID) {
                                    val m = HashMap<String, Boolean>()
                                    m["amountApproved"] = true
                                    approveAmount.visibility = View.GONE
                                    itemDelivered.visibility = View.VISIBLE
                                    i.ref.updateChildren(m as Map<String, Any>)
                                    Permssions(this@ApprovalActivity).sendNotifications(
                                        r.favourerUID,
                                        R.mipmap.app_icon,
                                        "Deliver the items at the given address.",
                                        "Amount Confirmed"
                                    )
                                    break
                                }

                            }
                        }

                    })

            }

        })
    }

    private fun completeFavour() {

        ref.child(Session(this).CURRENT_PROCESSING_REQUEST)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        val j = i.getValue(RequestProcessDTO::class.java)
                        if (j!!.requestID == requestDTO.requestID) {
                            val m = HashMap<String, Boolean>()
                            val mp = HashMap<String, Int>()
                            val md = HashMap<String, String>()
                            m["delivered"] = true
                            m["completed"] = true
                            var points = 0
                            if (requestDTO.shop_bor == 0) points = 100
                            else points = 50
                            mp["points"] = points
                            md["date"] = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
                            md["time"] = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                            i.ref.updateChildren(m as Map<String, Any>)
                            i.ref.updateChildren(mp as Map<String, Any>)
                            i.ref.updateChildren(md as Map<String, Any>)
                            Permssions(this@ApprovalActivity).sendNotifications(
                                j.favourerUID,
                                R.mipmap.app_icon,
                                "Congratulations! You have earned $points points.",
                                "Favour Completed"
                            )
                            break
                        }

                    }
                }
            })
        ref.child(Session(this).REQUESTS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {
                        val r = snap.getValue(RequestDTO::class.java)
                        if (r!!.requestID == requestDTO.requestID) {
                            val m = HashMap<String, Boolean>()
                            m["isCompleted"] = true
                            snap.ref.updateChildren(m as Map<String, Any>)
                            break

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun getFavourerName(favourerUID: String?) {
        ref.child(Session(this).USERS).child(favourerUID!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userDTO = snapshot.getValue(UserDTO::class.java)
                    favourerName.text = userDTO!!.username
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

    }

    private fun getPhone(favourerUID: String?) {
        ref.child(Session(this).USERS).child(favourerUID!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val userDTO = snapshot.getValue(UserDTO::class.java)
                    mobile = userDTO!!.mobile
                }
            })


    }

    private fun isPermissionsAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun askForPermissions(): Boolean {
        if (!isPermissionsAllowed()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.CALL_PHONE
                )
            ) {
                Permssions(this).showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CALL_PHONE),
                    REQUEST_CODE
                )
            }
            return false
        }
        return true
    }
}
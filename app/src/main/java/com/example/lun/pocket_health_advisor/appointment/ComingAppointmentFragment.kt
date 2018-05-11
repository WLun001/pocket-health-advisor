package com.example.lun.pocket_health_advisor.appointment

import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.View
import android.widget.ListView
import com.example.lun.pocket_health_advisor.adapter.AppointmentAdapter
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.Appointment
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.AuthUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.yesButton

/**
 * Created by wlun on 2/14/18.
 */
class ComingAppointmentFragment : ListFragment() {

    private lateinit var authUser: AuthUser

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //get firebase user
        val auth = FirebaseAuth.getInstance().currentUser
        auth?.displayName?.let { authUser = AuthUser(auth.uid, auth.displayName.toString()) }
        searchPatient()
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        val paymentStatus = (listAdapter.getItem(position) as Appointment).paymentStatus
        if (paymentStatus) alert { message = "Paid" ; yesButton { }}.show() else alert { message="please make payment"; yesButton {  } }.show()
    }

    private fun searchPatient() {
        var firestore = FirebaseFirestore.getInstance()
                .collection("patients")
                .whereEqualTo("name", authUser.name)
                .get()
                .addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        result?.let {
                            if (result.size() > 0) {
                                searchAppointment(result.documents[0].id)
                            } else toast("no matches found")
                        }

                    } else toast("No matches found")

                })
                .addOnFailureListener { toast("No matches found") }
    }

    private fun searchAppointment(id: String) {
        val appointmentList = ArrayList<Appointment>()
        var firestore = FirebaseFirestore.getInstance()
                .collection("appointments")
                .whereEqualTo("patient_id", id)
                .get()
                .addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        result?.let {
                            if (result.size() > 0) {
                                result.forEach {
                                    appointmentList.add(
                                            Appointment(
                                                    it.getString("doctor_name"),
                                                    it.getString("doctor_id"),
                                                    it.getString("hospital_id"),
                                                    it.getString("hospital_name"),
                                                    it.getString("patient_id"),
                                                    it.get("time").toString(),
                                                    it.getBoolean("payment_status")
                                            )
                                    )
                                }
                            } else toast("No appointments found")
                        }
                    }
                    listAdapter = AppointmentAdapter(context, appointmentList)
                })
                .addOnFailureListener { toast("No appointments found!") }
    }
}
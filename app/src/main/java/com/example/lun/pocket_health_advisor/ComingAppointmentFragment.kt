package com.example.lun.pocket_health_advisor

import android.os.Bundle
import android.support.v4.app.ListFragment
import com.example.lun.pocket_health_advisor.DataClassWrapper.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.support.v4.toast

/**
 * Created by wlun on 2/14/18.
 */
class ComingAppointmentFragment : ListFragment() {

    private lateinit var authUser: AuthUser
    private lateinit var hospitalUser: HospitalUser

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //get firebase user
        val auth = FirebaseAuth.getInstance().currentUser
        auth?.displayName?.let { authUser = AuthUser(auth.uid, auth.displayName.toString()) }
        searchPatient()
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
                                result.forEach {
                                    searchAppointment(it.getString("id"))
                                }
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
                                                    it.get("time").toString()
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
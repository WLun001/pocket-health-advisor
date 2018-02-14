package com.example.lun.pocket_health_advisor

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.ListFragment
import android.support.v7.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast

/**
 * Created by wlun on 2/14/18.
 */
class ComingAppointmentFragment : ListFragment() {

    private lateinit var authUser: DataClassWrapper.AuthUser
    private lateinit var hospitalUser: DataClassWrapper.HospitalUser

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //get firebase user
        val auth = FirebaseAuth.getInstance().currentUser
        auth?.displayName?.let { authUser = DataClassWrapper.AuthUser(auth.uid, auth.displayName as String) }

        context.toast("hello from current appointment fragment")
    }

    private fun getHospitalUser() {
        var firestore = FirebaseFirestore.getInstance()
                .collection("patients")
                .whereEqualTo("name", authUser.name)
                .get()
                .addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        result?.let {
                            if (result.size() > 0) {
                                for (i in result) {
                                    hospitalUser = DataClassWrapper.HospitalUser(
                                            i.getString("ic"),
                                            i.getString("id"),
                                            i.getString("name"),
                                            i.getString("hospital_id"),
                                            i.getString("age")
                                    )
                                }
                                getHospitalDetails(hospitalUser)
                                //toast(name.name)
                            } else toast("no matches found")
                        }

                    } else toast("No matches found")

                })
                .addOnFailureListener { toast("No matches found") }
    }

    private fun getHospitalDetails(hospitalUser: DataClassWrapper.HospitalUser) {
        val hospitalDetails = DataClassWrapper.AppointmentHospitalDetails()
        var firestore = FirebaseFirestore.getInstance()
                .collection("hospitals")
                .whereEqualTo("id", hospitalUser.hospitalId)
                .get()
                .addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        result?.let {
                            if (result.size() > 0) {
                                for (i in result) {
                                    hospitalDetails.name = i.getString("name")
                                }
                                val message = """
                                    Name : ${hospitalUser.name}
                                    Hospital : ${hospitalDetails.name}
                                    """
                                AlertDialog.Builder(context)
                                        .setTitle("Appointment")
                                        .setMessage(message)
                                        .create()
                                        .show()
                            } else toast("No hospital found")
                        }
                    }
                })
                .addOnFailureListener { toast("No hospital found!") }
    }
}
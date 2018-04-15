package com.example.lun.pocket_health_advisor.NearbyHospital

import android.os.Bundle
import android.support.v4.app.ListFragment
import com.example.lun.pocket_health_advisor.adapter.RegisteredHospitalAdapter
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.RegisteredHospital
import com.google.firebase.firestore.FirebaseFirestore


/**
 * Created by wlun on 4/15/18.
 */
class RegisteredHospitalFragment : ListFragment(){

    private val hospitals = ArrayList<RegisteredHospital>()
    private lateinit var firestore: FirebaseFirestore

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        getRegisteredHospitals()
    }

    private fun getRegisteredHospitals() {
        firestore.collection("hospitals").get().addOnCompleteListener { task ->
            if (task.isSuccessful) { val result = task.result
                result?.let {if (result.size() > 0) { result.forEach {
                            hospitals.add(
                                    RegisteredHospital(
                                            it.getString("id"),
                                            it.getString("name"),
                                            it.getString("email"),
                                            it.getString("contact_number"),
                                            it.getString("address"),
                                            it.getString("consultation_fee")
                                    ))}}}}
            listAdapter = RegisteredHospitalAdapter(context, hospitals)
        }
    }


}
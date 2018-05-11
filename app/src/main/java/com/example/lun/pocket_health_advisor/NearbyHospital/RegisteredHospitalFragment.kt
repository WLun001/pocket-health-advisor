package com.example.lun.pocket_health_advisor.NearbyHospital

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.View
import android.widget.ListView
import com.example.lun.pocket_health_advisor.R
import com.example.lun.pocket_health_advisor.adapter.RegisteredHospitalAdapter
import com.example.lun.pocket_health_advisor.appointment.RequestAppointmentActivity
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.RegisteredHospital
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.noButton
import org.jetbrains.anko.support.v4.alert


/**
 * Created by wlun on 4/15/18.
 */
class RegisteredHospitalFragment : ListFragment() {

    private val hospitals = ArrayList<RegisteredHospital>()
    private lateinit var firestore: FirebaseFirestore

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        getRegisteredHospitals()
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        alert("""
                                    Name : ${hospitals[position].name}
                                    Phone : ${hospitals[position].contactNo}
                                    Address : ${hospitals[position].address}
                                    Consultation Fee : ${hospitals[position].consultationFee}
                                    """) {
            positiveButton(R.string.make_appointment) { dialog ->
                startActivity(Intent(
                        activity, RequestAppointmentActivity::class.java)
                        .putExtra(getString(R.string.intent_hospital), hospitals[position]))
                dialog.dismiss()
            }
            noButton { }
        }.show()
    }

    private fun getRegisteredHospitals() {
        firestore.collection("hospitals").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                result?.let {
                    if (result.size() > 0) {
                        result.forEach {
                            hospitals.add(
                                    RegisteredHospital(
                                            it.getString("id"),
                                            it.getString("name"),
                                            it.getString("email"),
                                            it.getString("contact_number"),
                                            it.getString("address"),
                                            it.getString("consultation_fee")
                                    ))
                        }
                    }
                }
            }
            listAdapter = RegisteredHospitalAdapter(context, hospitals)
        }
    }

//    private fun showMakeAppointmentDialog(position: Int) {
//        var currentDoctor: String
//        val dialog: ProgressDialog = progressDialog(message = "Please wait a bit…", title = "Fetching data")
//        dialog.isIndeterminate = true
//        dialog.show()
//        val doctors = ArrayList<String>()
//        doAsync {
//            firestore.collection("doctors").whereEqualTo("hospital_id", hospitals[position].id)
//                    .get().addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            val result = task.result
//                            result?.let {
//                                if (result.size() > 0) {
//                                    result.forEach { doctors.add(it.getString("name")) }
//                                } else toast("no doctor found")
//                            }
//                        }
//                    }
//            onComplete {
//                dialog.dismiss()
//                uiThread {
//                    alert {
//                        title = getString(R.string.request_appointment)
//                        var doctorSelector: EditText?
//                        var datePickerEditText: EditText?
//                        customView {
//                            scrollView {
//                                verticalLayout {
//                                    padding = dip(30)
//                                    textView {
//                                        text = hospitals[position].name
//                                        textSize = 24f
//                                    }
//                                    doctorSelector = editText {
//                                        hint = context.getString(R.string.select_doctor)
//                                        isFocusable = false
//                                        isClickable = true
//                                        textSize = 24f
//                                    }
//                                    doctorSelector!!.setOnClickListener {
//                                        selector("Pick a doctor", doctors, { _, i ->
//                                            doctorSelector!!.setText(doctors[i])
//                                            currentDoctor = doctors[i]
//                                        })
//                                    }
//
//                                   datePickerEditText =  editText {
//                                        hint = context.getString(R.string.pick_date)
//                                        maxLines = 3
//                                        textSize = 24f
//                                    }
//                                    datePickerEditText!!.setOnClickListener {  }
//                                    editText {
//                                        hint = context.getString(R.string.appointment_notes)
//                                        maxLines = 3
//                                        textSize = 24f
//                                    }
//                                    button("Submit") {
//                                        textSize = 26f
//                                    }
//                                }
//                            }
//                        }
//                        yesButton { }
//                    }.show()
//                }
//            }
//        }
//    }


}
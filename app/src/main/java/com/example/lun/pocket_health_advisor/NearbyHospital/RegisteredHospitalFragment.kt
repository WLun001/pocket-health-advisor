package com.example.lun.pocket_health_advisor.NearbyHospital

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import com.example.lun.pocket_health_advisor.R
import com.example.lun.pocket_health_advisor.adapter.RegisteredHospitalAdapter
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.RegisteredHospital
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.progressDialog
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.toast


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

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        alert("""
                                Name : ${hospitals[position].name}
                                Phone : ${hospitals[position].contactNo}
                                Address : ${hospitals[position].address}
                                Consultation Fee : ${hospitals[position].consultationFee}
                                """){
            positiveButton(R.string.make_appointment){ dialog ->
                showMakeAppointmentDialog(position)
                dialog.dismiss()
            }
            noButton {  }
        }.show()
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

    private fun showMakeAppointmentDialog(position: Int){
        var currentDoctor:String
        val dialog: ProgressDialog = progressDialog(message = "Please wait a bit…", title = "Fetching data")
        dialog.isIndeterminate = true
        dialog.show()
        val doctors = ArrayList<String>()
        doAsync {
            firestore.collection("doctors").whereEqualTo("hospital_id", hospitals[position].id)
                    .get().addOnCompleteListener {task -> if (task.isSuccessful) {
                        val result = task.result
                        result?.let {
                            if (result.size() > 0) {
                                result.forEach { doctors.add(it.getString("name")) }
                            } else toast("no doctor found")
                            }}}
            onComplete {dialog.dismiss()
                uiThread {
                    alert {
                        title = "Make Appointment"
                        var doctorSelector: EditText?
                        customView {
                            verticalLayout {
                                padding = dip(30)
                                textView {
                                    text = hospitals[position].name
                                    textSize = 24f
                                }
                                doctorSelector = editText {
                                    hint = "Doctor"
                                    isFocusable = false
                                    isClickable = true
                                    textSize = 24f
                                }
                                doctorSelector!!.setOnClickListener{
                                    selector("Pick a doctor", doctors, { dialogInterface, i ->
                                        doctorSelector!!.setText(doctors[i])
                                        currentDoctor = doctors[i]
                                    })
                                }
                                editText {
                                    hint = "Notes"
                                    maxLines = 3
                                    textSize = 24f
                                }
                                button("Submit") {
                                    textSize = 26f
                                }}}
                        yesButton {  }
                    }.show()
                }}
        }
    }


}
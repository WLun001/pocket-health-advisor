package com.example.lun.pocket_health_advisor.appointment

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import com.example.lun.pocket_health_advisor.R
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.RegisteredHospital
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_request_appointment.*
import kotlinx.android.synthetic.main.content_request_appointment.*
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class RequestAppointmentActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var hospital: RegisteredHospital
    private lateinit var calendar: Calendar
    private var authUid: String? = ""
    private var displayName: String? = ""
    private val doctors = ArrayList<String>()
    private val reports = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_appointment)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Request Appointment"

        firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        authUid = auth.currentUser?.uid
        displayName = auth.currentUser?.displayName
        calendar = Calendar.getInstance()

        hospital = intent.getSerializableExtra(getString(R.string.intent_hospital)) as RegisteredHospital
        readData()
        setupListener()

    }

    private fun setupListener() {
        select_report.setOnClickListener { setupSelector(getString(R.string.select_medical_report), select_report, reports) }
        select_doctor.setOnClickListener { setupSelector(getString(R.string.select_doctor), select_doctor, doctors) }
        app_date.setOnClickListener { setupDatePicker() }
        app_time.setOnClickListener { setupTimePicker() }
        fab.setOnClickListener {
            if (checkUserInputNotEmpty()) sendWaitingList(getUserInput()) else
                alert {
                    title = "Error"
                    message = "Please fill all required fields!"
                    yesButton { }
                }.show()
        }
    }

    private fun setupSelector(title: String, editText: EditText, list: ArrayList<String>) {
        selector(title, list, { _, i -> editText.setText(list[i]) })
    }

    private fun setupDatePicker() {
        alert {
            isCancelable = false
            lateinit var datePicker: DatePicker
            customView {
                verticalLayout {
                    datePicker = datePicker {
                        minDate = System.currentTimeMillis()
                    }
                }
            }
            yesButton {
                calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                val format = SimpleDateFormat("dd-MM-yyyy").format(calendar.time)
                app_date.setText(format)
            }
            noButton { }
        }.show()
    }

    private fun setupTimePicker() {
        alert {
            isCancelable = false
            lateinit var timePicker: TimePicker
            customView {
                verticalLayout {
                    timePicker = timePicker { }
                }
            }
            yesButton {
                calendar.set(Calendar.HOUR, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)
                val format = SimpleDateFormat("hh:mm a").format(calendar.time)
                app_time.setText(format)
            }
            noButton { }
        }.show()
    }

    private fun readData() {
        val dialog = progressDialog(message = "Please wait a bit…", title = "Fetching data")
        dialog.isIndeterminate = true
        dialog.show()
        doAsync { getReport(); getDoctors(); onComplete { uiThread { dialog.dismiss() } } }
    }

    private fun getReport() {
        authUid?.let {
            reports.clear()
            firestore.collection(getString(R.string.first_col))
                    .document(authUid.toString())
                    .collection("medic_report")
                    .orderBy("timestamp")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (i: DocumentSnapshot in task.result) reports.add(readDiagnoseCondition(i))
                        } else toast(task.exception.toString())
                    }
        }
    }

    private fun readDiagnoseCondition(doc: DocumentSnapshot): String {
        val diagnoseCondition = doc["diagnose_condition"] as HashMap<*, *>
        return diagnoseCondition["name"].toString()
    }

    private fun getDoctors() {
        firestore.collection("doctors").whereEqualTo("hospital_id", hospital.id)
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        result?.let {
                            if (result.size() > 0) {
                                result.forEach { doctors.add(it.getString("name")) }
                            } else toast("no doctor found")
                        }
                    }
                }
    }

    private fun checkUserInputNotEmpty(): Boolean {
        return !select_report.text.isEmpty() && !app_date.text.isEmpty() && !app_time.text.isEmpty()
    }

    private fun sendWaitingList(data: HashMap<String, Any>) {
        val progress = progressDialog(getString(R.string.requesting), getString(R.string.please_wait))
        progress.isIndeterminate = true
        progress.show()
        firestore.collection("waiting_list").add(data).addOnCompleteListener {
            toast("request sent")
            progress.dismiss()
            finish()
        }.addOnSuccessListener { documentReference ->
            firestore.collection("waiting_list").document(documentReference.id).update("id", documentReference.id)
        }
                .addOnFailureListener { toast("couldn't send request"); progress.dismiss() }
    }

    private fun getUserInput(): HashMap<String, Any> {
        val data = HashMap<String, Any>()
        data["id"] = UUID.randomUUID().toString()
        data["appointment_desc"] = select_report.text.toString()
        data["patient_name"] = displayName.toString()
        data["doctor_name"] = if (!select_doctor.text.isEmpty()) select_doctor.text.toString() else "null"
        data["hospital_id"] = hospital.id
        data["date"] = app_date.text.toString()
        data["time"] = app_time.text.toString()
        data["notes"] = app_notes.text.toString()
        return data
    }

}

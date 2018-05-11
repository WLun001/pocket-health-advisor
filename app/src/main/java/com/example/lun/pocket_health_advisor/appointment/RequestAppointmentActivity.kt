package com.example.lun.pocket_health_advisor.appointment

import android.os.Bundle
import android.support.design.widget.Snackbar
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

class RequestAppointmentActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var hospital: RegisteredHospital
    private lateinit var calendar: Calendar
    private var authUid: String? = ""
    private val doctors = ArrayList<String>()
    private val reports = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_appointment)
        setSupportActionBar(toolbar)

        firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        authUid = auth.currentUser?.uid
        calendar = Calendar.getInstance()

        hospital = intent.getSerializableExtra(getString(R.string.intent_hospital)) as RegisteredHospital
        readData()
        setupListener()

    }

    private fun setupListener() {
        select_report.setOnClickListener { setupSelector(getString(R.string.select_medical_report), select_report, reports) }
        select_doctor.setOnClickListener { setupSelector(getString(R.string.select_doctor), select_doctor, doctors) }
        app_date.setOnClickListener {setupDatePicker() }
        app_time.setOnClickListener { setupTimePicker() }


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()

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
                        minDate = System.currentTimeMillis()}}}
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
                    timePicker = timePicker { }}}
            yesButton {
                calendar.set(Calendar.HOUR, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)
                val format = SimpleDateFormat("hh:mm a").format(calendar.time)
                app_time.setText(format)}
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
}

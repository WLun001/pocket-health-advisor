package com.example.lun.pocket_health_advisor.appointment

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import com.example.lun.pocket_health_advisor.R
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.RegisteredHospital
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_request_appointment.*
import kotlinx.android.synthetic.main.content_request_appointment.*
import org.jetbrains.anko.*

class RequestAppointmentActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var hospital: RegisteredHospital
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

        hospital = intent.getSerializableExtra(getString(R.string.intent_hospital)) as RegisteredHospital
        readData()

        select_report.setOnClickListener { setupSelector(getString(R.string.select_medical_report), select_report, reports) }

        select_doctor.setOnClickListener { setupSelector(getString(R.string.select_doctor), select_doctor, doctors) }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()

        }
    }

    private fun setupSelector(title: String, editText: EditText, list: ArrayList<String>) {
        selector(title, list, { _, i -> editText.setText(list[i]) })
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

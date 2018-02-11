package com.example.lun.pocket_health_advisor


import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.View
import android.widget.ListView
import com.example.lun.pocket_health_advisor.DataClassWrapper.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast


/**
 * Created by wlun on 2/10/18.
 */
class MedicReportHistoryFragment : ListFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val categories = ArrayList<String>()
        categories.add("Neurology")
        val condition = Condition(
                "Concussion",
                "Concussion",
                "acute",
                categories,
                "Please visit a neurologist as soon as possible.",
                "very rare",
                "moderate",
                "emergency")
        val medicReport = ArrayList<MedicReport>()

        medicReport.add(MedicReport(condition, null, null, "2017"))

        val adapter = MedicReportAdapter(context, medicReport)
        listAdapter = adapter

//        var authUser = activity.intent.getSerializableExtra(USER_DETAILS) as AuthUser
//        activity.toast(authUser.name)
        val authUid = FirebaseAuth.getInstance().currentUser?.uid
        getReportFromDb(authUid)

    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        toast("Clicked item " + position.toString())
    }

    private fun getReportFromDb(uid: String?) {
        val doc: ArrayList<DocumentSnapshot> = ArrayList()
        uid?.let {
            val db = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.first_col))
                    .document(uid)
                    .collection("medic_report")
                    .orderBy("timestamp")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (i: DocumentSnapshot in task.result) {
                                readDiagnoseCondition(i)
                                doc.add(i)
                            }
                        } else {
                            activity.toast(task.exception.toString())
                        }
                    }
        }
    }

    private fun readReportFromJson(i: DocumentSnapshot) {


    }

    private fun readInitialSyndrome(doc: DocumentSnapshot) {
        val initialSyndromeList = ArrayList<InitialSyndrome>()
        val initialSyndrome = (doc.get("initial") as HashMap<*, *>)["initial"] as ArrayList<*>
        for (initial in initialSyndrome){
            initialSyndromeList.add(
                    InitialSyndrome(
                            (initial as HashMap<*, *>)["name"].toString(),
                            initial["choice_id"].toString()
                    )
            )
        }
        for (i in initialSyndromeList) {
            toast(i.name)
        }
    }

    private fun readPossibleConditions(doc: DocumentSnapshot) {
        val possibleConditionsList = ArrayList<PossibleCondition>()
        val possibleConditions = doc.get("possible_conditions") as HashMap<*, *>
        val conditions = possibleConditions["conditions"] as ArrayList<*>
        for (cond in conditions) {
            possibleConditionsList.add(
                    PossibleCondition(
                            (cond as HashMap<*, *>)["name"].toString(),
                            cond["probability"].toString().toDouble()
                    )
            )
        }
    }

    private fun readDiagnoseCondition(doc: DocumentSnapshot) {
        val diagnoseCondition = doc.get("diagnose_condition") as HashMap<*, *>
        val name = diagnoseCondition["name"].toString()
        val commonName = diagnoseCondition["common_name"].toString()
        val acuteness = diagnoseCondition["acuteness"].toString()
        val categories = diagnoseCondition["categories"] as ArrayList<*>
        val hints = (diagnoseCondition["extras"] as HashMap<*, *>)["hint"].toString()
        var prevalence = diagnoseCondition["prevalence"].toString()
        var severity = diagnoseCondition["severity"].toString()
        var triageLevel = diagnoseCondition["triage_level"].toString()

        prevalence = formatString(prevalence)
        severity = formatString(severity)
        triageLevel = formatString(triageLevel)

        val categoriesList = ArrayList<String>()
        for (cat in categories){
            categoriesList.add(cat.toString())
        }


        val condition = Condition(
                name,
                commonName,
                acuteness,
                categoriesList,
                hints,
                prevalence,
                severity,
                triageLevel
        )
    }

    private fun readQuestions(doc: DocumentSnapshot) {

    }

    private fun formatString(s: String) : String{
        return s.replace('_', ' ')
    }


}
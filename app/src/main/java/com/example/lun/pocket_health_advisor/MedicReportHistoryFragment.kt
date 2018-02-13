package com.example.lun.pocket_health_advisor


import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import com.example.lun.pocket_health_advisor.DataClassWrapper.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.sinch.gson.JsonParser
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast


/**
 * Created by wlun on 2/10/18.
 */
class MedicReportHistoryFragment : ListFragment() {
    private var medicReportList = ArrayList<MedicReport>()
    val mapList = ArrayList<HashMap<*, *>>()
    private var displayName: String? = ""

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

//        var authUser = activity.intent.getSerializableExtra(USER_DETAILS) as AuthUser
//        activity.toast(authUser.name)
        val auth = FirebaseAuth.getInstance()
        val authUid = auth.currentUser?.uid
        displayName = auth.currentUser?.displayName
        getReportFromDb(authUid)

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_medic_report_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.send_to_hospital -> {
                val db = FirebaseFirestore.getInstance()
                        .collection("patients")
                        .whereEqualTo("name", displayName)
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val doc = task.result
                                for (i in doc) {
                                    val docId = i.id
                                    val jsonParse = JsonParser()
                                    FirebaseFirestore.getInstance()
                                            .collection("patients")
                                            .document(docId)
                                            .update(
                                                    "diagnosis_history",
                                                    (listAdapter.getItem(0) as MedicReport).generateMap()
                                            )
                                    // TODO: let user select which report to be sent
                                }

                            }
                        }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        toast("Clicked item " + position.toString())
    }

    private fun getReportFromDb(uid: String?) {
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
                                medicReportList.add(
                                        MedicReport(
                                                readDiagnoseCondition(i),
                                                readInitialSyndrome(i),
                                                readPossibleConditions(i),
                                                readQuestions(i),
                                                i.get("timestamp").toString()
                                        )
                                )
                            }
                            setUpListFragment(medicReportList)
                        } else {
                            activity.toast(task.exception.toString())
                        }
                    }
        }
    }

    private fun readInitialSyndrome(doc: DocumentSnapshot): ArrayList<InitialSyndrome> {
        val initialSyndromeList = ArrayList<InitialSyndrome>()
        val initialSyndrome = (doc["initial"] as HashMap<*, *>)["initial"] as ArrayList<*>
        for (initial in initialSyndrome) {
            initialSyndromeList.add(
                    InitialSyndrome(
                            (initial as HashMap<*, *>)["name"].toString(),
                            initial["choice_id"].toString(),
                            initial)
            )
        }
        return initialSyndromeList
    }

    private fun readPossibleConditions(doc: DocumentSnapshot): ArrayList<PossibleCondition> {
        val possibleConditionsList = ArrayList<PossibleCondition>()
        val possibleConditions = doc["possible_conditions"] as HashMap<*, *>
        val conditions = possibleConditions["conditions"] as ArrayList<*>
        for (cond in conditions) {
            possibleConditionsList.add(
                    PossibleCondition(
                            (cond as HashMap<*, *>)["name"].toString(),
                            cond["probability"].toString().toDouble(),
                            cond
                    )
            )
        }
        return possibleConditionsList
    }

    private fun readDiagnoseCondition(doc: DocumentSnapshot): Condition {
        val diagnoseCondition = doc["diagnose_condition"] as HashMap<*, *>
        val name = diagnoseCondition["name"].toString()
        val commonName = diagnoseCondition["common_name"].toString()
        var acuteness = diagnoseCondition["acuteness"].toString()
        val categories = diagnoseCondition["categories"] as ArrayList<*>
        val hints = (diagnoseCondition["extras"] as HashMap<*, *>)["hint"].toString()
        var prevalence = diagnoseCondition["prevalence"].toString()
        var severity = diagnoseCondition["severity"].toString()
        var triageLevel = diagnoseCondition["triage_level"].toString()

        acuteness = formatString(acuteness)
        prevalence = formatString(prevalence)
        severity = formatString(severity)
        triageLevel = formatString(triageLevel)

        val categoriesList = ArrayList<String>()
        for (cat in categories) {
            categoriesList.add(cat.toString())
        }
        return Condition(
                name,
                commonName,
                acuteness,
                categoriesList,
                hints,
                prevalence,
                severity,
                triageLevel,
                diagnoseCondition
        )
    }

    private fun readQuestions(doc: DocumentSnapshot): ArrayList<Question> {
        val questionList = ArrayList<Question>()
        val question = doc["questions"] as ArrayList<*>
        for (i in question) {
            questionList.add(
                    Question(
                            (i as HashMap<*, *>)["question"].toString(),
                            i["symptom"].toString(),
                            i["user_response"].toString(),
                            i
                    )
            )
        }
        return questionList
    }

    private fun formatString(s: String): String {
        return s.replace('_', ' ')
    }

    private fun setUpListFragment(medicReport: ArrayList<MedicReport>) {
        val adapter = MedicReportAdapter(context, medicReport)
        listAdapter = adapter
    }
}
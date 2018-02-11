package com.example.lun.pocket_health_advisor


import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.View
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast


/**
 * Created by wlun on 2/10/18.
 */
class MedicReportHistoryFragment : ListFragment(){

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var categories = ArrayList<String>()
        categories.add("Neurology")
        var condition = MedicReportActivity.Condition(
                "Concussion",
                "Concussion",
                "acute",
                categories,
                "Please visit a neurologist as soon as possible.",
                "very rare",
                "moderate",
                "emergency")
        var medicReport = ArrayList<MedicReportActivity.MedicReport>()

        medicReport.add(MedicReportActivity.MedicReport(condition,null, null,"2017"))

        val adapter = MedicReportAdapter(context ,medicReport)
        listAdapter = adapter

//        var authUser = activity.intent.getSerializableExtra(USER_DETAILS) as AuthUser
//        activity.toast(authUser.name)
        val authUid = FirebaseAuth.getInstance().currentUser?.uid
        getReportFromDb(authUid)

    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        toast("Clicked item " + position.toString())
    }

    override fun onResume() {
        super.onResume()

    }

    private fun getReportFromDb(uid: String?) : DocumentSnapshot?{
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
                                doc.add(i)
                            }
                        } else {
                            activity.toast(task.exception.toString())
                        }
                    }
            return doc
        }
    }
}
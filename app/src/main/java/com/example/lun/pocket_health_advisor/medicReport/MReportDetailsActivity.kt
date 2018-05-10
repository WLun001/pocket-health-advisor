package com.example.lun.pocket_health_advisor.medicReport

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.example.lun.pocket_health_advisor.R
import com.example.lun.pocket_health_advisor.medicReport.MedicReportHistoryFragment.Companion.MEDIC_REPORT
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.*
import kotlinx.android.synthetic.main.activity_mreport_details.*
import kotlinx.android.synthetic.main.content_mreport_details.*

class MReportDetailsActivity : AppCompatActivity() {

    private lateinit var medicReport: MedicReport
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mreport_details)
        setSupportActionBar(toolbar)

        medicReport = intent.getSerializableExtra(MEDIC_REPORT) as MedicReport
        title = medicReport.diagnoseCondition.name
        report_details.text = constructReport(medicReport)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    private fun constructReport(medicReport: MedicReport): String {
        return """
            ${constructDiagnosedCondition(medicReport.diagnoseCondition)}
            ${constructPossibleConditions(medicReport.possibleConditions)}
            ${constructInitialSymptoms(medicReport.initialSymptoms)}
            ${constructQuestions(medicReport.questions)}
            """
    }


    private fun constructDiagnosedCondition(condition: Condition): String {
        return """
        Diagnosed Condition

        Name : ${condition.name}
        Acuteness : ${condition.acuteness}
        Categories : ${condition.categories}
        Prevalence : ${condition.prevalence}
        Severity : ${condition.severity}
        Triage Level : ${condition.triageLevel}
        Hints : ${condition.hints}
        """
    }

    private fun constructPossibleConditions(conditions: ArrayList<PossibleCondition>): String {
        val details = StringBuilder().append("Other Possible Conditions\n")
        for (cond in conditions) {
            details.append("""
                Name : ${cond.name}
                Probability " ${cond.probability}
                """)
        }
        return details.toString()
    }

    private fun constructInitialSymptoms(symptoms: ArrayList<InitialSyndrome>): String {
        val details = StringBuilder().append("Initial Symptoms\n")
        for (symptom in symptoms) {
            details.append("""
                Name : ${symptom.name}
                Choice " ${symptom.choice}
                """)
        }
        return details.toString()
    }

    private fun constructQuestions(question: ArrayList<Question>): String {
        val details = StringBuilder().append("Interview questions\n")
        for (ques in question) {
            details.append("""
                Question : ${ques.question}
                Symptom " ${ques.symptom}
                User Response : ${ques.userResponse}
                """)
        }
        return details.toString()
    }
}

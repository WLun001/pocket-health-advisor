package com.example.lun.pocket_health_advisor


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.ListFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import org.jetbrains.anko.support.v4.toast
import android.widget.ArrayAdapter



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
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        toast("Clicked item " + position.toString())
    }
//
}
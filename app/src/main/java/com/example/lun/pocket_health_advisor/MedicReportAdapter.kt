package com.example.lun.pocket_health_advisor

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.lun.pocket_health_advisor.DataClassWrapper.MedicReport

/**
 * Created by wlun on 2/10/18.
 */
class MedicReportAdapter(private var context: Context, private var medicReportList: ArrayList<MedicReport>)
    : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View?
        val viewHolder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.hospital_list_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
            Log.i("ViewHolder", "set Tag for ViewHolder, position: " + position)
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }
        Log.d("adapter", "populating list")
        viewHolder.condition.text = medicReportList[position].diagnoseCondition.name
        viewHolder.timestamp.text = medicReportList[position].timestamp
        viewHolder.triageLevel.text = medicReportList[position].diagnoseCondition.triageLevel

        return view
    }


    override fun getItem(position: Int): Any {
        return medicReportList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return medicReportList.size
    }

    private class ViewHolder(view: View?) {
        val condition: TextView = view?.findViewById<TextView>(R.id.hospital_name) as TextView
        val timestamp: TextView = view?.findViewById<TextView>(R.id.hospital_distance) as TextView
        val triageLevel: TextView = view?.findViewById<TextView>(R.id.hospital_opening_status) as TextView
    }
}
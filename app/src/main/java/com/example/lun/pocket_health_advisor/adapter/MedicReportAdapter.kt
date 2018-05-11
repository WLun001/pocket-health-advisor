package com.example.lun.pocket_health_advisor.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.lun.pocket_health_advisor.R
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.MedicReport
import java.util.*

/**
 * Created by wlun on 2/10/18.
 */
class MedicReportAdapter(private var context: Context, private var medicReportList: ArrayList<MedicReport>)
    : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View?
        val viewHolder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.custom_list_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
            Log.i("ViewHolder", "set Tag for ViewHolder, position: " + position)
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }
        Log.d("adapter", "populating list")
        viewHolder.condition.text = medicReportList[position].diagnoseCondition.commonName
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
        val condition: TextView = view?.findViewById(R.id.tv_title) as TextView
        val timestamp: TextView = view?.findViewById(R.id.tv_1) as TextView
        val triageLevel: TextView = view?.findViewById(R.id.tv_2) as TextView
    }
}
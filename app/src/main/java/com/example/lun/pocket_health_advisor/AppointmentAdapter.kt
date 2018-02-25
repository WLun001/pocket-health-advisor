package com.example.lun.pocket_health_advisor

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.lun.pocket_health_advisor.DataClassWrapper.Appointment

/**
 * Created by wlun on 2/14/18.
 */
class AppointmentAdapter(private val context: Context, private val appointmentList: ArrayList<Appointment>) : BaseAdapter(){
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
        viewHolder.hospitalName.text = appointmentList[position].hospitalName.capitalize()
        viewHolder.doctorName.text = appointmentList[position].doctorName
        viewHolder.time.text = appointmentList[position].time

        return view
    }

    override fun getItem(position: Int): Any {
        return appointmentList[position]
    }

    override fun getItemId(position: Int): Long {
      return position.toLong()
    }

    override fun getCount(): Int {
        return appointmentList.size
    }

    private class ViewHolder(view: View?){
        val hospitalName: TextView = view?.findViewById<TextView>(R.id.tv_title) as TextView
        val doctorName: TextView = view?.findViewById<TextView>(R.id.tv_1) as TextView
        val time: TextView = view?.findViewById<TextView>(R.id.tv_2) as TextView
    }
}
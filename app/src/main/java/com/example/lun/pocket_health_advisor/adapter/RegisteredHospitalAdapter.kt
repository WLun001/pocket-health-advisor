package com.example.lun.pocket_health_advisor.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.lun.pocket_health_advisor.R
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.RegisteredHospital

/**
 * Created by wlun on 4/15/18.
 */
class RegisteredHospitalAdapter(private val context: Context, private val hospitals: ArrayList<RegisteredHospital>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View?
        val viewHolder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.custom_list_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
            Log.i("ViewHolder", "set Tag for ViewHolder, position: $position")
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }
        Log.d("adapter", "populating list")
        viewHolder.hospitalName.text = hospitals[position].name.capitalize()
        viewHolder.hospitalNo.text = hospitals[position].contactNo
        viewHolder.consultationFee.text = "RM ".plus(hospitals[position].consultationFee)

        return view
    }

    override fun getItem(position: Int): Any {
        return hospitals[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return hospitals.size
    }

    private class ViewHolder(view: View?) {
        val hospitalName: TextView = view?.findViewById(R.id.tv_title) as TextView
        val hospitalNo: TextView = view?.findViewById(R.id.tv_1) as TextView
        val consultationFee: TextView = view?.findViewById(R.id.tv_2) as TextView
    }
}
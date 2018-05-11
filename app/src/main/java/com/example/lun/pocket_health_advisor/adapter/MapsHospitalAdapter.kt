package com.example.lun.pocket_health_advisor.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.lun.pocket_health_advisor.R
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.MapsHospital
import kotlinx.android.synthetic.main.custom_list_item.view.*

/**
 * Created by Wei Lun on 1/22/2018.
 */
class MapsHospitalAdapter(private var hospitalList: ArrayList<MapsHospital>,
                          private var listener: OnItemClickListener)
    : RecyclerView.Adapter<MapsHospitalAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(hospital: MapsHospital)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindView(hospitalList[position], listener)
    }

    override fun getItemCount(): Int {
        return hospitalList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.custom_list_item, parent, false)
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(hospital: MapsHospital, listener: OnItemClickListener) {
            val hospitalStatus = itemView.tv_1 as TextView

            itemView.tv_title.text = hospital.name
            itemView.tv_2.text = hospital.distance

            hospitalStatus.text = hospital.openingStatus
            if (hospital.openingStatus == "Open Now")
                hospitalStatus.setTextColor(Color.parseColor("#FF3BE215"))
            else
                hospitalStatus.setTextColor(Color.parseColor("#FFE22315"))

            itemView.setOnClickListener { listener.onItemClick(hospital) }
        }
    }
}

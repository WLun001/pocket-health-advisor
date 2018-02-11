package com.example.lun.pocket_health_advisor

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.lun.pocket_health_advisor.DataClassWrapper.MapsHospital
import kotlinx.android.synthetic.main.hospital_list_item.view.*

/**
 * Created by Wei Lun on 1/22/2018.
 */
class NearbyHospitalAdapter(var hospitalList: ArrayList<MapsHospital>,
                            var listener: OnItemClickListerner)
    : RecyclerView.Adapter<NearbyHospitalAdapter.ViewHolder>() {

    interface OnItemClickListerner {
        fun onItemClick(hospital: MapsHospital)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindView(hospitalList[position], listener)
    }

    override fun getItemCount(): Int {
        return hospitalList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        var v = LayoutInflater.from(parent?.context).inflate(R.layout.hospital_list_item, parent, false)
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(hospital: MapsHospital, listener: OnItemClickListerner) {
            var hospitalStatus = itemView.hospital_opening_status as TextView

            itemView.hospital_name.text = hospital.name
            itemView.hospital_distance.text = hospital.distance

            hospitalStatus.text = hospital.openingStatus
            if (hospital.openingStatus == "Open Now")
                hospitalStatus.setTextColor(Color.parseColor("#FF3BE215"))
            else
                hospitalStatus.setTextColor(Color.parseColor("#FFE22315"))

            itemView.setOnClickListener { listener.onItemClick(hospital) }

        }

    }
}

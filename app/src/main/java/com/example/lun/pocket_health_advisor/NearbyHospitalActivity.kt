package com.example.lun.pocket_health_advisor

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_nearby_hospital.*
import kotlinx.android.synthetic.main.content_nearby_hospital.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.io.Serializable
import java.net.URL

class NearbyHospitalActivity : AppCompatActivity() {

    companion object {
        val searchPlaceURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=3.041803%2C101.793075&rankby=distance&type=hospital&key=AIzaSyCbe3pFzJfI2wmyInY4hmsz2Fp7WoXSpZs\n"
        val detailsPlaceURL = ""
        val distanceURL = ""
    }

    data class Hospital(
            var name: String,
            var openingStatus: String,
            var distance: Double) : Serializable

    private var hospitals = ArrayList<Hospital>()
    private var adapter = NearbyHospitalAdapter(hospitals)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_hospital)
        setSupportActionBar(toolbar)

        var linearLayout = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        linearLayout.isAutoMeasureEnabled = true
        nearby_hospital_recycleview.layoutManager = linearLayout

        nearby_hospital_recycleview.adapter = adapter

        getNearbyHospital()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    fun getNearbyHospital() {
        doAsync {
            var result = URL(searchPlaceURL).readText()
            var array = JSONObject(result).getJSONArray("results")
            var status: Boolean?
            for (i in 0 until array.length()) {
                var name = array.getJSONObject(i).getString("name")

                if(array.getJSONObject(i).has("opening_hours")) {
                    status = array.getJSONObject(i).getJSONObject("opening_hours")
                            .getBoolean("open_now")
                } else status = null

                var statusDes: String
                when(status){
                    true -> { statusDes = getString(R.string.hospital_open_now) }
                    false -> { statusDes = getString(R.string.hospital_closed) }
                    null -> { statusDes = getString(R.string.hospital_unknown)}
                }
                hospitals.add(Hospital(name, statusDes, 0.2))
            }

            uiThread {
                adapter.notifyDataSetChanged()
                toast("successfully fetched hospitals!")
            }
        }
    }
}

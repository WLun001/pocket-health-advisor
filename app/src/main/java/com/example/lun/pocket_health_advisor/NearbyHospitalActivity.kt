package com.example.lun.pocket_health_advisor

import android.content.AsyncTaskLoader
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.LinearLayout
import com.google.gson.Gson

import kotlinx.android.synthetic.main.activity_nearby_hospital.*
import kotlinx.android.synthetic.main.content_nearby_hospital.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.io.BufferedReader
import java.io.Serializable
import java.net.URL

class NearbyHospitalActivity : AppCompatActivity() {

    companion object {
        val searchPlaceURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=3.041803%2C101.793075&radius=7000&type=hospital&key=AIzaSyCbe3pFzJfI2wmyInY4hmsz2Fp7WoXSpZs"
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

    fun getNearbyHospital(){
        doAsync {
            var result = URL(searchPlaceURL).readText()
            var array = JSONObject(result).getJSONArray("results")
            for(i in 0 until array.length()){
                Log.d("array count", array.length().toString())
                var name = array.getJSONObject(i).getString("name")
                hospitals.add(Hospital(name, "open", 0.2))
            }

            uiThread {
                adapter.notifyDataSetChanged()
                toast("successfully fetched hospitals!")
            }
        }
    }


}

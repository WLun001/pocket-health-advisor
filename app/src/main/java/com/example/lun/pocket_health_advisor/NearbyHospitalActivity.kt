package com.example.lun.pocket_health_advisor

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View.GONE
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_nearby_hospital.*
import kotlinx.android.synthetic.main.content_nearby_hospital.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.io.Serializable
import java.net.URL

class NearbyHospitalActivity : AppCompatActivity() {

    companion object {
        val googleApiKey = "AIzaSyAg3W8vlilMkGYNSpdlceSxCzZtGUlKrx8"
        val searchPlaceURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?&location="
                .plus(Uri.encode("3.041803,101.793075"))
        val distanceURL = "https://maps.googleapis.com/maps/api/distancematrix/json?"
        val detailsPlaceURL = ""
    }

    data class Hospital(
            var name: String,
            var openingStatus: String,
            var placeId: String,
            var distance: String? = null) : Serializable

    private var hospitals = ArrayList<Hospital>()
    private var adapter = NearbyHospitalAdapter(hospitals)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_hospital)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var linearLayout = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        linearLayout.isAutoMeasureEnabled = true
        nearby_hospital_recycleview.layoutManager = linearLayout

        getNearbyHospital()

        nearby_hospital_recycleview.adapter = adapter

//        var networkInfo = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
//                .activeNetworkInfo
//        networkInfo?.let {
//            if (networkInfo.isConnected)
//
//            else {
//                toast("No network connection")
//                hospital_progress_bar.visibility = View.GONE
//            }
//
//        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    private fun getNearbyHospital() {
        doAsync {
            var tempHospital = ArrayList<Hospital>()
            var uriBuilder = Uri.parse(searchPlaceURL)
                    .buildUpon()
                    .appendQueryParameter("rankby", "distance")
                    .appendQueryParameter("type", "hospital")
                    .appendQueryParameter("key", googleApiKey)
            Log.d("Nearby", uriBuilder.toString())

            var result = URL(uriBuilder.toString()).readText()
            var array = JSONObject(result).getJSONArray("results")
            var status: Boolean?
            for (i in 0 until array.length()) {
                var name = array.getJSONObject(i).getString("name")
                var placeId = array.getJSONObject(i).getString("place_id")
                Log.d("placeId", placeId)

                if (array.getJSONObject(i).has("opening_hours")) {
                    status = array.getJSONObject(i).getJSONObject("opening_hours")
                            .getBoolean("open_now")
                } else status = null

                var statusDes: String
                when (status) {
                    true -> {
                        statusDes = getString(R.string.hospital_open_now)
                    }
                    false -> {
                        statusDes = getString(R.string.hospital_closed)
                    }
                    null -> {
                        statusDes = getString(R.string.hospital_unknown)
                    }
                }
                tempHospital.add(Hospital(name, statusDes, placeId))
            }

            uiThread {
                getHospitalDistance(tempHospital)
            }
        }
    }

    private fun getHospitalDistance(tempHospital: ArrayList<Hospital>) {
        doAsync {
            for (i in tempHospital) {
                var location = Uri.encode("3.041803,101.793075")
                val uriBuilder = Uri.parse(distanceURL)
                        .buildUpon()
                        .encodedQuery("""origins=$location&destinations=place_id:${i.placeId}&key=$googleApiKey
                    """.trimIndent())


                Log.d("URL", uriBuilder.toString())

                val result = URL(uriBuilder.toString()).readText()
                val distance = JSONObject(result).getJSONArray("rows")
                        .getJSONObject(0).getJSONArray("elements")
                        .getJSONObject(0).getJSONObject("distance")
                        .getString("text")

                Log.d("Distance", distance.toString())

                hospitals.add(Hospital(i.name, i.openingStatus, i.placeId, distance))
                uiThread {
                    adapter.notifyDataSetChanged()
                    hospital_progress_bar.visibility = GONE
                }
            }
        }
    }
}

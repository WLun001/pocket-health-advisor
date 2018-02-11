package com.example.lun.pocket_health_advisor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View.GONE
import android.widget.LinearLayout
import com.example.lun.pocket_health_advisor.DataClassWrapper.MapsHospital
import com.example.lun.pocket_health_advisor.DataClassWrapper.MapsHospitalDetails
import com.example.lun.pocket_health_advisor.NearbyHospitalAdapter.OnItemClickListerner
import kotlinx.android.synthetic.main.activity_nearby_hospital.*
import kotlinx.android.synthetic.main.content_nearby_hospital.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL

class NearbyHospitalActivity : AppCompatActivity() {

    companion object {
        const val googleApiKey = "AIzaSyAg3W8vlilMkGYNSpdlceSxCzZtGUlKrx8"
        const val searchPlaceURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
        const val distanceURL = "https://maps.googleapis.com/maps/api/distancematrix/json?"
        val detailsPlaceURL = "https://maps.googleapis.com/maps/api/place/details/json?"
    }


    private var listener = object : OnItemClickListerner {
        override fun onItemClick(hospital: MapsHospital) {
            getHospitalDetails(hospital)
        }

    }
    private var hospitals = ArrayList<MapsHospital>()
    private var adapter = NearbyHospitalAdapter(hospitals, listener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_hospital)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var linearLayout = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        linearLayout.isAutoMeasureEnabled = true
        nearby_hospital_recycleview.layoutManager = linearLayout
        nearby_hospital_recycleview.adapter = adapter

        var networkInfo = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                .activeNetworkInfo
        networkInfo?.let {
            if (networkInfo.isConnected)
                toast("connected")
        }
        networkInfo ?: kotlin.run { toast("not connected") }
        getNearbyHospital()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    private fun getNearbyHospital() {
        doAsync {
            var location = Uri.encode("3.041803,101.793075")
            var tempHospital = ArrayList<MapsHospital>()
            var uriBuilder = Uri.parse(searchPlaceURL)
                    .buildUpon()
                    .encodedQuery("location=$location")
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
                tempHospital.add(MapsHospital(name, statusDes, placeId))
            }

            uiThread {
                getHospitalDistance(tempHospital)
            }
        }
    }

    private fun getHospitalDistance(tempHospital: ArrayList<MapsHospital>) {
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

                hospitals.add(MapsHospital(i.name, i.openingStatus, i.placeId, distance))

            }
            uiThread {
                hospitals.sortBy { hospital -> hospital.distance }
                adapter.notifyDataSetChanged()
                hospital_progress_bar.visibility = GONE
            }
        }
    }

    private fun getHospitalDetails(hospital: MapsHospital) {
        doAsync {
            var uriBuilder = Uri.parse(detailsPlaceURL)
                    .buildUpon()
                    .appendQueryParameter("placeid", hospital.placeId)
                    .appendQueryParameter("key", googleApiKey)

            Log.d("URL", uriBuilder.toString())

            var response = URL(uriBuilder.toString()).readText()
            val result = JSONObject(response).getJSONObject("result")
            var address = result.getString("formatted_address")
            var phoneNo = result.getString("formatted_phone_number")
            var name = result.getString("name")
            // var weekdayText = result.getJSONObject("opening_hours").getJSONArray()
            var placeId = result.getString("place_id")
            // var rating = result.getDouble("rating")
            var url = result.getString("url")
            //var website = result.getString("website")

            var hospitalDetails = MapsHospitalDetails(name, placeId, phoneNo, address, "", 0.0, "", url)

            uiThread {
                AlertDialog.Builder(this@NearbyHospitalActivity)
                        .setTitle("Details")
                        .setMessage(
                                """
                                Name : ${hospitalDetails.name}
                                Phone : ${hospitalDetails.phoneNo}
                                Address : ${hospitalDetails.address}
                                Rating : ${hospitalDetails.rating}
                                Website : ${hospitalDetails.website}
                                """
                        )
                        .setPositiveButton(R.string.button_ok, { dialogInterface, i ->
                        })
                        .create()
                        .show()
            }
        }
    }
}

package com.example.lun.pocket_health_advisor

import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.LinearLayout
import com.example.lun.pocket_health_advisor.DataClassWrapper.MapsHospital
import com.example.lun.pocket_health_advisor.DataClassWrapper.MapsHospitalDetails
import com.example.lun.pocket_health_advisor.NearbyHospitalAdapter.OnItemClickListerner
import kotlinx.android.synthetic.main.activity_nearby_hospital.*
import kotlinx.android.synthetic.main.content_nearby_hospital.*
import org.jetbrains.anko.*
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
    private lateinit var progress: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_hospital)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        progress = progressDialog("fetching hospitals")

        val linearLayout = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        linearLayout.isAutoMeasureEnabled = true
        nearby_hospital_recycleview.layoutManager = linearLayout
        nearby_hospital_recycleview.adapter = adapter

        val networkInfo = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
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
        progress.show()
        doAsync {
            val location = Uri.encode("3.041803,101.793075")
            val uriBuilder = Uri.parse(searchPlaceURL)
                    .buildUpon()
                    .encodedQuery("location=$location")
                    .appendQueryParameter("rankby", "distance")
                    .appendQueryParameter("type", "hospital")
                    .appendQueryParameter("key", googleApiKey)
            Log.d("Nearby", uriBuilder.toString())

            val result = URL(uriBuilder.toString()).readText()
            val array = JSONObject(result).getJSONArray("results")
            progress.max = array.length()
            var status: Boolean?
            for (i in 0 until array.length()) {
                val name = array.getJSONObject(i).getString("name")
                val placeId = array.getJSONObject(i).getString("place_id")
                Log.d("placeId", placeId)

                status = if (array.getJSONObject(i).has("opening_hours")) {
                    array.getJSONObject(i).getJSONObject("opening_hours")
                            .getBoolean("open_now")
                } else null

                val statusDes: String = when (status) {
                    true -> {
                        getString(R.string.hospital_open_now)
                    }
                    false -> {
                        getString(R.string.hospital_closed)
                    }
                    null -> {
                        getString(R.string.hospital_unknown)
                    }
                }
                getHospitalDistance(MapsHospital(name, statusDes, placeId))
            }
        }
    }

    private fun getHospitalDistance(mapsHospital: MapsHospital) {
//        val progress = progressDialog("Calculating distances")
//        progress.max = tempHospital.size
//        progress.show()
        doAsync {
                val location = Uri.encode("3.041803,101.793075")
                val uriBuilder = Uri.parse(distanceURL)
                        .buildUpon()
                        .encodedQuery("""origins=$location&destinations=place_id:${mapsHospital.placeId}&key=$googleApiKey
                    """.trimIndent())


                Log.d("URL", uriBuilder.toString())

                val result = URL(uriBuilder.toString()).readText()
                val distance = JSONObject(result).getJSONArray("rows")
                        .getJSONObject(0).getJSONArray("elements")
                        .getJSONObject(0).getJSONObject("distance")
                        .getString("text")

                Log.d("Distance", distance.toString())

            onComplete {
                hospitals.add(MapsHospital(mapsHospital.name, mapsHospital.openingStatus, mapsHospital.placeId, distance))
                progress.incrementProgressBy(1)
                uiThread {
                    if (progress.isShowing && progress.progress == progress.max) {
                        hospitals.sortedWith(compareBy { it.distance })
                        adapter.notifyDataSetChanged()
                        progress.dismiss()
                    }
            }

            }
        }
    }

    private fun getHospitalDetails(hospital: MapsHospital) {
        doAsync {
            val uriBuilder = Uri.parse(detailsPlaceURL)
                    .buildUpon()
                    .appendQueryParameter("placeid", hospital.placeId)
                    .appendQueryParameter("key", googleApiKey)

            Log.d("URL", uriBuilder.toString())

            val response = URL(uriBuilder.toString()).readText()
            val result = JSONObject(response).getJSONObject("result")
            val address = result.getString("formatted_address")
            val phoneNo = result.getString("formatted_phone_number")
            val name = result.getString("name")
            // var weekdayText = result.getJSONObject("opening_hours").getJSONArray()
            val placeId = result.getString("place_id")
            // var rating = result.getDouble("rating")
            val url = result.getString("url")
            //var website = result.getString("website")

            val hospitalDetails = MapsHospitalDetails(name, placeId, phoneNo, address, "", 0.0, "", url)

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
                        .setPositiveButton(R.string.button_ok, { _, i ->
                        })
                        .create()
                        .show()
            }
        }
    }
}

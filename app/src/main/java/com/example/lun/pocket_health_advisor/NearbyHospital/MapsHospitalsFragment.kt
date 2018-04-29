package com.example.lun.pocket_health_advisor.NearbyHospital

import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.lun.pocket_health_advisor.R
import com.example.lun.pocket_health_advisor.adapter.MapsHospitalAdapter
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper
import kotlinx.android.synthetic.main.fragment_hospital.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.noButton
import org.jetbrains.anko.onComplete
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.progressDialog
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL

/**
 * Created by wlun on 4/15/18.
 */
class MapsHospitalsFragment : Fragment() {

    companion object {
        const val googleApiKey = "AIzaSyAg3W8vlilMkGYNSpdlceSxCzZtGUlKrx8"
        const val searchPlaceURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
        const val distanceURL = "https://maps.googleapis.com/maps/api/distancematrix/json?"
        const val detailsPlaceURL = "https://maps.googleapis.com/maps/api/place/details/json?"
    }

    private var listener = object : MapsHospitalAdapter.OnItemClickListener {
        override fun onItemClick(hospital: DataClassWrapper.MapsHospital) {
            getHospitalDetails(hospital)
        }

    }
    private var hospitals = ArrayList<DataClassWrapper.MapsHospital>()
    private var adapter = MapsHospitalAdapter(hospitals, listener)
    private lateinit var progress: ProgressDialog

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_hospital, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        progress = progressDialog("fetching hospitals")

        val linearLayout = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        linearLayout.isAutoMeasureEnabled = true
        hospital_recycleview.layoutManager = linearLayout
        hospital_recycleview.adapter = adapter

        val networkInfo = (activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                .activeNetworkInfo
        networkInfo?.let {
            if (networkInfo.isConnected) {
                toast("connected")
                getNearbyHospital()
            }
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
                getHospitalDistance(DataClassWrapper.MapsHospital(name, statusDes, placeId))
            }
        }
    }

    private fun getHospitalDistance(mapsHospital: DataClassWrapper.MapsHospital) {
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
                hospitals.add(DataClassWrapper.MapsHospital(mapsHospital.name, mapsHospital.openingStatus, mapsHospital.placeId, distance))
                progress.incrementProgressBy(1)
                uiThread {
                    if (progress.isShowing && progress.progress == progress.max) {
                        hospitals.sortWith(compareBy { it.distance })
                        adapter.notifyDataSetChanged()
                        progress.dismiss()
                    }
                }

            }
        }
    }

    private fun getHospitalDetails(hospital: DataClassWrapper.MapsHospital) {
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

            val hospitalDetails = DataClassWrapper.MapsHospitalDetails(name, placeId, phoneNo, address, "", 0.0, "", url)

            uiThread {
                alert("""
                                Name : ${hospitalDetails.name}
                                Phone : ${hospitalDetails.phoneNo}
                                Address : ${hospitalDetails.address}
                                Rating : ${hospitalDetails.rating}
                                Website : ${hospitalDetails.website}
                                """) {
                    positiveButton(R.string.button_ok) { dialog ->
                        dialog.dismiss()
                    }
                    noButton { }
                }.show()
            }
        }
    }
}
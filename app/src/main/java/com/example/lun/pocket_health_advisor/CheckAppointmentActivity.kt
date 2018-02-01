package com.example.lun.pocket_health_advisor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.example.lun.pocket_health_advisor.MainActivity.AuthUser
import com.example.lun.pocket_health_advisor.MainActivity.HospitalUser
import com.example.lun.pocket_health_advisor.R.id.check_appointment
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.toast

class CheckAppointmentActivity : AppCompatActivity() {

    lateinit var authUser: AuthUser
    lateinit var hospitalUser: HospitalUser

    data class HospitalDetails(
            var name: String = ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_appointment)

        authUser = intent.getSerializableExtra(MainActivity.USER_DETAILS) as AuthUser

        val listView = findViewById(R.id.list) as ListView
        listView.adapter = ListViewAdapter(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.check_appointment_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id) {
            check_appointment -> {
                getHospitalUser()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun getHospitalUser() {
        var firestore = FirebaseFirestore.getInstance()
                .collection("patients")
                .whereEqualTo("name", authUser.name)
                .get()
                .addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        var result = task.result
                        result?.let {
                            if (result.size() > 0) {
                                for (i in result) {
                                    hospitalUser = MainActivity.HospitalUser(
                                            i.getString("ic"),
                                            i.getString("id"),
                                            i.getString("name"),
                                            i.getString("hospital_id"),
                                            i.getString("age")
                                    )
                                }
                                getHospitalDetails(hospitalUser)
                                //toast(name.name)
                            } else toast("no matches found")
                        }

                    } else toast("No matches found")

                })
                .addOnFailureListener { toast("No matches found") }
    }

    fun getHospitalDetails(hospitalUser: HospitalUser) {
        var hospitalDetails = HospitalDetails()
        var firestore = FirebaseFirestore.getInstance()
                .collection("hospitals")
                .whereEqualTo("id", hospitalUser.hospitalId)
                .get()
                .addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        result?.let {
                            if (result.size() > 0) {
                                for (i in result) {
                                    hospitalDetails.name = i.getString("name")
                                }
                                var message = """
                                    Name : ${hospitalUser.name}
                                    Hospital : ${hospitalDetails.name}
                                    """
                                AlertDialog.Builder(this)
                                        .setTitle("Appointment")
                                        .setMessage(message)
                                        .create()
                                        .show()
                            } else toast("No hospital found")
                        }
                    }
                })
                .addOnFailureListener { toast("No hospital found!") }
    }

    private class ListViewAdapter(private val context: Context) : BaseAdapter() {
        internal var sList = arrayOf("One,", "two", "three")
        private val inflator: LayoutInflater = LayoutInflater.from(context)

        override fun getCount(): Int {
            return sList.size
        }

        override fun getItem(position: Int): Any {
            return sList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

//        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//            var view: View = convertView as View
//            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//
//            view = inflater.inflate(R.layout.list_row, parent, false)
//            val tvTitle = view.findViewById<TextView>(R.id.label)
//            tvTitle.setText("Monday Appointment")
//
//
//        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val view: View?
            val viewHolder: ListRowHolder
            if (convertView == null) {
                view = this.inflator.inflate(R.layout.list_row, parent, false)
                viewHolder = ListRowHolder(view)
                view.tag = viewHolder
            } else {
                view = convertView
                viewHolder = view.tag as ListRowHolder
            }

            viewHolder.label.text = sList[position]
            view?.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val intent = Intent(context, SinchLoginActivity::class.java)
                    context.startActivity(intent)
                }

            })

            return view
        }
    }

    private class ListRowHolder(row: View?) {

        public val label: TextView

        init {
            this.label = row?.findViewById<TextView>(R.id.title) as TextView
        }
    }


}

package com.example.lun.pocket_health_advisor

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView

class CheckAppointmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_appointment)

        val listView = findViewById(R.id.list) as ListView
        listView.adapter = ListViewAdapter(this)
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

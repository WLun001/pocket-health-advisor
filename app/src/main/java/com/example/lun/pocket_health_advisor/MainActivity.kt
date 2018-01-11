package com.example.lun.pocket_health_advisor

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.GridLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action kotlin", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val GridLayout = mainGrid as GridLayout

        setSingleEvent(gridLayout = GridLayout)
    }

    fun setSingleEvent(gridLayout: GridLayout){
        for (count in 0..4){

            var cardView = gridLayout.getChildAt(count) as? CardView
            //val finalI: Int = count
            cardView?.setOnClickListener(object: View.OnClickListener {
                override fun onClick(v: View?) {
                     when(count) {
                        0 -> {
                            startActivity(Intent(applicationContext, ChatbotActivity::class.java))
                        }
                        3 -> {
                             val intent = Intent(applicationContext, CheckAppointmentActivity::class.java)
                             startActivity(intent)
                         }
                     }
                }

            })
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_login) {
            true
        } else super.onOptionsItemSelected(item)

    }

    fun payment(view: View){
        startActivity(Intent(this, PaymentActivity::class.java))
    }
}

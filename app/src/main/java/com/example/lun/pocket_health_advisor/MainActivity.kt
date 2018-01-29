package com.example.lun.pocket_health_advisor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import com.example.lun.pocket_health_advisor.R.id.*
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.toast
import java.io.Serializable
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        val USER_DETAILS: String = "com.example.lun.pocket_health_advisor.USER_DETAILS"
    }

    data class User(var id: String, var name: String = "") : Serializable

    lateinit var auth: FirebaseAuth
    lateinit var authListener: FirebaseAuth.AuthStateListener
    lateinit var user: FirebaseUser

    val RC_SIGN_IN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        auth = FirebaseAuth.getInstance()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action kotlin", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val GridLayout = mainGrid as GridLayout

        setSingleEvent(gridLayout = GridLayout)

        authListener = FirebaseAuth.AuthStateListener { auth ->

            var firebaseUser: FirebaseUser? = auth.currentUser
            if (firebaseUser == null) {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(
                                        Arrays.asList(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                .build(),
                        RC_SIGN_IN)
            } else {
                user = firebaseUser
            }
        }
    }

    override fun onResume() {
        super.onResume()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        if (authListener != null) {
            auth.removeAuthStateListener(authListener)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_SIGN_IN -> {
                if (resultCode == Activity.RESULT_OK)
                    Toast.makeText(this, "Welcome Back!", Toast.LENGTH_LONG)
                else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Until Next Time!", Toast.LENGTH_SHORT).show()
                    finish()
                }

            }
        }
    }

    fun setSingleEvent(gridLayout: GridLayout) {
        for (count in 0..4) {

            var cardView = gridLayout.getChildAt(count) as? CardView
            //val finalI: Int = count
            cardView?.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    when (count) {
                        0 -> {
                            var userName = ""
                            user.displayName?.let { userName = user.displayName as String }

                            var userDetails = User(user.uid, userName)

                            val intent = Intent(applicationContext, ChatbotActivity::class.java)
                            intent.putExtra(USER_DETAILS, userDetails as Serializable)
                            intent.putExtras(intent)

                            startActivity(intent)
                        }

                        1 -> {
                            startActivity(Intent(applicationContext, NearbyHospitalActivity::class.java))
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

        when (id) {
            sign_out -> AuthUI.getInstance().signOut(this)

            nearby_hospital -> startActivity(Intent(this, NearbyHospitalActivity::class.java))

            check_appointment -> {
                var firestore = FirebaseFirestore.getInstance()
                        .collection("patients")
                        .whereEqualTo("name", "yong")
                        .get()
                        .addOnCompleteListener({
                            task -> if (task.isSuccessful) {
                            var result = task.result
                            result?.let {
                                if (result.size() > 0) {
                                    for (i in result) {
                                        var age = i.get("age")
                                        toast(age.toString())

                                    }
                                }else toast("no matches found")
                            }

                        } else toast("No matches found")

                        })
                        .addOnFailureListener { toast("No matches found") }
            }
        }
        return super.onOptionsItemSelected(item)

    }

    fun payment(view: View) {
        startActivity(Intent(this, PaymentActivity::class.java))
    }
}

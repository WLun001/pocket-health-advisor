package com.example.lun.pocket_health_advisor

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import com.example.lun.pocket_health_advisor.NearbyHospital.HospitalActivity
import com.example.lun.pocket_health_advisor.R.id.auth_user
import com.example.lun.pocket_health_advisor.R.id.sign_out
import com.example.lun.pocket_health_advisor.appointment.AppointmentActivity
import com.example.lun.pocket_health_advisor.chatbot.ChatbotActivity
import com.example.lun.pocket_health_advisor.medicReport.MedicReportActivity
import com.example.lun.pocket_health_advisor.payment.PaymentActivity
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.AuthUser
import com.example.lun.pocket_health_advisor.videoCall.SinchLoginActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.toast
import java.io.Serializable
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        val USER_DETAILS: String = "com.example.lun.pocket_health_advisor.USER_DETAILS"
        private const val APP_KEY = "cc277e12-542f-4612-97d7-f67e7b2f85e5"
        private const val APP_SECRET = "XIcRHW3cm06Nba+n6UMmuQ=="
        private const val ENVIRONMENT = "sandbox.sinch.com"
        private const val RC_SIGN_IN = 1
    }

    lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var authUser: AuthUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        auth = FirebaseAuth.getInstance()
        checkSinchClient()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action kotlin", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val gridLayout = mainGrid as GridLayout

        setSingleEvent(gridLayout = gridLayout)

        authListener = FirebaseAuth.AuthStateListener { auth ->

            val firebaseUser: FirebaseUser? = auth.currentUser
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
                this.firebaseUser = firebaseUser
                var userName = ""
                firebaseUser.displayName?.let { userName = firebaseUser.displayName as String }
                authUser = AuthUser(firebaseUser.uid, userName)
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

    private fun setSingleEvent(gridLayout: GridLayout) {
        for (count in 0..4) {

            val cardView = gridLayout.getChildAt(count) as? CardView
            //val finalI: Int = count
            cardView?.setOnClickListener {
                when (count) {
                    0 -> {
                        val intent = Intent(applicationContext, ChatbotActivity::class.java)
                        startActivity(intent)
                    }

                    1 -> startActivity(Intent(applicationContext, HospitalActivity::class.java))

                    2 -> {
                        val intent = Intent(this, MedicReportActivity::class.java)
                        intent.putExtra(USER_DETAILS, authUser as Serializable)
                        intent.putExtras(intent)
                        startActivity(intent)
                    }

                    3 -> {
                        val intent = Intent(applicationContext, AppointmentActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
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

            auth_user -> {
                var builder = AlertDialog.Builder(this)
                        .setView(R.layout.auth_user_details_dialog)
                        .setPositiveButton(R.string.button_ok,
                                { dialogInterface, _ ->
                                    val dialog = dialogInterface as Dialog
                                    val newName = dialog.findViewById<EditText>(R.id.display_name)
                                            .text.toString()
                                    if (newName.isNotEmpty()) {
                                        updateAuthUser(newName)
                                    }
                                })
                        .setNegativeButton(R.string.button_cancel,
                                { _, _ ->
                                    toast("cancel")
                                })
                        .setTitle(authUser.name)
                        .create()
                        .show()

            }
        }
        return super.onOptionsItemSelected(item)

    }

    fun payment(view: View) {
        startActivity(Intent(this, PaymentActivity::class.java))
    }

    private fun updateAuthUser(newName: String) {
        val userProfile = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
        firebaseUser.updateProfile(userProfile)
        toast(R.string.updated_display_name)
    }

    private fun checkSinchClient() {
//        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        var sinchClientStatus = sharedPreferences.getBoolean("sinch_client", false)
//        if (!sinchClientStatus){
//            alert("Enable Video call feature?"){
//                yesButton {
        startActivity(Intent(applicationContext, SinchLoginActivity::class.java))
//                    sharedPreferences.edit().putBoolean("sinch_client", true).apply()
//                    toast("registered sinch client")
//                }
//                noButton { }
//            }.show()
//        }
    }
}

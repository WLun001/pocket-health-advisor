package com.example.lun.pocket_health_advisor


import com.bra

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginActivity : BaseActivity(), SinchService.StartFailedListener {

    private var mLoginButton: Button? = null
    private var mLoginName: EditText? = null
    private var mSpinner: ProgressDialog? = null

    protected fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        mLoginName = findViewById(R.id.loginName) as EditText

        mLoginButton = findViewById(R.id.loginButton) as Button
        mLoginButton!!.isEnabled = false
        mLoginButton!!.setOnClickListener { loginClicked() }
    }

    protected fun onServiceConnected() {
        mLoginButton!!.isEnabled = true
        getSinchServiceInterface().setStartListener(this)
    }

    protected fun onPause() {
        if (mSpinner != null) {
            mSpinner!!.dismiss()
        }
        super.onPause()
    }

    fun onStartFailed(error: SinchError) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
        if (mSpinner != null) {
            mSpinner!!.dismiss()
        }
    }

    fun onStarted() {
        openPlaceCallActivity()
    }

    private fun loginClicked() {
        val userName = mLoginName!!.text.toString()

        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show()
            return
        }

        if (userName != getSinchServiceInterface().getUserName()) {
            getSinchServiceInterface().stopClient()
        }

        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(userName)
            showSpinner()
        } else {
            openPlaceCallActivity()
        }
    }

    private fun openPlaceCallActivity() {
        val mainActivity = Intent(this, PlaceCallActivity::class.java)
        startActivity(mainActivity)
    }

    private fun showSpinner() {
        mSpinner = ProgressDialog(this)
        mSpinner!!.setTitle("Logging in")
        mSpinner!!.setMessage("Please wait...")
        mSpinner!!.show()
    }
}

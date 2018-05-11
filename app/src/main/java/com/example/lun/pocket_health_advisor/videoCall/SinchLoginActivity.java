package com.example.lun.pocket_health_advisor.videoCall;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.sinch.android.rtc.SinchError;

public class SinchLoginActivity extends BaseActivity implements SinchService.StartFailedListener {

    private Button mLoginButton;
    private EditText mLoginName;
    private ProgressDialog mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sinch_login);
//
//        mLoginName = (EditText) findViewById(R.id.loginName);
//
//        mLoginButton = (Button) findViewById(R.id.loginButton);
//        mLoginButton.setEnabled(false);
//        mLoginButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                loginClicked();
//            }
//        });
    }

    @Override
    protected void onServiceConnected() {
        //mLoginButton.setEnabled(true);
        getSinchServiceInterface().setStartListener(this);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            loginClicked(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            loginClicked("111");
        }
    }

    @Override
    protected void onPause() {
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
    }

    @Override
    public void onStarted() {
        openPlaceCallActivity();
    }

    private void loginClicked(String userName) {

//        if (userName.isEmpty()) {
//            Toast.makeText(this, "Please enter a hospitalName", Toast.LENGTH_LONG).show();
//            return;
//        }

        if (!userName.equals(getSinchServiceInterface().getUserName())) {
            getSinchServiceInterface().stopClient();
        }

        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(userName);
            showSpinner();
        } else {
            openPlaceCallActivity();
        }
    }

    private void openPlaceCallActivity() {
        finish();
//        Intent mainActivity = new Intent(this, PlaceCallActivity.class);
//        startActivity(mainActivity);
    }

    private void showSpinner() {
        mSpinner = new ProgressDialog(this);
        mSpinner.setTitle("Logging in");
        mSpinner.setMessage("Please wait...");
        mSpinner.show();
    }
}

package com.example.lun.pocket_health_advisor.payment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.HttpClient;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.example.lun.pocket_health_advisor.MainActivity;
import com.example.lun.pocket_health_advisor.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import static com.example.lun.pocket_health_advisor.ulti.ConstWrapper.PAYMENT_CONSULTATION_FEE;
import static com.example.lun.pocket_health_advisor.ulti.ConstWrapper.PAYMENT_DIAGNOSIS_PRICE;

public class PaymentActivity extends AppCompatActivity {

    final int REQUEST_CODE = 1;
    //replace to your own server when necessary
    final String get_token = "http://10.0.2.2:5000/client_token";
    final String send_payment_details = "http://10.0.2.2:5000/checkout";

    private String token, amount;
    private HashMap<String, String> paramHash;
    private Button btnPay;
    private EditText etAmount;
    private LinearLayout llHolder;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;
    private String appointmentId;
    private String appID;
    private int paymentType;
    private String hospitalId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        llHolder = (LinearLayout) findViewById(R.id.llHolder);
        etAmount = (EditText) findViewById(R.id.etPrice);
        btnPay = (Button) findViewById(R.id.btnPay);
        //TODO: get patient id from intent

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        paymentType = intent.getIntExtra(getString(R.string.payment_type), PAYMENT_CONSULTATION_FEE);
        if (paymentType == PAYMENT_CONSULTATION_FEE) {
            appID = intent.getStringExtra("appointment_id");
            hospitalId = intent.getStringExtra("hospital_id");
            getConsultationPrice();
        } else {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Waiting for amount");
            progressDialog.setMessage("Please wait for doctor to key in amount, do not close the app");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            String remoteCallerId = intent.getStringExtra("remote_id");
            searchAppointment(remoteCallerId);
        }

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBraintreeSubmit();
            }
        });
        new HttpRequest().execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                String stringNonce = nonce.getNonce();
                Log.d("mylog", "Result: " + stringNonce);
                // Send payment price with the nonce
                // use the result to update your UI and send the payment method nonce to your server
                if (!etAmount.getText().toString().isEmpty()) {
                    amount = etAmount.getText().toString();
                    paramHash = new HashMap<>();
                    paramHash.put("amount", amount);
                    paramHash.put("nonce", stringNonce);
                    sendPaymentDetails();
                } else
                    Toast.makeText(PaymentActivity.this, "Please enter a valid amount.", Toast.LENGTH_SHORT).show();

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // the authUser canceled
                Log.d("mylog", "authUser canceled");
            } else {
                // handle errors here, an exception may be available in
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.d("mylog", "Error : " + error.toString());
            }
        }
    }

    private void searchAppointment(String remoteCallerId) {
        //TODO: Change to patient ic and doctor ic
        db.collection("appointments")
                .whereEqualTo("patient_id", "5a234c39-3999-d6e3-8526-f97a3128bcf2")
                .whereEqualTo("doctor_name", remoteCallerId)
                .whereEqualTo("date",/* new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime())*/"31-05-2018")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().size() > 0) {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            //get appointment id
                            appointmentId = doc.getId();
                            getDiagnosisPrice(appointmentId);
                        }
                    }
                });
    }

    /**
     * This method attached a listener to get diagnosis fee from appointments collection
     *
     * @param appointmentId id of an appointment
     */
    private void getDiagnosisPrice(String appointmentId) {
        db.collection("appointments")
                .whereEqualTo("id", appointmentId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        DocumentSnapshot doc = documentSnapshots.getDocuments().get(0);
                        Log.d("doc", doc.getData().toString());
                        if (doc.get("diagnosis_price") != null) {
                            etAmount.setText(doc.get("diagnosis_price").toString());
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    /**
     * This method get consultation amount
     */
    private void getConsultationPrice() {
        db.collection("hospitals")
                .whereEqualTo("id", hospitalId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().size() > 0) {
                    etAmount.setText(task.getResult().getDocuments().get(0).get("consultation_fee").toString());
                }
            }
        });
    }

    /**
     * This method to write payment details to Firestore
     */
    private void recordPayment() {
        //TODO: get patient id from intent or db
        db.collection("appointments")
                .whereEqualTo("patient_id", "5a234c39-3999-d6e3-8526-f97a3128bcf2")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        Map<String, Object> data = new HashMap<>();
                        data.put("hospital_id", doc.getString("hospital_id"));
                        data.put("patient_id", doc.getString("patient_id"));
                        data.put("patient_name", doc.getString("patient_name"));
                        data.put("diagnosis_price", doc.getDouble("diagnosis_price"));
                        data.put("appointment_id", doc.getId());
                        data.put("timestmap", FieldValue.serverTimestamp());

                        db.collection("payments").add(data)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(PaymentActivity.this, "recorded payment", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }

    /**
     * This method change the payment status in patients collection
     */
    private void recordPatientPaymentStatus() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("payment_status", true);
        db.collection("patients").document("5a234c39-3999-d6e3-8526-f97a3128bcf2")
                .update(data);
    }

    /**
     * This method change the payment status in appointment collection
     */
    private void recordAppointmentPaymentStatus() {
        if (appID != null) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("payment_status", true);
            db.collection("appointments").document(appID)
                    .update(data);
        }
    }

    public void onBraintreeSubmit() {
        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(token);
        startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE);
    }

    private void sendPaymentDetails() {
        RequestQueue queue = Volley.newRequestQueue(PaymentActivity.this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, send_payment_details,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("Successful")) {
                            if (paymentType == PAYMENT_DIAGNOSIS_PRICE) {
                                recordPayment();
                                recordPatientPaymentStatus();
                            } else recordAppointmentPaymentStatus();
                            Toast.makeText(PaymentActivity.this, "Transaction successful", Toast.LENGTH_LONG).show();
                            llHolder.setVisibility(View.GONE);

                            Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else
                            Toast.makeText(PaymentActivity.this, "Transaction failed", Toast.LENGTH_LONG).show();
                        Log.d("mylog", "Final Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Volley error : " + error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                if (paramHash == null)
                    return null;
                Map<String, String> params = new HashMap<>();
                for (String key : paramHash.keySet()) {
                    params.put(key, paramHash.get(key));
                    Log.d("mylog", "Key : " + key + " Value : " + paramHash.get(key));
                }

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(stringRequest);
    }

    private class HttpRequest extends AsyncTask {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(PaymentActivity.this, android.R.style.Theme_DeviceDefault_Dialog);
            progress.setCancelable(false);
            progress.setMessage("We are contacting our servers for token, Please wait");
            progress.setTitle("Getting token");
            progress.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            HttpClient client = new HttpClient();
            client.get(get_token, new HttpResponseCallback() {
                @Override
                public void success(String responseBody) {
                    Log.d("mylog", responseBody);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PaymentActivity.this, "Successfully got token", Toast.LENGTH_SHORT).show();
                            llHolder.setVisibility(View.VISIBLE);
                        }
                    });
                    token = responseBody;
                }

                @Override
                public void failure(Exception exception) {
                    final Exception ex = exception;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PaymentActivity.this, "Failed to get token: " + ex.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            progress.dismiss();
        }
    }
}

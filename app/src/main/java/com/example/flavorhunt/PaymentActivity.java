package com.example.flavorhunt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {
    Button basePlan;
    String Publishable_key = "pk_test_51NCzZpSAkTCEHeoNqMecjeYvGshTl2cNNVVhCx5EjIWdLm47FT9uEhxXiAuSle7WUZfZDNQ4AhtJ3gwW1EkeRBel00Zg4pABtS";
    String Secret_key = "sk_test_51NCzZpSAkTCEHeoN2cvhOGQjCXE8lo5xlzm9m8qh5M4Bd7SVq3tFc0KBfWVKPtMtjMMZ0ZDvB4zpnougfPWdtvAz00yL3PCUar";
    String Customerid;
    String EmphericalKey;
    String ClientSecret;
    FirebaseAuth auth;
    FirebaseFirestore db;

    PaymentSheet paymentSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        basePlan = findViewById(R.id.pay_base);
        basePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = auth.getCurrentUser().getUid();
                DocumentReference userDocRef = db.collection("users").document(userId);

                userDocRef.update("payment","active")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Payment status updated successfully
                                            Toast.makeText(PaymentActivity.this, "Payment successful. Enjoy!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Error updating payment status
                                            Toast.makeText(PaymentActivity.this, "Error updating payment status: " + task.getException(), Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                paymentflow();
            }
        });
        PaymentConfiguration.init(this,Publishable_key);
        paymentSheet = new PaymentSheet(this,paymentSheetResult -> {
            onPaymentResult(paymentSheetResult);

        });


        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/customers",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            Customerid = object.getString("id");
                            Toast.makeText(PaymentActivity.this, Customerid, Toast.LENGTH_SHORT).show();
                            getEmphericalKey();

                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PaymentActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + Secret_key);

                return header;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }


    private void paymentflow() {
        paymentSheet.presentWithPaymentIntent(ClientSecret,new PaymentSheet.Configuration("Learn ", new PaymentSheet.CustomerConfiguration(
                Customerid,EmphericalKey
        )));
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Successfull", Toast.LENGTH_SHORT).show();
        }
    }

        private void getEmphericalKey () {
            StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/ephemeral_keys",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject object = new JSONObject(response);
                                Customerid = object.getString("id");
                                Toast.makeText(PaymentActivity.this, Customerid, Toast.LENGTH_SHORT).show();
                                getClientSecret(Customerid, EmphericalKey);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(PaymentActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("Authorization", "Bearer " + Secret_key);
                    header.put("Stripe-Version", "2022-11-15");

                    return header;
                }

                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("customer", Customerid);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(request);
        }

    private void getClientSecret(String customerid, String emphericalKey) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            ClientSecret = object.getString("client_secret");
                            Toast.makeText(PaymentActivity.this, ClientSecret, Toast.LENGTH_SHORT).show();


                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PaymentActivity.this, "srgnkrngk", Toast.LENGTH_SHORT).show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + Secret_key);

                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer",Customerid);
                params.put("amount","300"+"00");
                params.put("currency","INR");
                params.put("automatic_payment_methods[enabled]","true");

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
}

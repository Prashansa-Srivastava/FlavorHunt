package com.example.flavorhunt.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.flavorhunt.MainActivity2;
import com.example.flavorhunt.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText inputEmail,inputPassword;
    Button btn_Login,btn_register;
    public static final String Shared_Pref = " sharePreference";
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_register = findViewById(R.id.register_now);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });
        inputEmail = findViewById(R.id.userid);
        inputPassword = findViewById(R.id.password);
        auth = FirebaseAuth.getInstance();
        checkbox();
        btn_Login = findViewById(R.id.login_btn);
        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    inputEmail.setError("Email is required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    inputPassword.setError("Password is required");
                    return;
                }


                auth.signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                SharedPreferences sharedPreferences = getSharedPreferences(Shared_Pref,MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("name","true");
                                editor.apply();
                                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                // Create Firestore document for user
                                createUserDocument();

                                Intent intent = new Intent(LoginActivity.this,MainActivity2.class);
                                startActivity(intent);
                                finish();

                            }
                            else {
                                Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private void createUserDocument() {
        // Get the current user's unique identifier (e.g., UID)
        String userId = auth.getCurrentUser().getUid();

        // Get a reference to the Firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a document reference with the user's unique identifier in the "users" collection
        DocumentReference userDocRef = db.collection("users").document(userId);

        // Create a HashMap to store user information
        Map<String, Object> userData = new HashMap<>();
        userData.put("subscriptionStatus", "active"); // Example field: subscriptionStatus
        userData.put("recipeViewCount", 0); // Example field: recipeViewCount

        // Set the document with the user's information
        userDocRef.set(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Document created successfully
                            Toast.makeText(LoginActivity.this, "User document created successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Error creating document
                            Toast.makeText(LoginActivity.this, "Error creating user document: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkbox() {
        SharedPreferences sharedPreferences = getSharedPreferences(Shared_Pref,MODE_PRIVATE);
        String check = sharedPreferences.getString("name","");

        if(check .equals("true")){
            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this,MainActivity2.class);
            startActivity(intent);
            finish();

        }
    }
}
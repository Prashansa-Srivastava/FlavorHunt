package com.example.flavorhunt.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flavorhunt.MainActivity2;
import com.example.flavorhunt.Model.User;
import com.example.flavorhunt.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    EditText inputName,inputEmail,inputpassword,conformPassword;
    Button registerNow, loginRegister;
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    FirebaseAuth auth;
    String emailPattern = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        inputName = findViewById(R.id.userName);
        inputEmail = findViewById(R.id.userid);
        inputpassword = findViewById(R.id.password);
        conformPassword = findViewById(R.id.conPass);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Creating your account");
        progressDialog.setTitle("Your account is creating ");



        loginRegister = findViewById(R.id.login_pg);
        loginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });

        progressDialog.dismiss();
        registerNow = findViewById(R.id.registerNow);
        registerNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = inputName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputpassword.getText().toString().trim();
                String confirmPassword = conformPassword.getText().toString().trim();

                //Validate user input
                if (TextUtils.isEmpty(userName)) {
                    inputName.setError("Please enter your username");
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    inputEmail.setError("Please enter email address");
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    inputEmail.setError("Please enter a valid email address");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    inputpassword.setError("Please enter the password");
                    return;
                }
                if (TextUtils.isEmpty(confirmPassword)) {
                    conformPassword.setError("Please enter your password");
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    conformPassword.setError("Password do not match");
                    return;
                }
                // Create a new user

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if(task.isSuccessful()){
                                    String userId = auth.getCurrentUser().getUid();
                                    User user = new User(userName,email,password);
                                    database.getReference().child("Users").child(userId).setValue(user);

                                    Intent intent = new Intent(RegisterActivity.this,MainActivity2.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    // If registration fails, display a message to the user.
                                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
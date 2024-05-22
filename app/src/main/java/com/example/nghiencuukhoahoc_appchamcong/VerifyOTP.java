package com.example.nghiencuukhoahoc_appchamcong;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyOTP extends AppCompatActivity {
    public static final String TAG = VerifyOTP.class.getName();
    private EditText edt_OTP;
    private Button btn_XacNhanOTP;
    private TextView txt_GuiLaiOTP;
    private FirebaseAuth mAuth;
    String mPhoneNumber, mVerifition;
    private PhoneAuthProvider.ForceResendingToken mForceResendingToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_otp_layout);
        mAuth = FirebaseAuth.getInstance();
        setTitleToolbar();
        initui();
        GetDataIntent();

        btn_XacNhanOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String OTP = edt_OTP.getText().toString().trim();
                XacNhanMaOTP(OTP);
            }
        });
        txt_GuiLaiOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GuiLaiMaOTP();
            }
        });
    }
    private void initui()
    {
        edt_OTP = findViewById(R.id.edt_OTP);
        btn_XacNhanOTP = findViewById(R.id.btn_XacNhanOTP);
        txt_GuiLaiOTP = findViewById(R.id.txt_GuiLaiOTP);
    }
    private void XacNhanMaOTP(String OTP)
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerifition, OTP);
        signInWithPhoneAuthCredential(credential);
    }
    private void GuiLaiMaOTP()
    {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(mPhoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)
                        .setForceResendingToken(mForceResendingToken)// (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {  // xử lý thành công
                                signInWithPhoneAuthCredential(phoneAuthCredential);
                            }
                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) { // xử lý thất bại
                                Toast.makeText(VerifyOTP.this, "Gửi thất bại", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verifiationID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) { // Nhận mã OTP
                                super.onCodeSent(verifiationID, forceResendingToken);
                                mVerifition = verifiationID;
                                mForceResendingToken = forceResendingToken;
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private void GetDataIntent()
    {
        mPhoneNumber = getIntent().getStringExtra("Phone_Number");
        mVerifition = getIntent().getStringExtra("Verifiation_Id");
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // Update UI
                            goMainActivity(user.getPhoneNumber());
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(VerifyOTP.this, "Mã xác minh đã nhập không hợp lệ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
    private void setTitleToolbar()
    {
        if(getSupportActionBar()!= null)
        {
            getSupportActionBar().setTitle("VerifyOTP");
        }
    }
    private void goMainActivity(String phoneNumber) {
        Intent intent = new Intent(VerifyOTP.this, HomeUserActivity.class);
        intent.putExtra("Phone_Number",phoneNumber);
        startActivity(intent);
    }
}

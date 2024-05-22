package com.example.nghiencuukhoahoc_appchamcong;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ForgotPassword extends AppCompatActivity {
    public static final String TAG = ForgotPassword.class.getName();
    private EditText edt_SdtQuenMatKhau;
    private Button btn_XacNhanSDT;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_layout);
        setTitleToolbar();
        initui();
        mAuth = FirebaseAuth.getInstance();

        btn_XacNhanSDT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sdt = edt_SdtQuenMatKhau.getText().toString().trim();
                XacMinhSDT(sdt);
            }
        });
    }
    private void XacMinhSDT(String sdt)
    {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(sdt)
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {  // xử lý thành công
                                signInWithPhoneAuthCredential(phoneAuthCredential);
                            }
                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) { // xử lý thất bại
                                Log.e(TAG, "onVerificationFailed: " + e.getMessage());
                                Toast.makeText(ForgotPassword.this, "Gửi thất bại", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verifiationID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) { // Nhận mã OTP
                                super.onCodeSent(verifiationID, forceResendingToken);
                                gotoVerifyOTP(sdt,verifiationID);
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void gotoVerifyOTP(String sdt, String verifiationID) {
        Intent intent = new Intent(ForgotPassword.this, VerifyOTP.class);
        intent.putExtra("Phone_Number",sdt);
        intent.putExtra("Verifiation_Id",verifiationID);
        startActivity(intent);
    }
    private void goMainActivity(String phoneNumber) {
        Intent intent = new Intent(ForgotPassword.this, HomeUserActivity.class);
        intent.putExtra("Phone_Number",phoneNumber);
        startActivity(intent);
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
                                Toast.makeText(ForgotPassword.this, "Mã xác minh đã nhập không hợp lệ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void setTitleToolbar()
    {
        if(getSupportActionBar()!= null)
        {
            getSupportActionBar().setTitle("ForgotPassword");
        }
    }
    private void initui()
    {
        edt_SdtQuenMatKhau = findViewById(R.id.edt_SdtQuenMatKhau);
        btn_XacNhanSDT = findViewById(R.id.btn_XacNhanSDT);

    }

}

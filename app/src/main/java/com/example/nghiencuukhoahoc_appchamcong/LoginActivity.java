package com.example.nghiencuukhoahoc_appchamcong;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private TextView txt_Quenmatkhau;
    private EditText edtEmail, edtMatKhau;
    private Button btn_DangNhap;
    String User_Id,User_Name,Position_Name,User_Img;
    int Id_Position;
    ImageButton btnShowPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        txt_Quenmatkhau = findViewById(R.id.txt_Quenmatkhau);
        edtEmail = findViewById(R.id.edtEmail);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        btn_DangNhap = findViewById(R.id.btn_DangNhap);
        btnShowPassword = findViewById(R.id.btnShowPassword);

        txt_Quenmatkhau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });
        btn_DangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnClickCheckLogin();
            }
        });
        btnShowPassword.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
                if (!isPasswordVisible) {
                    // Nếu mật khẩu không được hiển thị, hiển thị mật khẩu
                    edtMatKhau.setTransformationMethod(null);
                    isPasswordVisible = true;
                    btnShowPassword.setImageResource(R.drawable.baseline_vpn_key_24);
                } else {
                    // Nếu mật khẩu đang được hiển thị, ẩn mật khẩu
                    edtMatKhau.setTransformationMethod(new PasswordTransformationMethod());
                    isPasswordVisible = false;
                    btnShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24);
                }

                // Di chuyển con trỏ của EditText về cuối chuỗi mật khẩu để tránh việc con trỏ quay về đầu khi thay đổi kiểu hiển thị
                edtMatKhau.setSelection(edtMatKhau.getText().length());
            }

            // Xử lý sự kiện khi nhấn nút cập nhật
        });
    }
    private void OnClickCheckLogin()
    {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        String enteredAccount = edtEmail.getText().toString();
        String enteredPassword = edtMatKhau.getText().toString();

        usersRef.orderByChild("account").equalTo(enteredAccount).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isAuthenticated = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String passwordFromDB = snapshot.child("password").getValue(String.class);
                    if (passwordFromDB != null && passwordFromDB.equals(enteredPassword)) {
                        isAuthenticated = true;
                        User_Id = snapshot.getKey();
                        User_Name = snapshot.child("name").getValue(String.class);
                        Position_Name = snapshot.child("position/name_Position").getValue(String.class);
                        Id_Position = snapshot.child("position/id_Position").getValue(Integer.class);
                        User_Img = snapshot.child("img").getValue(String.class);

                        break;
                    }
                }

                if (isAuthenticated) {
                    if(Id_Position == 1)
                    {
                        Intent intent = new Intent(LoginActivity.this,HomeAdminActivity.class);
                        intent.putExtra("User_Id", User_Id);
                        intent.putExtra("User_Name",User_Name);
                        intent.putExtra("Position_Name", Position_Name);
                        intent.putExtra("User_Img",User_Img);
                        startActivity(intent);
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Intent intent = new Intent(LoginActivity.this,HomeUserActivity.class);
                        intent.putExtra("User_Id", User_Id);
                        intent.putExtra("User_Name",User_Name);
                        intent.putExtra("Position_Name", Position_Name);
                        intent.putExtra("User_Img",User_Img);
                        HashMap<String, Object> object = new HashMap<>();
                        object.put("QRCodeIMG",generateQRCodetoBase64(User_Id));
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference databaseReference = database.getReference("Users");
                        databaseReference.child(User_Id).updateChildren(object, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                                    }
                                });
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                    }


                } else {
                    Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra trong quá trình đọc dữ liệu từ Firebase Realtime Database
            }
        });

    }
    private String generateQRCodetoBase64(String employeeId) {
        // Sử dụng ZXing để tạo mã QR code
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(employeeId, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            return convert(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String convert(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

}

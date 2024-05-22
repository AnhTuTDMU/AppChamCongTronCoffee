package com.example.nghiencuukhoahoc_appchamcong;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import DTO.Users;
import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateUserActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_IMAGE = 1;
    EditText edtTaiKhoan, edtMatKhau, edtNgaySinh, edtSoDienThoai, edtEmail;
    RadioButton radioNam, radioNu;
    long ngaySinhLong;
    Button btn_CapNhat, btn_ChonAnh;
    CircleImageView img_ChonAnh;
    DatabaseReference userRef;
    String userId;
    ImageButton btnShowPassword;
    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_update_layout);
        initUI();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("User_IdHome");
            getUserInfo(userId);
        }

        btn_ChonAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_IMAGE);
            }
        });

        btnShowPassword.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
                edtMatKhau.setTransformationMethod(isPasswordVisible ? new PasswordTransformationMethod() : null);
                isPasswordVisible = !isPasswordVisible;
                btnShowPassword.setImageResource(isPasswordVisible ? R.drawable.baseline_remove_red_eye_24 : R.drawable.baseline_vpn_key_24);
                edtMatKhau.setSelection(edtMatKhau.getText().length());
            }
        });

        btn_CapNhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserInfo();
            }
        });
    }

    private void updateUserInfo() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(userId));
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user != null) {
                        if (imageUrl != null) {
                            user.setImg(imageUrl);
                        }
                        user.setAccount(edtTaiKhoan.getText().toString().trim());
                        user.setPassword(edtMatKhau.getText().toString().trim());

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        try {
                            Date date = sdf.parse(edtNgaySinh.getText().toString().trim());
                            if (date != null) {
                                ngaySinhLong = date.getTime();
                                user.setDateOfBirth(ngaySinhLong);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String gioiTinh = radioNam.isChecked() ? "Nam" : "Nữ";
                        user.setSex(gioiTinh);
                        user.setPhoneNumber(edtSoDienThoai.getText().toString().trim());
                        user.setEmail(edtEmail.getText().toString().trim());

                        userRef.setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(UpdateUserActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(UpdateUserActivity.this, "Cập nhật thông tin thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            img_ChonAnh.setImageURI(selectedImageUri);
            uploadImageToFirebase(selectedImageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // Kiểm tra nếu imageUri không tồn tại (người dùng không chọn ảnh mới), không tải lên ảnh lên Firebase
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());

            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            imageUrl = downloadUri.toString();
                            userRef.child("img").setValue(imageUrl);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Xử lý khi tải ảnh lên Firebase thất bại
                }
            });
        }
    }

    private void getUserInfo(String userId) {
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(userId));
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user != null) {
                        edtTaiKhoan.setText(user.getAccount());
                        edtMatKhau.setText(user.getPassword());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String ngaySinhFormatted = sdf.format(new Date(user.getDateOfBirth()));
                        edtNgaySinh.setText(ngaySinhFormatted);
                        edtSoDienThoai.setText(user.getPhoneNumber());
                        edtEmail.setText(user.getEmail());
                        if (user.getSex().equals("Nam")) {
                            radioNam.setChecked(true);
                        } else {
                            radioNu.setChecked(true);
                        }
                        if (user.getImg() != null) {
                            Picasso.get().load(user.getImg()).into(img_ChonAnh);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    private void initUI() {
        img_ChonAnh = findViewById(R.id.img_ChonAnh);
        edtTaiKhoan = findViewById(R.id.edtTaiKhoan);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        edtNgaySinh = findViewById(R.id.edtNgaySinh);
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai);
        edtEmail = findViewById(R.id.edtEmail);
        radioNam = findViewById(R.id.radioNam);
        radioNu = findViewById(R.id.radioNu);
        btn_CapNhat = findViewById(R.id.btn_Sua);
        btn_ChonAnh = findViewById(R.id.btn_ChonAnh);
        btnShowPassword = findViewById(R.id.btnShowPassword);
    }
}

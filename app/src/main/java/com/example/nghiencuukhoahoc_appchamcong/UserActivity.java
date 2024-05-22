package com.example.nghiencuukhoahoc_appchamcong;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import Adapter.PositionAdapter;
import DTO.Position;
import DTO.Users;

public class UserActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_IMAGE = 1;

    EditText edtTenNhanVien, edtTaiKhoan, edtMatKhau, edtNgaySinh, edtSoDienThoai, edtEmail, edtLuongCoBan;
    Spinner sp_MaChucVu;
    RadioButton radioNam, radioNu;
    Button btn_LuuNhanVien, btn_ChonAnh;
    ImageView img_ChonAnh;
    PositionAdapter positionAdapter;
    int positionId;
    String imageUrl;
    String selectedPositionName;
    String Name,Account,Password,DateOfBirth,PhoneNumber,Email,SalaryText;
    long timestamp;
    Double Salary;
    String sex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_layout);

        initui();
        loadPositions();
        btn_ChonAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở hộp thoại để chọn ảnh từ thư viện hoặc máy ảnh
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_IMAGE);
            }
        });
        sp_MaChucVu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            DatabaseReference positionRef = FirebaseDatabase.getInstance().getReference("Position");
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPositionName = positionAdapter.getItem(sp_MaChucVu.getSelectedItemPosition()).getName_Position();
                positionRef.orderByChild("name_Position").equalTo(selectedPositionName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            positionId = Integer.parseInt(snapshot.getKey());

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Xử lý khi có lỗi xảy ra
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Xử lý khi không có gì được chọn
            }
        });
        btn_LuuNhanVien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickPushData();
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
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());

        UploadTask uploadTask = imageRef.putFile(imageUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Xử lý khi tải ảnh lên thành công
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        // Lấy URL của ảnh sau khi đã tải lên thành công
                        imageUrl = downloadUri.toString();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Xử lý khi tải ảnh lên thất bại
            }
        });
    }

    private void OnClickPushData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

         Name = edtTenNhanVien.getText().toString().trim();
         Account = edtTaiKhoan.getText().toString().trim();
         Password = edtMatKhau.getText().toString().trim();
         DateOfBirth = edtNgaySinh.getText().toString().trim();
         PhoneNumber = edtSoDienThoai.getText().toString().trim();
         Email = edtEmail.getText().toString().trim();
         SalaryText = edtLuongCoBan.getText().toString().trim();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date;
        try {
            date = format.parse(DateOfBirth);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        if (TextUtils.isEmpty(Name) || TextUtils.isEmpty(Account) || TextUtils.isEmpty(Password) ||
                TextUtils.isEmpty(DateOfBirth) || TextUtils.isEmpty(PhoneNumber) || TextUtils.isEmpty(Email) ||
                TextUtils.isEmpty(SalaryText)) {
            // Hiển thị thông báo hoặc xử lý theo ý của bạn
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi thành timestamp (milliseconds)
         timestamp = date.getTime();

        if (radioNam.isChecked()) {
            sex = "Nam";
        } else if (radioNu.isChecked()) {
            sex = "Nữ";
        } else {
            Toast.makeText(UserActivity.this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidEmail(Email)) {
            // Địa chỉ email không hợp lệ
            Toast.makeText(this, "Địa chỉ email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isValidPhoneNumber(PhoneNumber))
        {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Salary = Double.valueOf(SalaryText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Lương cơ bản không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        userRef.orderByChild("phoneNumber").equalTo(PhoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Số điện thoại đã tồn tại trong Firebase
                    Toast.makeText(UserActivity.this, "Số điện thoại đã tồn tại", Toast.LENGTH_SHORT).show();
                } else {
                    // Kiểm tra trùng lặp email
                    userRef.orderByChild("email").equalTo(Email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Email đã tồn tại trong Firebase
                                Toast.makeText(UserActivity.this, "Email đã tồn tại", Toast.LENGTH_SHORT).show();
                            } else {
                                // Số điện thoại và email không trùng lặp, tiến hành thêm dữ liệu
                                addNewUserData();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Xử lý lỗi nếu có
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });

    }
    private void addNewUserData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                long maxId = 0;
                // Tìm khóa lớn nhất trong danh sách
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    long key = Long.parseLong(snapshot.getKey());
                    if (key > maxId) {
                        maxId = key;
                    }
                }
                long newId = maxId + 1;
                Position userPosition = new Position(positionId, selectedPositionName);
                Users newUser = new Users((int)newId,Name,imageUrl,Account,Password,timestamp,sex,PhoneNumber,Email,Salary,userPosition);
                userRef.child(String.valueOf(newId)).setValue(newUser, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @androidx.annotation.NonNull DatabaseReference ref) {
                        if (error == null) {
                            Toast.makeText(UserActivity.this, "Đã thêm nhân viên thành công", Toast.LENGTH_SHORT).show();
                            reset();
                        } else {
                            Toast.makeText(UserActivity.this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });

    }
    private void loadPositions() {
        DatabaseReference positionRef = FirebaseDatabase.getInstance().getReference("Position");
        positionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Position> positions = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Position position = snapshot.getValue(Position.class);
                    if (position != null) {
                        positions.add(position);
                    }
                }

                positionAdapter = new PositionAdapter(UserActivity.this, R.layout.item_name_position, positions);
                sp_MaChucVu.setAdapter(positionAdapter); // Sử dụng positionAdapter cho Spinner
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isValidPhoneNumber(String phoneNumber) {
        String phonePattern = "^[0-9]{10}$";

        return phoneNumber.matches(phonePattern);
    }

    private void initui()
    {
        edtTenNhanVien = findViewById(R.id.edtTenNhanVien);
        edtTaiKhoan = findViewById(R.id.edtTaiKhoan);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        edtNgaySinh = findViewById(R.id.edtNgaySinh);
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai);
        edtEmail = findViewById(R.id.edtEmail);
        edtLuongCoBan = findViewById(R.id.edtLuongCoBan);
        sp_MaChucVu = findViewById(R.id.sp_MaChucVu);
        radioNam = findViewById(R.id.radioNam);
        radioNu = findViewById(R.id.radioNu);
        btn_LuuNhanVien = findViewById(R.id.btn_LuuNhanVien);
        btn_ChonAnh = findViewById(R.id.btn_ChonAnh);
        img_ChonAnh = findViewById(R.id.img_ChonAnh);
    }
    private void reset()
    {
        edtTenNhanVien.setText("");
        edtTaiKhoan.setText("");
        edtMatKhau.setText("");
        edtNgaySinh.setText("");
        if (radioNam.isChecked()) {
            radioNam.setChecked(false);
        } else if (radioNu.isChecked()) {
            radioNu.setChecked(false);
        }
        edtSoDienThoai.setText("");
        edtEmail.setText("");
        edtLuongCoBan.setText("");
        img_ChonAnh.setImageResource(0);
    }
}

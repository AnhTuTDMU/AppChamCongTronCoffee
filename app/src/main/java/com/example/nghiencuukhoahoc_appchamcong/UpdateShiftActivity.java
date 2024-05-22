package com.example.nghiencuukhoahoc_appchamcong;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
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

import java.util.Locale;

import DTO.Shift;

public class UpdateShiftActivity extends AppCompatActivity {
    EditText edt_TenCaLam;
    Button btn_UpdateNameShift;
    Button TP_GioBatDau, TP_GioKetThuc;
    int shiftid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shift_update_layout);
        initui();
        Intent intent = getIntent();
        if(intent != null) {
            shiftid = intent.getIntExtra("Shift_Id", 0);
            String shiftname = intent.getStringExtra("Shift_Name");
            edt_TenCaLam.setText(shiftname);

            long startTime = Long.parseLong(intent.getStringExtra("Shift_TimeStart"));
            String formattedStartTime = formatTime(startTime);
            TP_GioBatDau.setText(formattedStartTime);

            long endTime = Long.parseLong(intent.getStringExtra("Shift_TimeEnd"));
            String formattedEndTime = formatTime(endTime);
            TP_GioKetThuc.setText(formattedEndTime);
        }
        btn_UpdateNameShift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateShiftData();
            }
        });
        TP_GioBatDau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(TP_GioBatDau);
            }
        });
        TP_GioKetThuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(TP_GioKetThuc);
            }
        });
    }

    private void updateShiftData() {
        DatabaseReference shiftRef = FirebaseDatabase.getInstance().getReference("Shift");

        String newName = edt_TenCaLam.getText().toString().trim();
        String startTime = TP_GioBatDau.getText().toString();
        String endTime = TP_GioKetThuc.getText().toString();

        shiftRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long maxId = 0;
                // Tìm khóa lớn nhất trong danh sách
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    long key = Long.parseLong(snapshot.getKey());
                    if (key > maxId) {
                        maxId = key;
                    }
                }

                boolean isDuplicate = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Shift shift = snapshot.getValue(Shift.class);
                    if (shift != null && shift.getName_Shift().equalsIgnoreCase(newName)) {
                        isDuplicate = true;
                        break;
                    }
                }

                long startMillis = convertTimeToMillis(startTime);
                long endMillis = convertTimeToMillis(endTime);

                 if (isTimeOverlap(startMillis, endMillis, dataSnapshot)) {
                    // Thông báo khi thời gian mới trùng với một ca làm đã có trong cơ sở dữ liệu
                    Toast.makeText(UpdateShiftActivity.this, "Thời gian đã bị trùng lặp với một ca làm khác", Toast.LENGTH_SHORT).show();
                } else {
                    Shift updatedShift = new Shift(shiftid, newName, startMillis, endMillis);

                    shiftRef.child(String.valueOf(shiftid)).setValue(updatedShift)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Cập nhật dữ liệu thành công, có thể cập nhật giao diện hoặc thông báo
                                    Toast.makeText(UpdateShiftActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xử lý nếu cập nhật dữ liệu không thành công
                                    Toast.makeText(UpdateShiftActivity.this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    private void showTimePicker(final Button button) {
        int style = AlertDialog.THEME_HOLO_DARK;
        // Hiển thị TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(UpdateShiftActivity.this,style,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Xử lý khi người dùng chọn giờ
                        String selectedTime = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute);
                        button.setText(selectedTime); // Đặt giờ đã chọn vào button
                    }
                },
                0, 0, true);
        timePickerDialog.show();
    }
    private long convertTimeToMillis(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        // Chuyển đổi giờ và phút sang milliseconds
        return (hours * 60 * 60 * 1000) + (minutes * 60 * 1000);
    }
    private boolean isTimeOverlap(long newStartTime, long newEndTime, DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Shift shift = snapshot.getValue(Shift.class);
            if (shift != null) {
                long existingStartTime = shift.getTime_Start();
                long existingEndTime = shift.getTime_End();

                // Kiểm tra xem thời gian mới có trùng với bất kỳ ca làm nào trong cơ sở dữ liệu không
                if (!((newStartTime >= existingEndTime) || (newEndTime <= existingStartTime))) {
                    // Thời gian mới trùng lặp với một ca làm đã tồn tại
                    return true;
                }
            }
        }
        return false;
    }
    private String formatTime(long millis) {
        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis / (1000 * 60)) % 60);

        // Định dạng chuỗi HH:mm
        String formattedTime = String.format("%02d:%02d", hours, minutes);
        return formattedTime;
    }

    private void initui()
    {
        btn_UpdateNameShift = findViewById(R.id.btn_UpdateNameShift);
        edt_TenCaLam = findViewById(R.id.edt_TenLamViec);
        TP_GioBatDau = findViewById(R.id.TP_GioBatDau);
        TP_GioKetThuc = findViewById(R.id.TP_GioKetThuc);
    }
}

package com.example.nghiencuukhoahoc_appchamcong;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import DTO.Position;
import DTO.Shift;

public class ShiftActivity extends AppCompatActivity {
    EditText edt_TenCaLam;
    Button btn_ThemCaLamViec;
    Button TP_GioBatDau, TP_GioKetThuc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shift_layout);
        initui();

        btn_ThemCaLamViec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnClickPushData();
            }
        });
        TP_GioBatDau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị TimePicker khi người dùng nhấn vào button TP_GioBatDau
                showTimePicker(TP_GioBatDau);
            }
        });

        TP_GioKetThuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị TimePicker khi người dùng nhấn vào button TP_GioKetThuc
                showTimePicker(TP_GioKetThuc);
            }
        });

    }

    private void OnClickPushData() {
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
                        // Tìm thấy tên trùng lặp trong cơ sở dữ liệu
                        isDuplicate = true;
                        break;
                    }
                }
                long startMillis = convertTimeToMillis(startTime);
                long endMillis = convertTimeToMillis(endTime);
                if (isDuplicate) {
                    // Thông báo khi tên mới trùng với tên đã tồn tại
                    Toast.makeText(ShiftActivity.this, "Ca làm đã tồn tại", Toast.LENGTH_SHORT).show();

                } else if (isTimeOverlap(startMillis, endMillis, dataSnapshot)) {
                    // Thông báo khi thời gian mới trùng với một ca làm đã có trong cơ sở dữ liệu
                    Toast.makeText(ShiftActivity.this, "Thời gian đã bị trùng lặp với một ca làm khác", Toast.LENGTH_SHORT).show();
                }else {
                    long newId = maxId + 1;

                    Shift newShift = new Shift((int) newId, newName,startMillis,endMillis);

                    // Thêm Shift mới vào Firebase với khóa là số tăng dần
                    shiftRef.child(String.valueOf(newId)).setValue(newShift, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                Toast.makeText(ShiftActivity.this, "Thêm ca làm việc thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ShiftActivity.this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                            }
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
        TimePickerDialog timePickerDialog = new TimePickerDialog(ShiftActivity.this,style,
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

    private void initui()
    {
        btn_ThemCaLamViec = findViewById(R.id.btn_ThemCaLamViec);
        edt_TenCaLam = findViewById(R.id.edt_TenLamViec);
        TP_GioBatDau = findViewById(R.id.TP_GioBatDau);
        TP_GioKetThuc = findViewById(R.id.TP_GioKetThuc);
    }
}

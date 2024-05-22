package com.example.nghiencuukhoahoc_appchamcong;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Adapter.TimekeepingAdapter;
import DTO.Salary;
import DTO.TimeKeeping;

public class SalaryActivity extends AppCompatActivity {
    DatabaseReference timekeepingRef;
    DatabaseReference usersRef;
    Spinner spinnerThang, spinnerNam;
    TextView txt_NgayCong, txt_LuongThucTe,txt_TongGioLamViec;
    Button btn_XacNhan;
    ArrayAdapter<String> yearAdapter;
    int MaNV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.salary_layout);


        Intent intent = getIntent();
        if (intent != null) {
            MaNV = Integer.parseInt(intent.getStringExtra("User_IdHome"));
        }

        txt_NgayCong = findViewById(R.id.txt_NgayCong);
        txt_LuongThucTe = findViewById(R.id.txt_LuongThucTe);
        btn_XacNhan = findViewById(R.id.btn_XacNhan);
        spinnerThang = findViewById(R.id.spinner_Thang);
        spinnerNam = findViewById(R.id.spinner_Nam);

        timekeepingRef = FirebaseDatabase.getInstance().getReference("TimeKeeping");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this, R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerThang.setAdapter(monthAdapter);

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        spinnerThang.setSelection(currentMonth);

        spinnerNam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedYear = yearAdapter.getItem(i);
                int selectedMonth = spinnerThang.getSelectedItemPosition() + 1;
                int year = Integer.parseInt(selectedYear);
                calculateSalary(year, selectedMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerThang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (yearAdapter != null && spinnerThang != null && spinnerThang.getSelectedItem() != null) {
                    int selectedYear = Integer.parseInt(spinnerNam.getSelectedItem().toString());
                    int selectedMonth = i + 1;
                    calculateSalary(selectedYear, selectedMonth);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        btn_XacNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedMonth = spinnerThang.getSelectedItemPosition() + 1;
                String selectedYear = spinnerNam.getSelectedItem().toString();

                int ngaycong = Integer.parseInt(txt_NgayCong.getText().toString());
                double luongThucTe = Double.parseDouble(txt_LuongThucTe.getText().toString().replaceAll("[^\\d]", ""));

                DatabaseReference salaryRef = FirebaseDatabase.getInstance().getReference("salary");

                String salaryId = MaNV + "_" + selectedMonth + "_" + selectedYear;

                Salary salary = new Salary();
                salary.setMaNV_Thang_Nam(MaNV + "_" + selectedMonth + "_" + selectedYear);
                salary.setMaNV(MaNV);
                salary.setThang(selectedMonth);
                salary.setNam(Integer.parseInt(selectedYear));
                salary.setNgayCong(ngaycong);
                salary.setLuongThucTe(luongThucTe);

                // Thêm hoặc cập nhật bản ghi lương trong Firebase
                salaryRef.child(salaryId).setValue(salary)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(SalaryActivity.this, "Lưu dữ liệu lương thành công", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SalaryActivity.this, "Lưu dữ liệu lương thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


        loadYearData();

    }

    private void loadYearData() {
        timekeepingRef.orderByChild("status").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> years = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TimeKeeping timekeeping = snapshot.getValue(TimeKeeping.class);
                    if (timekeeping != null) {
                        String year = String.valueOf(getYearFromDate(timekeeping.getDate()));
                        if (!years.contains(year)) {
                            years.add(year);
                        }
                    }
                }
                yearAdapter = new ArrayAdapter<>(SalaryActivity.this, android.R.layout.simple_spinner_item, years);
                yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerNam.setAdapter(yearAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SalaryActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getYearFromDate(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.YEAR);
    }

    private void calculateSalary(int year, int month) {
        Query query = timekeepingRef.orderByChild("date").startAt(getStartTimeOfMonth(year, month)).endAt(getEndTimeOfMonth(year, month));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int NgayCong = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TimeKeeping timekeeping = snapshot.getValue(TimeKeeping.class);
                    if (timekeeping != null && timekeeping.getId_User() == MaNV) {
                        NgayCong++;
                    }
                }

                // Lấy mức lương cơ bản từ dữ liệu người dùng
                double LuongCoBan = 3000000;

                // Tính tiền lương thực tế
                double LuongThucTe = (LuongCoBan / 26) * NgayCong;
                txt_NgayCong.setText(String.valueOf(NgayCong));
                // Hiển thị tiền lương thực tế
                String formattedSalary = String.format(Locale.getDefault(), "%,.0f VNĐ", LuongThucTe);
                txt_LuongThucTe.setText(formattedSalary);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SalaryActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private long getStartTimeOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndTimeOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
}

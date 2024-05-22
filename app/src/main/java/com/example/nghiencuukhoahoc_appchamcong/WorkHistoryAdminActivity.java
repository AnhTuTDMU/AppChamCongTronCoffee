package com.example.nghiencuukhoahoc_appchamcong;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import Adapter.TimekeepingAdapter;
import DTO.TimeKeeping;
import DTO.Users;

public class WorkHistoryAdminActivity extends AppCompatActivity {
    ListView lv_ThongTinChamCong;
    TextView TongSoGioLamTrongThang;
    private ArrayAdapter<String> yearAdapter;
    private ArrayList<TimeKeeping> TimeKeepingList = new ArrayList<>();
    private ArrayList<Users> userList = new ArrayList<>();
    int userId;
    Spinner spinnerThang, spinnerNam,spinnerNhanVien;
    TimekeepingAdapter timekeepingAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_history_admin_layout);
        initui();

        timekeepingAdapter = new TimekeepingAdapter(this, R.layout.item_timekeeping, new ArrayList<>());
        lv_ThongTinChamCong.setAdapter(timekeepingAdapter);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this, R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerThang.setAdapter(monthAdapter);
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);

        yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timekeepingAdapter.getYearData());
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNam.setAdapter(yearAdapter);

        spinnerThang.setSelection(currentMonth);
        getDataUser();
        spinnerNhanVien.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedEmployeeName = spinnerNhanVien.getSelectedItem().toString();
                userId = getUserIdFromEmployeeName(selectedEmployeeName);
                int selectedMonth = spinnerThang.getSelectedItemPosition() + 1;
                String selectedYear = yearAdapter.getItem(spinnerNam.getSelectedItemPosition());
                getDataForEmployeeWithStatusAndMonthYear(userId, selectedMonth, Integer.parseInt(selectedYear));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Xử lý khi không có gì được chọn
            }
        });
        spinnerThang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int selectedMonth = position + 1;
                String selectedYear = yearAdapter.getItem(spinnerNam.getSelectedItemPosition());
                getDataForEmployeeWithStatusAndMonthYear(userId, selectedMonth, Integer.parseInt(selectedYear));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Xử lý khi không có tháng nào được chọn
            }
        });
        spinnerNam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedYear = yearAdapter.getItem(i);

                int selectedMonth = spinnerThang.getSelectedItemPosition() + 1;
                getDataForEmployeeWithStatusAndMonthYear(userId, selectedMonth,Integer.parseInt(selectedYear));

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    private void initui()
    {
        lv_ThongTinChamCong = findViewById(R.id.lv_ThongTinChamCong);
        spinnerThang = findViewById(R.id.spinner_Thang);
        spinnerNam = findViewById(R.id.spinner_Nam);
        spinnerNhanVien = findViewById(R.id.sp_TenNhanVien);
        TongSoGioLamTrongThang = findViewById(R.id.TongSoGioLamTrongThang);

    }
    private void getDataForEmployeeWithStatusAndMonthYear(int employeeId, int selectedMonth, int selectedYear) {
        DatabaseReference TimeKeepingRef = FirebaseDatabase.getInstance().getReference("TimeKeeping");

        Query query = TimeKeepingRef.orderByChild("id_User").equalTo(employeeId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TimeKeepingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TimeKeeping timeKeeping = snapshot.getValue(TimeKeeping.class);
                    if (timeKeeping != null && timeKeeping.getStatus() == true) {
                        long firebaseDateInMillis = timeKeeping.getDate();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(firebaseDateInMillis);
                        int firebaseMonth = calendar.get(Calendar.MONTH) + 1;
                        int firebaseYear = calendar.get(Calendar.YEAR);

                        if (firebaseMonth == selectedMonth && !TimeKeepingList.contains(timeKeeping)&& firebaseYear == selectedYear) {
                            TimeKeepingList.add(timeKeeping);
                        }
                    }
                }

                timekeepingAdapter.updateData(TimeKeepingList);
                timekeepingAdapter.notifyDataSetChanged();
                String totalWorkTime = timekeepingAdapter.getTotalWorkTime();
                TongSoGioLamTrongThang.setText(Html.fromHtml(totalWorkTime));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }
    private void getDataUser() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.orderByChild("position/id_Position").equalTo(2).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> employeeNames = new ArrayList<>();
                userList.clear(); // Xóa dữ liệu cũ để cập nhật mới

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int userId = snapshot.child("id_User").getValue(Integer.class);
                    String employeeName = snapshot.child("name").getValue(String.class);

                    if (employeeName != null) {
                        employeeNames.add(employeeName);

                        // Thêm thông tin người dùng vào userList
                        Users user = new Users();
                        user.setId_User(userId);
                        user.setName(employeeName);
                        userList.add(user);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(WorkHistoryAdminActivity.this, android.R.layout.simple_spinner_item, employeeNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerNhanVien.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }
    private int getUserIdFromEmployeeName(String selectedEmployeeName) {
        for (Users uses : userList) {
            if (uses.getName().equals(selectedEmployeeName)) {
                return uses.getId_User();
            }
        }
        return -1;
    }

}

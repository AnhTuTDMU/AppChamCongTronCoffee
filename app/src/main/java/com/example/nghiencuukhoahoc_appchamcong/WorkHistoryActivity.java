package com.example.nghiencuukhoahoc_appchamcong;

import android.content.Intent;
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

public class WorkHistoryActivity extends AppCompatActivity {
    ListView lv_ThongTinChamCong;
    TextView TongSoGioLamTrongThang;
    private ArrayAdapter<String> yearAdapter;
    private ArrayList<TimeKeeping> TimeKeepingList = new ArrayList<>();
    int MaNV;
    Spinner spinnerThang, spinnerNam;
    TimekeepingAdapter timekeepingAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_history_user_layout);

        initui();
        Intent intent = getIntent();
        if (intent != null) {
            MaNV = Integer.parseInt(intent.getStringExtra("User_IdHome"));
        }
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
        spinnerThang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int selectedMonth = position + 1;
                String selectedYear = yearAdapter.getItem(spinnerNam.getSelectedItemPosition());
                getDataForEmployeeWithStatusAndMonthYear(MaNV, selectedMonth, Integer.parseInt(selectedYear));

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
                getDataForEmployeeWithStatusAndMonthYear(MaNV, selectedMonth,Integer.parseInt(selectedYear));

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
}

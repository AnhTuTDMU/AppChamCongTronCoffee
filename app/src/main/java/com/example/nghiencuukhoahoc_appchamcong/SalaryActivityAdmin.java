package com.example.nghiencuukhoahoc_appchamcong;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import Adapter.UserAdapter;
import Adapter.UserNameAdapter;
import DTO.Salary;
import DTO.TimeKeeping;
import DTO.Users;


public class SalaryActivityAdmin extends AppCompatActivity {
    DatabaseReference salaryRef;
    DatabaseReference usersRef;
    Spinner spinnerThang, spinnerNam, spinnerNhanVien;
    private ArrayAdapter<String> yearAdapter;
    TextView txt_NgayCong, txt_LuongThucTe;
    int MaNV, ngaycong;
    double LuongThucTe, LuongCoBan;
    boolean isNhanVienSelected = false;
    int selectedMonth ,selectedYear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.salary_layout_admin);

        txt_NgayCong = findViewById(R.id.txt_NgayCong);
        txt_LuongThucTe = findViewById(R.id.txt_LuongThucTe);

        salaryRef = FirebaseDatabase.getInstance().getReference("salary");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        spinnerNhanVien = findViewById(R.id.sp_TenNhanVien);

        // Lấy danh sách nhân viên từ Firebase và hiển thị trong Spinner
        usersRef.orderByChild("position/id_Position").equalTo(2).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Users> usersList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users user = snapshot.getValue(Users.class);
                    MaNV = user.getId_User();
                    if (user != null && user.getName() != null) {
                        usersList.add(user);
                    }
                }

                // Tạo adapter và đặt adapter cho spinner
                UserNameAdapter adapter = new UserNameAdapter(SalaryActivityAdmin.this, R.layout.item_name_user, usersList);
                spinnerNhanVien.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SalaryActivityAdmin.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });




        // Thiết lập Spinner tháng và năm
        spinnerThang = findViewById(R.id.spinner_Thang);
        spinnerNam = findViewById(R.id.spinner_Nam);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this, R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerThang.setAdapter(monthAdapter);
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        spinnerThang.setSelection(currentMonth);

        yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getYearData());
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNam.setAdapter(yearAdapter);

        spinnerNhanVien.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Users selectedUser = (Users) spinnerNhanVien.getSelectedItem();
                if (selectedUser != null) {
                    MaNV = selectedUser.getId_User();
                    spinnerThang.setSelection(currentMonth);
                    spinnerNam.setSelection(yearAdapter.getPosition(String.valueOf(currentYear)));
                    LuongCoBan = selectedUser.getSalary();
                    isNhanVienSelected = true;
                    getAllSalaries();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Xử lý khi không có mục nào được chọn.
            }
        });

        spinnerThang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedYear = Integer.parseInt(yearAdapter.getItem(spinnerNam.getSelectedItemPosition()));
                selectedMonth = i + 1;
                getAllSalaries(); // Gọi hàm để lấy dữ liệu lương
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerNam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedYear = yearAdapter.getItem(i);
                selectedMonth = spinnerThang.getSelectedItemPosition() + 1;
                getAllSalaries(); // Gọi hàm để lấy dữ liệu lương
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    private List<String> getYearData() {
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= 2010; i--) {
            years.add(String.valueOf(i));
        }
        return years;
    }

    private void getAllSalaries() {
        if (isNhanVienSelected) {
            DatabaseReference salaryRef = FirebaseDatabase.getInstance().getReference("salary");
            Query query = salaryRef.orderByChild("maNV_Thang_Nam").equalTo(MaNV + "_" + selectedMonth + "_" + selectedYear);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Salary salary = snapshot.getValue(Salary.class);
                        if (salary != null ) {
                            int ngaycong = salary.getNgayCong();
                            double luongthucte = salary.getLuongThucTe();

                            txt_NgayCong.setText(String.valueOf(ngaycong));
                            String formattedSalary = String.format(Locale.getDefault(), "%,.0f VNĐ", luongthucte);

                            txt_NgayCong.setText(String.valueOf(ngaycong));
                            txt_LuongThucTe.setText(formattedSalary);

                        }
                        else{
                            txt_NgayCong.setText("Không có dữ liệu");
                            txt_LuongThucTe.setText("Không có dữ liệu");
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Xử lý khi có lỗi xảy ra trong quá trình truy vấn dữ liệu
                    Log.e("FirebaseError", "Error getting data from Firebase: " + databaseError.getMessage());
                }
            });
        }
    }
}

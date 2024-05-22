package com.example.nghiencuukhoahoc_appchamcong;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
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

import java.util.ArrayList;

import Adapter.PositionAdapter;
import Adapter.UserAdapter;
import DTO.Position;
import DTO.Users;

public class ListUserActivity extends AppCompatActivity {
    ListView lv_ThongTinNhanVien;
    UserAdapter adapter;
    EditText editTextSearch;
    private ArrayList<Users> employeeList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_user_layout);

        lv_ThongTinNhanVien = findViewById(R.id.lv_ThongTinNhanVien);
        getData();

        registerForContextMenu(lv_ThongTinNhanVien);

        editTextSearch = findViewById(R.id.edt_search_name);

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchQuery = charSequence.toString();
                searchByName(searchQuery);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    private void getData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        userRef.orderByChild("position/id_Position").equalTo(2).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users user = snapshot.getValue(Users.class);
                    if (user != null) {
                        employeeList.add(user);
                    }
                }

                // Cập nhật dữ liệu cho adapter
                adapter = new UserAdapter(ListUserActivity.this, R.layout.item_user, employeeList);
                lv_ThongTinNhanVien.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }
    private void searchByName(String searchName) {
        ArrayList<Users> filteredList = new ArrayList<>();

        for (Users user : employeeList) {
            if (user.getName().toLowerCase().contains(searchName.toLowerCase())) {
                filteredList.add(user);
            }
        }

        // Cập nhật dữ liệu cho adapter sau khi tìm kiếm
        adapter = new UserAdapter(ListUserActivity.this, R.layout.item_user, filteredList);
        lv_ThongTinNhanVien.setAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_menu, menu);
        menu.setHeaderTitle("Nhân Viên");
        menu.setHeaderIcon(R.mipmap.ic_launcher_round);


        super.onCreateContextMenu(menu,v,menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.action_delete) {
            int id =(int) info.id;
            Users MaNhanVien = adapter.getItem(id);

            // Thực hiện xóa dữ liệu từ Firebase
            DatabaseReference positionRef = FirebaseDatabase.getInstance().getReference("Users");
            positionRef.child(String.valueOf(MaNhanVien.getId_User())).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Xóa dữ liệu thành công, có thể cập nhật giao diện hoặc thông báo
                            adapter.remove(MaNhanVien);
                            Toast.makeText(ListUserActivity.this, "Xóa thành công !", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Xử lý nếu xóa dữ liệu không thành công
                            Toast.makeText(ListUserActivity.this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }
}

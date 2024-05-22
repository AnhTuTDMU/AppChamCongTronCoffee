package com.example.nghiencuukhoahoc_appchamcong;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import Adapter.ShiftAdapter;
import Adapter.UserAdapter;
import DTO.Position;
import DTO.Shift;
import DTO.Users;


public class ListShiftActivity extends AppCompatActivity {
    ListView lv_ThongTinCaLam;
    ShiftAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_shift_layout);

        lv_ThongTinCaLam = findViewById(R.id.lv_ThongTinCaLam);
        getData();

        registerForContextMenu(lv_ThongTinCaLam);
    }
    private void getData() {
        DatabaseReference ShiftRef = FirebaseDatabase.getInstance().getReference("Shift");

        ShiftRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Shift> Shifts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Shift Shift = snapshot.getValue(Shift.class);
                    if (Shift != null) {
                        Shifts.add(Shift);
                    }
                }

                // Cập nhật dữ liệu cho adapter toàn cục
                adapter = new ShiftAdapter(ListShiftActivity.this, R.layout.item_shift, Shifts);
                lv_ThongTinCaLam.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_menu_shift, menu);
        menu.setHeaderTitle("Ca Làm Việc");
        menu.setHeaderIcon(R.mipmap.ic_launcher_round);


        super.onCreateContextMenu(menu,v,menuInfo);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.action_delete) {
            int id =(int) info.id;
            Shift MaCaLam = adapter.getItem(id);

            // Thực hiện xóa dữ liệu từ Firebase
            DatabaseReference ShiftRef = FirebaseDatabase.getInstance().getReference("Shift");
            ShiftRef.child(String.valueOf(MaCaLam.getId_Shift())).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Xóa dữ liệu thành công, có thể cập nhật giao diện hoặc thông báo
                            adapter.remove(MaCaLam);
                            Toast.makeText(ListShiftActivity.this, "Xóa thành công !", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Xử lý nếu xóa dữ liệu không thành công
                            Toast.makeText(ListShiftActivity.this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            return true;
        }
        if (item.getItemId() == R.id.action_update) {
            int id = (int) info.id;
            Shift selectedShift = adapter.getItem(id);

            // Tạo Intent để chuyển đến hoạt động UpdateShiftActivity
            Intent intent = new Intent(ListShiftActivity.this, UpdateShiftActivity.class);

            // Truyền dữ liệu Shift cần cập nhật qua Intent
            intent.putExtra("Shift_Id",selectedShift.getId_Shift());
            intent.putExtra("Shift_Name",selectedShift.getName_Shift());
            intent.putExtra("Shift_TimeStart", String.valueOf(selectedShift.getTime_Start()));
            intent.putExtra("Shift_TimeEnd", String.valueOf(selectedShift.getTime_End()));
            // Bắt đầu hoạt động mới để cập nhật dữ liệu
            startActivity(intent);

            return true;
        }
        else {
            return super.onContextItemSelected(item);
        }

    }
}

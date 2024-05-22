package com.example.nghiencuukhoahoc_appchamcong;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Adapter.TimekeepingAdapter;
import Adapter.UserAdapter;
import DTO.TimeKeeping;
import DTO.Users;

public class EmploymentConfirmationActivity extends AppCompatActivity {
    ListView lv_PhieuChamCong;
    TimekeepingAdapter adapter;
    private ArrayList<TimeKeeping> TimeKeepingLists = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employment_confirmation_layout);

        lv_PhieuChamCong = findViewById(R.id.lv_PhieuChamCong);
        getData();

        registerForContextMenu(lv_PhieuChamCong);
    }
    private void getData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("TimeKeeping");

        userRef.orderByChild("status").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TimeKeeping timeKeepings = snapshot.getValue(TimeKeeping.class);
                    if (timeKeepings != null) {
                        TimeKeepingLists.add(timeKeepings);
                    }
                }

                // Cập nhật dữ liệu cho adapter
                adapter = new TimekeepingAdapter(EmploymentConfirmationActivity.this, R.layout.item_timekeeping, TimeKeepingLists);
                lv_PhieuChamCong.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.timekeeping_menu, menu);
        menu.setHeaderTitle("Phiếu chấm công");
        menu.setHeaderIcon(R.mipmap.ic_launcher_round);
        super.onCreateContextMenu(menu,v,menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.action_success) {
            int ID = (int) info.id;
            TimeKeeping MaChamCong = adapter.getItem(ID);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xác nhận duyệt");
            builder.setMessage("Bạn có chắc chắn muốn duyệt ?");
            builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DatabaseReference positionRef = FirebaseDatabase.getInstance().getReference("TimeKeeping");

                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("status", true);

                    positionRef.child(String.valueOf(MaChamCong.getId_Timekeeping())).updateChildren(updateData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    MaChamCong.setStatus(true);
                                    adapter.remove(MaChamCong);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(EmploymentConfirmationActivity.this, "Duyệt phiếu thành công !", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xử lý nếu cập nhật không thành công
                                    Toast.makeText(EmploymentConfirmationActivity.this, "Duyệt phiếu thất bại, có lỗi xảy ra! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
            builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

}

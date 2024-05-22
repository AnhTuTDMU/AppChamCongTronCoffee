package com.example.nghiencuukhoahoc_appchamcong;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import java.util.ArrayList;

import Adapter.PositionAdapter;
import DTO.Position;

public class PositionActivity extends AppCompatActivity {
    private EditText edtTenChucVu;
    private Button btn_Luu;
    private ListView lv_ThongTinChucVu;
    public PositionAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.position_layout);

        initui();
        btn_Luu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnClickPushData();
            }
        });
        getData();

        registerForContextMenu(lv_ThongTinChucVu);
    }
    private void OnClickPushData() {
        DatabaseReference positionRef = FirebaseDatabase.getInstance().getReference("Position");

        String newName = edtTenChucVu.getText().toString().trim();

        positionRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    Position position = snapshot.getValue(Position.class);
                    if (position != null && position.getName_Position().equalsIgnoreCase(newName)) {
                        // Tìm thấy tên trùng lặp trong cơ sở dữ liệu
                        isDuplicate = true;
                        break;
                    }
                }

                if (isDuplicate) {
                    // Thông báo khi tên mới trùng với tên đã tồn tại
                    Toast.makeText(PositionActivity.this, "Tên chức vụ đã tồn tại", Toast.LENGTH_SHORT).show();
                } else {
                    long newId = maxId + 1;
                    Position newPosition = new Position((int) newId, newName);

                    // Thêm Position mới vào Firebase với khóa là số tăng dần
                    positionRef.child(String.valueOf(newId)).setValue(newPosition, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                Toast.makeText(PositionActivity.this, "Đã thêm chức vụ thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PositionActivity.this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
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
    private void getData() {
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

                // Cập nhật dữ liệu cho adapter toàn cục
                adapter = new PositionAdapter(PositionActivity.this, R.layout.item_name_position, positions);
                lv_ThongTinChucVu.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_menu, menu);
        menu.setHeaderTitle("Chức vụ");
        menu.setHeaderIcon(R.mipmap.ic_launcher_round);


        super.onCreateContextMenu(menu,v,menuInfo);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.action_delete) {
            int id =(int) info.id;
            Position MaChucVu = adapter.getItem(id);

            // Thực hiện xóa dữ liệu từ Firebase
            DatabaseReference positionRef = FirebaseDatabase.getInstance().getReference("Position");
            positionRef.child(String.valueOf(MaChucVu.getId_Position())).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Xóa dữ liệu thành công, có thể cập nhật giao diện hoặc thông báo
                            adapter.remove(MaChucVu);
                            Toast.makeText(PositionActivity.this, "Xóa thành công !", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Xử lý nếu xóa dữ liệu không thành công
                            Toast.makeText(PositionActivity.this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    private void initui()
    {
        edtTenChucVu = findViewById(R.id.edtTenChucVu);
        btn_Luu = findViewById(R.id.btn_Luu);
        lv_ThongTinChucVu = findViewById(R.id.lv_ThongTinChucVu);
    }
}

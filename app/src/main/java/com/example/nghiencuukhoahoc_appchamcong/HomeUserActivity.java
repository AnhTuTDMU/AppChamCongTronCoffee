package com.example.nghiencuukhoahoc_appchamcong;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.nghiencuukhoahoc_appchamcong.fcm.MyFirebaseMessagingService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import DTO.Shift;
import DTO.TimeKeeping;

public class HomeUserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    TextView txt_NameUser,txt_today;
    String userId;
    ImageView img_User,img_today;
    long idTimekeeping = 0;
    View headerView;
    CardView CardView_ChamCong,CardView_LichSuLamViec,CardView_Luong;
    public static final String TAG = MyFirebaseMessagingService.class.getName();
    private AlertDialog currentAlertDialog;
    private boolean isCheckedIn = false;
    private Handler handler;
    private TextView textViewMessages;
    private DatabaseReference messagesRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_user_layout);

        SliderImage();
        handler = new Handler(Looper.getMainLooper());
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTextViewRealTimeData();
            }
        }, 0, 1000);
        initui();
        setSupportActionBar(toolbar);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        setTitleToolbar();
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("User_Id");
            String userName = intent.getStringExtra("User_Name");
            txt_NameUser.setText(userName);
            String imageUrl = intent.getStringExtra("User_Img");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(img_User);
            } else {
                Toast.makeText(HomeUserActivity.this, "Lỗi không thể load ảnh", Toast.LENGTH_SHORT).show();
            }
        }
        // Ánh xạ TextView từ layout để hiển thị tin nhắn
        textViewMessages = findViewById(R.id.textViewMessages);

        // Khởi tạo DatabaseReference để tham chiếu đến node chứa tin nhắn trong Firebase
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        // Lắng nghe sự kiện thay đổi trên Firebase Realtime Database
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // Lấy dữ liệu từ DataSnapshot
                String messageText = dataSnapshot.child("messageText").getValue(String.class);
                String messageUser = dataSnapshot.child("messageUser").getValue(String.class);

                // Hiển thị tin nhắn lên TextView
                displayMessage(messageUser + ": " + messageText);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // Đối với trường hợp tin nhắn thay đổi (không xử lý trong ví dụ này)
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Đối với trường hợp tin nhắn bị xóa (không xử lý trong ví dụ này)
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // Đối với trường hợp tin nhắn được di chuyển (không xử lý trong ví dụ này)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Lấy FCM token thành công
                    String token = task.getResult();
                    saveUserTokenToDatabase(userId,token);
                });

        CardView_ChamCong.setOnClickListener(view -> {
            Intent intentQR = new Intent(HomeUserActivity.this, QRCodeGenerationActivity.class);
            intentQR.putExtra("User_IdHome", userId);
            startActivity(intentQR);
        });
        CardView_LichSuLamViec.setOnClickListener(view -> {
            Intent intent1 = new Intent(HomeUserActivity.this, WorkHistoryActivity.class);
            intent1.putExtra("User_IdHome", userId);
            startActivity(intent1);
        });
        CardView_Luong.setOnClickListener(view -> {
            Intent intent12 = new Intent(HomeUserActivity.this, SalaryActivity.class);
            intent12.putExtra("User_IdHome", userId);
            startActivity(intent12);
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }
    private void setTitleToolbar() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Trang chủ");
        }
    }
    private void saveUserTokenToDatabase(String userId, String token) {
        DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("user_tokens");
        tokensRef.child(userId).setValue(token);
    }
    private void initui()
    {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        txt_NameUser = findViewById(R.id.txt_NameUser);
        headerView = navigationView.getHeaderView(0);
        img_User = headerView.findViewById(R.id.img_User);
        CardView_ChamCong = findViewById(R.id.CardView_ChamCong);
        CardView_LichSuLamViec = findViewById(R.id.CardView_LichSuLamViec);
        CardView_Luong = findViewById(R.id.CardView_Luong);
        txt_today = findViewById(R.id.txt_today);
        img_today = findViewById(R.id.img_today);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_Logout) {
            Intent intent = new Intent(HomeUserActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        if(id == R.id.nav_user)
        {
            Intent intent = new Intent(HomeUserActivity.this, UpdateUserActivity.class);
            intent.putExtra("User_IdHome", userId);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void updateTextViewRealTimeData() {
        // Đoạn mã này sẽ được gọi mỗi giây để cập nhật thời gian trong TextView
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Lấy thời gian thực
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("HH:mm");
                String currentTime = dateFormat.format(new Date());

                // Hiển thị thời gian trong TextView
                String greeting = getGreeting(currentTime);
                String timeMessage = greeting;
                txt_today.setText(timeMessage);
            }
        });
    }
    private String getGreeting (String currentTime){

        int hour = Integer.parseInt(currentTime.split(":")[0]);
        if (hour >= 5 && hour < 12) {
            Drawable newDrawable = getResources().getDrawable(R.drawable.iconmorning);
            img_today.setImageDrawable(newDrawable);
            return "Chào buổi sáng";
        } else if (hour >= 12 && hour < 17) {
            Drawable newDrawable = getResources().getDrawable(R.drawable.iconafternoon);
            img_today.setImageDrawable(newDrawable);
            return "Chào buổi trưa";
        } else {
            Drawable newDrawable = getResources().getDrawable(R.drawable.iconevening);
            img_today.setImageDrawable(newDrawable);
            return "Chào buổi tối";
        }
    }
    private void SliderImage()
    {
        ImageSlider imageSlider = findViewById(R.id.ImageSlider);
        ArrayList<SlideModel> slideModels = new ArrayList<>();

        slideModels.add(new SlideModel(R.drawable.silder1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slider2, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slider3, ScaleTypes.FIT));

        imageSlider.setImageList(slideModels,ScaleTypes.FIT );

    }
    private void displayMessage(String message) {
        String currentMessages = textViewMessages.getText().toString();
        String updatedMessages = currentMessages + "\n" + message;
        textViewMessages.setText(updatedMessages);
    }
}

package com.example.nghiencuukhoahoc_appchamcong;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.nghiencuukhoahoc_appchamcong.fcm.MyFirebaseMessagingService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeAdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    TextView txt_NameUser,txt_today;
    String userId;
    CardView QLNV, QLCV,QLCL,XNCL,QLCC,CardView_SendMessage,CardView_QLL;
    CircleImageView img_User;
    ImageView img_today,QRicon;
    View headerView;
;

    private Handler handler;
    public static final String TAG = MyFirebaseMessagingService.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_admin_layout);
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
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
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

            }
        }
        QRicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentqr = new Intent(HomeAdminActivity.this,QRCodeReaderActivity.class);
                startActivity(intentqr);
            }
        });
        QLNV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeAdminActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });
        QLCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeAdminActivity.this, PositionActivity.class);
                startActivity(intent);
            }
        });
        QLCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeAdminActivity.this, ShiftActivity.class);
                startActivity(intent);
            }
        });
        XNCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeAdminActivity.this, EmploymentConfirmationActivity.class);
                startActivity(intent);
            }
        });
        QLCC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeAdminActivity.this, WorkHistoryAdminActivity.class);
                startActivity(intent);
            }
        });
        CardView_SendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeAdminActivity.this, SendMessageActivity.class);
                startActivity(intent);
            }
        });
        CardView_QLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeAdminActivity.this, SalaryActivityAdmin.class);
                startActivity(intent);
            }
        });

    }

    private void initui()
    {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        txt_NameUser = findViewById(R.id.txt_NameUser);
        img_today = findViewById(R.id.img_today);
        txt_today = findViewById(R.id.txt_today);
        QLNV = findViewById(R.id.CardView_QLNV);
        QLCV = findViewById(R.id.CardView_QLCV);
        headerView = navigationView.getHeaderView(0);
        img_User = headerView.findViewById(R.id.img_User);
        QLCL = findViewById(R.id.CardView_QLCL);
        XNCL = findViewById(R.id.CardView_XNCL);
        QLCC = findViewById(R.id.CardView_QLCC);
        QRicon = findViewById(R.id.QRicon);
        CardView_SendMessage = findViewById(R.id.CardView_SendMessage);
        CardView_QLL = findViewById(R.id.CardView_QLL);
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
        } else if (hour >= 12 && hour < 18) {
            Drawable newDrawable = getResources().getDrawable(R.drawable.iconafternoon);
            img_today.setImageDrawable(newDrawable);
            return "Chào buổi trưa";
        } else {
            Drawable newDrawable = getResources().getDrawable(R.drawable.iconevening);
            img_today.setImageDrawable(newDrawable);
            return "Chào buổi tối";
        }
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


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_user)
        {
            Intent intent = new Intent(HomeAdminActivity.this, UpdateUserActivity.class);
            intent.putExtra("User_IdHome", userId);
            startActivity(intent);
        }
        if(id == R.id.nav_list_user)
        {
            Intent intent = new Intent(HomeAdminActivity.this, ListUserActivity.class);
            startActivity(intent);
        }
        if(id == R.id.nav_list_shift)
        {
            Intent intent = new Intent(HomeAdminActivity.this, ListShiftActivity.class);
            startActivity(intent);
        }
        if(id == R.id.nav_position)
        {
            Intent intent = new Intent(HomeAdminActivity.this, PositionActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_Logout) {
            Intent intent = new Intent(HomeAdminActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}

package com.example.nghiencuukhoahoc_appchamcong;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import DTO.ChatMessage;

public class SendMessageActivity extends AppCompatActivity {
    private EditText editTextMessage;;
    private Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminmessagingservice);

        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotificationToEmployees(editTextMessage.getText().toString().trim());
            }
        });

    }

    private void sendNotificationToEmployees(String messageText) {
        DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("user_tokens");
        tokensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userToken = userSnapshot.getValue(String.class);
                    if (userToken != null) {
                        sendNotificationToEmployee(userToken, messageText);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void sendNotificationToEmployee(String userToken, String messageText) {
        JSONObject notificationData = new JSONObject();
        try {
            notificationData.put("to", userToken);
            JSONObject notification = new JSONObject();
            notification.put("title", "Thông báo mới");
            notification.put("body", messageText);
            notificationData.put("notification", notification);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", notificationData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("NotificationResponse", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("NotificationError", "Error sending notification: " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "key= AAAAEOiKbyI:APA91bFaKRRrJDJWETQpaokfQ7Uy7R2aDPprE3dZKfjQBSYuN9Mq7i0SdGIyg2uUwRL9hmKdPHYUK4nvueo3ADq7EJ_jiT0MW51bA1cx6b0z89SgyClVsyT8NRr6I7Zh1AR-gnIohkLN");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}


package com.example.nghiencuukhoahoc_appchamcong;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DecoratedBarcodeView.TorchListener;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;

import java.sql.Time;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import DTO.Shift;
import DTO.TimeKeeping;

public class QRCodeReaderActivity extends AppCompatActivity {
    private DecoratedBarcodeView barcodeView;
    private long updateId = 0;
    long idTimekeeping = 0;
    boolean isCheckedIn = true;
    String shiftName;
    private SharedPreferences sharedPreferencesCaLam;
    private DatabaseReference userCheckInStatusRef;
    boolean isQRCodeScanned = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_reader);
        userCheckInStatusRef = FirebaseDatabase.getInstance().getReference("UserCheckInStatus");

        barcodeView = findViewById(R.id.barcode_scanner);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        sharedPreferencesCaLam = getSharedPreferences("MyPrefs_CaLam", MODE_PRIVATE);
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (!isQRCodeScanned) { // Kiểm tra xem đã quét mã QR hay chưa
                    handleScannedQRCode(result.getText());
                    isQRCodeScanned = true;
                }

            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Có thể xử lý các điểm kết quả có thể ở đây (không cần thiết)
            }
        });
    }
    private void handleScannedQRCode(String scannedText) {
        DatabaseReference userCheckInRef = FirebaseDatabase.getInstance().getReference("UserCheckInStatus").child(scannedText);

        // Kiểm tra trạng thái check-in của người dùng trên Firebase
        userCheckInRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isCheckedIn = false;

                // Duyệt qua các ca làm việc để kiểm tra trạng thái check-in
                for (DataSnapshot shiftSnapshot : dataSnapshot.getChildren()) {
                    Boolean isCheckedInValue = shiftSnapshot.child("isCheckedIn").getValue(Boolean.class);
                    if (isCheckedInValue != null && isCheckedInValue.booleanValue()) {
                        isCheckedIn = true;
                        break;
                    }
                }
                // Kiểm tra xem ngày hiện tại có phải là ngày mới không
                if (isNewDay(getLastCheckInTime(dataSnapshot))) {
                    resetCheckInStatusForNewDay(userCheckInRef);
                }

                // Nếu chưa check-in, thực hiện check-in và cập nhật trạng thái true trên Firebase
                if (!isCheckedIn) {
                    performCheckIn(scannedText);
                } else {
                    // Ngược lại, thực hiện check-out và cập nhật trạng thái false trên Firebase
                    performCheckOut(scannedText);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi
            }
        });
    }

    private void performCheckIn(String userId) {
        DatabaseReference userCheckInRef = FirebaseDatabase.getInstance().getReference("UserCheckInStatus").child(userId);

        userCheckInRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userCheckInSnapshot) {
                DatabaseReference shiftsRef = FirebaseDatabase.getInstance().getReference("Shift");
                shiftsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot shiftDataSnapshot) {
                        for (DataSnapshot shiftSnapshot : shiftDataSnapshot.getChildren()) {
                            Shift shift = shiftSnapshot.getValue(Shift.class);

                            if (shift != null && isWithinShiftTime(shift)) {
                                String shiftId = String.valueOf(shift.getId_Shift());
                                if (!hasCheckedInForShift(userCheckInSnapshot, shiftId)) {
                                    if (!hasCheckedOutForShift(userCheckInSnapshot, shiftId)) {
                                        // Nếu chưa check-out, thực hiện check-in
                                        handleCheckIn(userId, shift);

                                        // Cập nhật trạng thái check-in và thời điểm cuối cùng check-in trong Firebase Realtime Database
                                        userCheckInRef.child(shiftId).child("isCheckedIn").setValue(true);
                                        userCheckInRef.child("lastCheckInTime").setValue(System.currentTimeMillis());
                                    } else {
                                        Toast.makeText(QRCodeReaderActivity.this, "Bạn đã check-out cho ca làm này rồi", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                break; // Dừng vòng lặp sau khi kiểm tra xong
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Xử lý lỗi ở đây
                    }
                });

                isQRCodeScanned = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi ở đây
            }
        });
    }



    private boolean hasCheckedInForShift(DataSnapshot userCheckInSnapshot, String shiftId) {
        // Kiểm tra xem đã check-in cho ca làm việc hay chưa
        Boolean isCheckedIn = userCheckInSnapshot.child(shiftId).child("isCheckedIn").getValue(Boolean.class);
        return isCheckedIn != null ? isCheckedIn : false;
    }
    private boolean hasCheckedOutForShift(DataSnapshot userCheckInSnapshot, String shiftId) {
        // Kiểm tra xem đã check-out cho ca làm việc hay chưa
        Boolean isCheckedOut = userCheckInSnapshot.child(shiftId).child("isCheckedIn").getValue(Boolean.class);
        return isCheckedOut != null && !isCheckedOut; // Trả về true nếu đã check-out, false nếu chưa
    }

    private long getLastCheckInTime(DataSnapshot dataSnapshot) {
        Long lastCheckInTime = dataSnapshot.child("lastCheckInTime").getValue(Long.class);
        return lastCheckInTime != null ? lastCheckInTime : 0;
    }

    private boolean isNewDay(long lastCheckInTime) {
        // Kiểm tra xem có phải là ngày mới hay không dựa trên thời điểm cuối cùng check-in
        Calendar todayCalendar = Calendar.getInstance();
        long currentTime = System.currentTimeMillis();

        todayCalendar.setTimeInMillis(currentTime);
        int todayYear = todayCalendar.get(Calendar.YEAR);
        int todayDayOfYear = todayCalendar.get(Calendar.DAY_OF_YEAR);

        Calendar checkInCalendar = Calendar.getInstance();
        checkInCalendar.setTimeInMillis(lastCheckInTime);
        int checkInYear = checkInCalendar.get(Calendar.YEAR);
        int checkInDayOfYear = checkInCalendar.get(Calendar.DAY_OF_YEAR);

        return todayYear != checkInYear || todayDayOfYear != checkInDayOfYear;
    }

    private void resetCheckInStatusForNewDay(DatabaseReference userCheckInRef) {
        // Đặt lại trạng thái check-in cho ngày mới
        userCheckInRef.child("isCheckedIn").setValue(false);
        userCheckInRef.child("lastCheckInTime").setValue(System.currentTimeMillis());
    }

    private boolean isWithinShiftTime(Shift shift) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        int shiftStartHour = (int) (shift.getTime_Start() / 3600000);
        int shiftStartMinute = (int) ((shift.getTime_Start() % 3600000) / 60000);

        int shiftEndHour = (int) (shift.getTime_End() / 3600000);
        int shiftEndMinute = (int) ((shift.getTime_End() % 3600000) / 60000);

        return (currentHour > shiftStartHour || (currentHour == shiftStartHour && currentMinute >= shiftStartMinute))
                && (currentHour < shiftEndHour || (currentHour == shiftEndHour && currentMinute <= shiftEndMinute));
    }



    private void handleCheckIn(String userId, Shift shift) {
        DatabaseReference timekeepingRef = FirebaseDatabase.getInstance().getReference("TimeKeeping");
        timekeepingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long maxId = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    long key = Long.parseLong(snapshot.getKey());
                    if (key > maxId) {
                        maxId = key;
                    }
                }

                TimeKeeping newTimeKeeping = new TimeKeeping();
                Calendar calendar = Calendar.getInstance();
                int gio = calendar.get(Calendar.HOUR_OF_DAY);
                int phut = calendar.get(Calendar.MINUTE);
                int giay = calendar.get(Calendar.SECOND);
                String gioHienTai = String.format(Locale.getDefault(), "%02d:%02d:00", gio, phut, giay);
                Date ngayHienTai = calendar.getTime();
                Time Gio = Time.valueOf(gioHienTai);

                long newId = maxId + 1;
                idTimekeeping = newId;
                newTimeKeeping.setId_Timekeeping((int) newId);
                newTimeKeeping.setId_User(Integer.parseInt(userId));
                newTimeKeeping.setDate(ngayHienTai.getTime());
                newTimeKeeping.setTime_CheckIn(Gio.getTime());

                newTimeKeeping.setShift(shift);

                timekeepingRef.child(String.valueOf(newId)).setValue(newTimeKeeping, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @androidx.annotation.NonNull DatabaseReference ref) {
                        if (error == null) {
                            isCheckedIn = false;
                            Toast.makeText(QRCodeReaderActivity.this, "Check-in thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(QRCodeReaderActivity.this, "Lỗi hệ thống không thể check-in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
            }
        });
    }

    private void performCheckOut(String userId) {
        DatabaseReference userCheckInRef = FirebaseDatabase.getInstance().getReference("UserCheckInStatus").child(userId);

        // Lấy thời gian hiện tại
        Calendar calendar = Calendar.getInstance();
        int gio = calendar.get(Calendar.HOUR_OF_DAY);
        int phut = calendar.get(Calendar.MINUTE);
        int giay = calendar.get(Calendar.SECOND);
        String gioHienTai = String.format(Locale.getDefault(), "%02d:%02d:00", gio, phut, giay);
        Time gioHienTaiTime = Time.valueOf(gioHienTai);

        // Kiểm tra xem người dùng đã check-in cho ca làm việc nào chưa
        userCheckInRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userCheckInSnapshot) {
                // Lặp qua các ca làm việc
                for (DataSnapshot shiftSnapshot : userCheckInSnapshot.getChildren()) {
                    // Kiểm tra xem người dùng đã check-in cho ca làm việc này hay chưa
                    Boolean isCheckedIn = shiftSnapshot.child("isCheckedIn").getValue(Boolean.class);
                    if (isCheckedIn != null && isCheckedIn) {
                        // Nếu đã check-in, thực hiện check-out
                        String shiftKey = shiftSnapshot.getKey(); // Lấy shiftId từ key của dataSnapshot
                        handleCheckOut(userId, shiftKey, gioHienTaiTime.getTime()); // Sửa đổi để truyền shiftKey thay vì đối tượng Shift

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi
            }
        });
    }


    // Phương thức xử lý check-out
    private void handleCheckOut(String userId, String shiftId, long checkOutTimeMillis) {
        // Cập nhật isCheckedIn trong UserCheckInStatus
        DatabaseReference userCheckInRef = FirebaseDatabase.getInstance().getReference("UserCheckInStatus").child(userId).child(shiftId);
        userCheckInRef.child("isCheckedIn").setValue(false);

        // Cập nhật time_CheckOut trong TimeKeeping
        DatabaseReference timekeepingRef = FirebaseDatabase.getInstance().getReference("TimeKeeping");
        timekeepingRef.orderByChild("id_User").equalTo(Integer.parseInt(userId)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TimeKeeping timeKeeping = snapshot.getValue(TimeKeeping.class);
                    if (timeKeeping != null) {
                        Shift shift = timeKeeping.getShift();
                        if (shift != null && shift.getId_Shift() == Integer.parseInt(shiftId)) {
                            snapshot.getRef().child("time_CheckOut").setValue(checkOutTimeMillis);
                            Toast.makeText(QRCodeReaderActivity.this, "Check-out thành công", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Toast.makeText(QRCodeReaderActivity.this, "Check-in thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
        isQRCodeScanned = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

}
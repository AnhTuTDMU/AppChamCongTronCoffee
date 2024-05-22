package DTO;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SalaryCalculator {
    private DatabaseReference timekeepingRef;

    public SalaryCalculator() {
        // Khởi tạo DatabaseReference để tham chiếu đến node chứa bảng Timekeeping trong Firebase
        timekeepingRef = FirebaseDatabase.getInstance().getReference("TimeKeeping");
    }

    public void calculateSalary() {
        // Truy vấn dữ liệu từ Firebase Realtime Database
        Query query = timekeepingRef.orderByChild("date");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy dữ liệu từ snapshot
                    long date = snapshot.child("date").getValue(Long.class);
                    int userId = snapshot.child("id_User").getValue(Integer.class);
                    long checkInTime = snapshot.child("time_CheckIn").getValue(Long.class);
                    long checkOutTime = snapshot.child("time_CheckOut").getValue(Long.class);

                    // Tính toán thời gian làm việc
                    long workTime = checkOutTime - checkInTime;

                    // Tính lương dựa trên thời gian làm việc và mức lương của nhân viên
                    int salary = calculateSalaryForEmployee(userId, workTime);

                    // Lưu lương vào Firebase hoặc thực hiện xử lý tiếp theo tùy theo yêu cầu của bạn
                    // Ví dụ: salaryRef.child(userId).setValue(salary);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra trong quá trình truy vấn dữ liệu
            }
        });
    }

    private int calculateSalaryForEmployee(int userId, long workTime) {
        // Viết mã để tính lương dựa trên thời gian làm việc và mức lương của nhân viên
        // Trong ví dụ này, giả sử mức lương là 1000 VND/giờ
        int hourlyWage = 1000;
        return (int) (workTime * hourlyWage);
    }
}

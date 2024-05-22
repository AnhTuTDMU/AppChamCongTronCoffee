package Adapter;

import android.content.Context;
import android.icu.text.DateFormatSymbols;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.example.nghiencuukhoahoc_appchamcong.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import DTO.Shift;
import DTO.TimeKeeping;
import DTO.Users;


public class TimekeepingAdapter extends ArrayAdapter<TimeKeeping> {
    private Context context;
    private int resource;
    private List<TimeKeeping> timekeepings;
    public TimekeepingAdapter(Context context, int resource, List<TimeKeeping> timekeeping) {
        super(context,resource,timekeeping);
        this.context = context;
        this.resource = resource;
        this.timekeepings = timekeeping;
    }
    public List<TimeKeeping> getData() {
        return timekeepings;
    }
    public void updateData(List<TimeKeeping> newData) {
        timekeepings.clear();
        timekeepings.addAll(newData);
        notifyDataSetChanged();
    }



    public List<String> getYearData() {
        List<String> years = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        for (int year = currentYear; year >= 2022; year--) {
            years.add(String.valueOf(year));
        }

        return years;
    }

    public List<String> getMonthData() {
        List<String> months = new ArrayList<>();
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] monthNames = dfs.getMonths();

        for (int month = 0; month < monthNames.length ; month++) {
            months.add(monthNames[month]);
        }

        return months;
    }



    @Override
    public View getView(int timekeeping, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, null);
        }
        TimeKeeping item_timekeepings = timekeepings.get(timekeeping);
        int userId = getItem(timekeeping).getId_User();
        int shiftid = getItem(timekeeping).getShift().getId_Shift();
        if (item_timekeepings != null) {
            TextView txt_TenNhanVienChamcong = convertView.findViewById(R.id.txt_TenNhanVienChamcong);
            TextView txt_Ngay = convertView.findViewById(R.id.txt_Ngay);
            TextView txt_GioVao = convertView.findViewById(R.id.txt_GioVao);
            TextView txt_GioRa = convertView.findViewById(R.id.txt_GioRa);
            TextView txt_TenCaLamViecChamCong = convertView.findViewById(R.id.txt_TenCaLamViecChamCong);
            TextView txt_TongGioLamViec = convertView.findViewById(R.id.txt_TongGioLamViec);
            if (txt_TenNhanVienChamcong != null) {
                FirebaseDatabase.getInstance().getReference("Users").child(String.valueOf(userId)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Users user = dataSnapshot.getValue(Users.class);
                            if (user != null) {
                                String userName = user.getName();
                                txt_TenNhanVienChamcong.setText(userName);
                                txt_TenNhanVienChamcong.setTextColor(getContext().getColor(R.color.blue));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Xử lý lỗi nếu cần
                    }
                });
            }
            if (txt_Ngay != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = dateFormat.format(item_timekeepings.getDate());
                txt_Ngay.setText(formattedDate);
            }

            if (txt_GioVao != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                String formattedTime = timeFormat.format(item_timekeepings.getTime_CheckIn());
                txt_GioVao.setText(formattedTime);
            }

            if (txt_GioRa != null) {
                if(item_timekeepings.getTime_CheckOut() == 0)
                {
                    txt_GioRa.setText("Chưa check-out");
                    txt_GioRa.setTextColor(getContext().getColor(R.color.red));
                }
                else {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    String formattedTime = timeFormat.format(item_timekeepings.getTime_CheckOut());
                    txt_GioRa.setText(formattedTime);
                }
            }
            if (txt_TenCaLamViecChamCong != null) {
                FirebaseDatabase.getInstance().getReference("Shift").child(String.valueOf(shiftid)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Shift shift = dataSnapshot.getValue(Shift.class);
                            if (shift != null) {
                                String shiftname = shift.getName_Shift();
                                txt_TenCaLamViecChamCong.setText(shiftname);
                                txt_TenCaLamViecChamCong.setTextColor(getContext().getColor(R.color.blue));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Xử lý lỗi nếu cần
                    }
                });
            }
            // Bên trong phương thức getView của TimekeepingAdapter

            if (txt_TongGioLamViec != null) {
                long checkInTime = item_timekeepings.getTime_CheckIn(); // Thời gian check-in
                long checkOutTime = item_timekeepings.getTime_CheckOut(); // Thời gian check-out

                if (checkOutTime == 0) {
                    // Nếu chưa check-out, hiển thị thông báo
                    txt_TongGioLamViec.setText("Chưa check-out");
                    txt_TongGioLamViec.setTextColor(getContext().getColor(R.color.red));
                } else {
                    // Nếu đã check-out, tính tổng số giờ và phút làm việc
                    long workTimeInMillis = checkOutTime - checkInTime;
                    int hours = (int) (workTimeInMillis / (1000 * 60 * 60));
                    int minutes = (int) ((workTimeInMillis / (1000 * 60)) % 60);

                    txt_TongGioLamViec.setText(hours + " giờ " + minutes + " phút");
                    txt_TongGioLamViec.setTextColor(getContext().getColor(R.color.blue));
                }
            }


        }
        return convertView;
    }
    public String getTotalWorkTime() {
        long totalWorkTimeInMillis = 0;

        for (TimeKeeping timeKeeping : timekeepings) {
            long checkInTime = timeKeeping.getTime_CheckIn();
            long checkOutTime = timeKeeping.getTime_CheckOut();

            if (checkOutTime != 0) {
                totalWorkTimeInMillis += checkOutTime - checkInTime;
            }
        }

        // Chuyển đổi tổng thời gian làm việc từ millisecond sang giờ và phút
        int totalHours = (int) (totalWorkTimeInMillis / (1000 * 60 * 60));
        int totalMinutes = (int) ((totalWorkTimeInMillis / (1000 * 60)) % 60);

        String coloredTitle = "<font color='#000000'>Tổng giờ làm trong tháng:</font>";
        return coloredTitle + "<br/>" + totalHours + " giờ " + totalMinutes + " phút";

    }
    public int DemNgayCong(List<TimeKeeping> timekeepingList) {
        int DemNgayCongs = 0;

        for (TimeKeeping timekeeping : timekeepingList) {
            if (timekeeping.getStatus()) {
                DemNgayCongs++;
            }
        }

        return DemNgayCongs;
    }
}

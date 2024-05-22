package DTO;
import android.icu.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Shift {
    private int id_Shift;
    private String name_Shift;
    private long Time_Start;
    private long Time_End;
    private CheckInStatus checkInStatus;
    public Shift(){}
    public Shift(int id_Shift, String name_Shift){
        super();
        this.id_Shift = id_Shift;
        this.name_Shift = name_Shift;
        this.checkInStatus = new CheckInStatus();
    }

    public CheckInStatus getCheckInStatus() {
        return checkInStatus;
    }

    public void setCheckInStatus(CheckInStatus checkInStatus) {
        this.checkInStatus = checkInStatus;
    }
    public Shift(int id_Shift, String name_Shift, long Time_Start, long Time_End)
    {
        super();
        this.id_Shift = id_Shift;
        this.name_Shift = name_Shift;
        this.Time_Start = Time_Start;
        this.Time_End = Time_End;
    }
    public int getId_Shift() {
        return id_Shift;
    }

    public void setId_Shift(int id_Shift) {
        this.id_Shift = id_Shift;
    }

    public String getName_Shift() {
        return name_Shift;
    }

    public void setName_Shift(String name_Shift) {
        this.name_Shift = name_Shift;
    }

    public long getTime_Start() {
        return Time_Start;
    }

    public void setTime_Start(long time_Start) {
        Time_Start = time_Start;
    }

    public long getTime_End() {
        return Time_End;
    }

    public void setTime_End(long Time_End) {
        this.Time_End = Time_End;
    }

    public String convertMillisToTime(long millis) {
        android.icu.text.SimpleDateFormat formatter = new android.icu.text.SimpleDateFormat("HH:mm", Locale.getDefault());
        formatter.setTimeZone(android.icu.util.TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date(millis));
    }

}

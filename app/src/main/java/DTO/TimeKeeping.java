package DTO;

import java.util.HashMap;
import java.util.Map;

public class TimeKeeping {
    private int Id_Timekeeping;
    private int Id_User;
    private String name_Shift;
    private String name_User;
    private long Date;
    private long Time_CheckIn;
    private long Time_CheckOut;
    private Shift shift;
    private boolean status;
    private Map<String, Boolean> userShiftCheckIn;
    public TimeKeeping(){}

    public TimeKeeping(int id_Timekeeping, int id_User, long date, long time_CheckIn, long time_CheckOut, Shift shift, boolean status) {
        super();
        Id_Timekeeping = id_Timekeeping;
        Id_User = id_User;
        Date = date;
        Time_CheckIn = time_CheckIn;
        Time_CheckOut = time_CheckOut;
        this.shift = shift;
        this.status = status;
    }

    public Map<String, Boolean> getUserShiftCheckIn() {
        return userShiftCheckIn;
    }

    public void setUserShiftCheckIn(Map<String, Boolean> userShiftCheckIn) {
        this.userShiftCheckIn = userShiftCheckIn;
    }
    public String getName_Shift() {
        return name_Shift;
    }

    public void setName_Shift(String name_Shift) {
        this.name_Shift = name_Shift;
    }
    public String getName_User() {
        return name_User;
    }

    public void setName_User(String name_User) {
        this.name_User = name_User;
    }
    public int getId_Timekeeping() {
        return Id_Timekeeping;
    }

    public void setId_Timekeeping(int id_Timekeeping) {
        Id_Timekeeping = id_Timekeeping;
    }

    public int getId_User() {
        return Id_User;
    }

    public void setId_User(int id_User) {
        Id_User = id_User;
    }

    public long getDate() {
        return Date;
    }

    public void setDate(long date) {
        Date = date;
    }

    public long getTime_CheckIn() {
        return Time_CheckIn;
    }

    public void setTime_CheckIn(long time_CheckIn) {
        Time_CheckIn = time_CheckIn;
    }

    public long getTime_CheckOut() {
        return Time_CheckOut;
    }

    public void setTime_CheckOut(long time_CheckOut) {
        Time_CheckOut = time_CheckOut;
    }
    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }
    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

}

package DTO;

public class Salary {
    private String MaNV_Thang_Nam;
    private int MaNV;
    private int thang; // Thêm trường thang
    private int nam;
    private int ngayCong;
    private double luongThucTe;

    public Salary() {
        // Constructor mặc định cần thiết cho việc đọc dữ liệu từ Firebase
    }

    public Salary(String maNV_Thang_Nam, int maNV, int thang, int nam, int ngayCong, double luongThucTe) {
        MaNV_Thang_Nam = maNV_Thang_Nam;
        MaNV = maNV;
        this.thang = thang;
        this.nam = nam;
        this.ngayCong = ngayCong;
        this.luongThucTe = luongThucTe;
    }

    public String getMaNV_Thang_Nam() {
        return MaNV_Thang_Nam;
    }

    public void setMaNV_Thang_Nam(String maNV_Thang_Nam) {
        MaNV_Thang_Nam = maNV_Thang_Nam;
    }

    public int getMaNV() {
        return MaNV;
    }

    public void setMaNV(int maNV) {
        MaNV = maNV;
    }

    public int getThang() {
        return thang;
    }

    public void setThang(int thang) {
        this.thang = thang;
    }

    public int getNam() {
        return nam;
    }

    public void setNam(int nam) {
        this.nam = nam;
    }

    public int getNgayCong() {
        return ngayCong;
    }

    public void setNgayCong(int ngayCong) {
        this.ngayCong = ngayCong;
    }

    public double getLuongThucTe() {
        return luongThucTe;
    }

    public void setLuongThucTe(double luongThucTe) {
        this.luongThucTe = luongThucTe;
    }
}
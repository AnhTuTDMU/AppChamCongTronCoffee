package DTO;

import java.util.HashMap;
import java.util.Map;

public class Users {

    private int id_User;
    private String Img;
    private String name;
    private String account;
    private String password;
    private long dateOfBirth;

    private String sex;
    private String phoneNumber;
    private String email;
    private Double salary;
    private Position position;

    public Users(){}
    public Users(String account, String password, String phoneNumber, long dateOfBirth, String email, String sex) {
        super();
        this.account = account;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.sex = sex;
    }

    public Users(int id_User, String name,String Img, String account, String password, long dateOfBirth,String sex, String phoneNumber, String email, Double salary, Position position) {
        this.id_User = id_User;
        this.name = name;
        this.Img = Img;
        this.account = account;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.sex = sex;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.salary = salary;
        this.position = position;
    }

    public int getId_User() {
        return id_User;
    }

    public void setId_User(int id_User) {
        this.id_User = id_User;
    }
    public String getImg() {
        return Img;
    }

    public void setImg(String img) {
        Img = img;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }
    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}

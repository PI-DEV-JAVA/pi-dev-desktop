package talentospidev.models;

import java.time.LocalDate;

public class UserViewModel {

    private int id;
    private String fullName;
    private String email;
    private String role;
    private int age;
    private String phoneNumber;
    private LocalDate joinDate;
    private String status;
    private String location;
    private String professionalTitle;
    private String summary;

    public UserViewModel(int id, String fullName, String email, String role, int age, String phoneNumber,
            LocalDate joinDate, String status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.joinDate = joinDate;
        this.status = status;
    }

    public UserViewModel(int id, String fullName, String email, String role, int age, String phoneNumber,
            LocalDate joinDate, String status, String location, String professionalTitle, String summary) {
        this(id, fullName, email, role, age, phoneNumber, joinDate, status);
        this.location = location;
        this.professionalTitle = professionalTitle;
        this.summary = summary;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location != null ? location : "";
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProfessionalTitle() {
        return professionalTitle != null ? professionalTitle : "";
    }

    public void setProfessionalTitle(String professionalTitle) {
        this.professionalTitle = professionalTitle;
    }

    public String getSummary() {
        return summary != null ? summary : "";
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}

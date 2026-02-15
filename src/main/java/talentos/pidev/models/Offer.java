package talentos.pidev.models;

import java.time.LocalDate;

public class Offer {
    private int id;
    private String title;
    private String description;
    private String department;
    private String contractType;
    private String experienceLevel;
    private double salaryMin;
    private double salaryMax;
    private String location;
    private String status;
    private LocalDate publishDate;
    private LocalDate closingDate;
    private int positionsAvailable;
    private int applicationsReceived;

    // Constructeurs
    public Offer() {}

    public Offer(String title, String description, String department, String contractType,
                 String experienceLevel, double salaryMin, double salaryMax, String location,
                 String status, LocalDate publishDate, LocalDate closingDate, int positionsAvailable) {
        this.title = title;
        this.description = description;
        this.department = department;
        this.contractType = contractType;
        this.experienceLevel = experienceLevel;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.location = location;
        this.status = status;
        this.publishDate = publishDate;
        this.closingDate = closingDate;
        this.positionsAvailable = positionsAvailable;
        this.applicationsReceived = 0;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public double getSalaryMin() { return salaryMin; }
    public void setSalaryMin(double salaryMin) { this.salaryMin = salaryMin; }

    public double getSalaryMax() { return salaryMax; }
    public void setSalaryMax(double salaryMax) { this.salaryMax = salaryMax; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }

    public LocalDate getClosingDate() { return closingDate; }
    public void setClosingDate(LocalDate closingDate) { this.closingDate = closingDate; }

    public int getPositionsAvailable() { return positionsAvailable; }
    public void setPositionsAvailable(int positionsAvailable) { this.positionsAvailable = positionsAvailable; }

    public int getApplicationsReceived() { return applicationsReceived; }
    public void setApplicationsReceived(int applicationsReceived) { this.applicationsReceived = applicationsReceived; }

    @Override
    public String toString() {
        return title + " (" + department + ")";
    }
}
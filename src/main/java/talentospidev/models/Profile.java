package talentospidev.models;

import java.time.LocalDate;

public class Profile {

    private int id;
    private User user;

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String phoneNumber;
    private String location;
    private String professionalTitle;
    private int yearsOfExperience;
    private String summary;
    private String profilePicturePath;
    private String cvPath;
    private boolean profileCompleted;

    // Constructors
    public Profile() {
    }

    public Profile(User user) {
        this.user = user;
        this.profileCompleted = false;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        checkProfileCompletion();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        checkProfileCompletion();
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
        checkProfileCompletion();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        checkProfileCompletion();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        checkProfileCompletion();
    }

    public String getProfessionalTitle() {
        return professionalTitle;
    }

    public void setProfessionalTitle(String professionalTitle) {
        this.professionalTitle = professionalTitle;
        checkProfileCompletion();
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
        checkProfileCompletion();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
        checkProfileCompletion();
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public String getCvPath() {
        return cvPath;
    }

    public void setCvPath(String cvPath) {
        this.cvPath = cvPath;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    /**
     * Computes profile completion percentage based on milestones:
     * - 20% = account created (always true)
     * - +40% = profile info filled (firstName, lastName, phone, title, location)
     * - +10% = profile picture uploaded
     * - +20% = email verified (needs User reference)
     * - +10% = CV uploaded
     */
    public int getCompletionPercentage(boolean emailVerified) {
        int pct = 20; // Account exists

        // Profile info filled (+40%)
        boolean infoFilled = firstName != null && !firstName.isEmpty()
                && lastName != null && !lastName.isEmpty()
                && phoneNumber != null && !phoneNumber.isEmpty()
                && professionalTitle != null && !professionalTitle.isEmpty()
                && location != null && !location.isEmpty();
        if (infoFilled)
            pct += 40;

        // Profile picture (+10%)
        if (profilePicturePath != null && !profilePicturePath.isEmpty())
            pct += 10;

        // Email verified (+20%)
        if (emailVerified)
            pct += 20;

        // CV uploaded (+10%)
        if (cvPath != null && !cvPath.isEmpty())
            pct += 10;

        return pct;
    }

    // Internal logic
    private void checkProfileCompletion() {
        this.profileCompleted = firstName != null &&
                lastName != null &&
                birthDate != null &&
                phoneNumber != null &&
                professionalTitle != null;
    }
}

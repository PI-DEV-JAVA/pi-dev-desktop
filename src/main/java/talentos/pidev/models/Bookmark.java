package talentos.pidev.models;

import java.time.LocalDateTime;

public class Bookmark {
    private int id;
    private int candidateId;      // ID du candidat (si connecté)
    private int offerId;          // ID de l'offre sauvegardée
    private LocalDateTime savedAt;
    private String notes;         // Notes personnelles sur l'offre

    // Constructeurs
    public Bookmark() {}

    public Bookmark(int candidateId, int offerId) {
        this.candidateId = candidateId;
        this.offerId = offerId;
        this.savedAt = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public int getOfferId() { return offerId; }
    public void setOfferId(int offerId) { this.offerId = offerId; }

    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
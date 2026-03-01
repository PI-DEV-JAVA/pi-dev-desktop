package com.pi.models;

import java.time.LocalDateTime;

public class Feedback {
    private int idFeedback;
    private int idParticipation;
    private int note;
    private String commentaire;
    private LocalDateTime dateFeedback;
    private boolean recommanderait;

    public Feedback() {}

    public Feedback(int idParticipation, int note, String commentaire, boolean recommanderait) {
        this.idParticipation = idParticipation;
        this.note = note;
        this.commentaire = commentaire;
        this.recommanderait = recommanderait;
        this.dateFeedback = LocalDateTime.now();
    }

    // Getters et Setters
    public int getIdFeedback() { return idFeedback; }
    public void setIdFeedback(int idFeedback) { this.idFeedback = idFeedback; }

    public int getIdParticipation() { return idParticipation; }
    public void setIdParticipation(int idParticipation) { this.idParticipation = idParticipation; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateFeedback() { return dateFeedback; }
    public void setDateFeedback(LocalDateTime dateFeedback) { this.dateFeedback = dateFeedback; }

    public boolean isRecommanderait() { return recommanderait; }
    public void setRecommanderait(boolean recommanderait) { this.recommanderait = recommanderait; }

    @Override
    public String toString() {
        return "Feedback{id=" + idFeedback +
                ", participation=" + idParticipation +
                ", note=" + note +
                ", date=" + dateFeedback + "}";
    }
}
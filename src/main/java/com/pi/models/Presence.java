package com.pi.models;

import java.time.LocalDateTime;

public class Presence {
    private int idPresence;
    private int idParticipation;
    private boolean estPresent;
    private LocalDateTime dateScan;
    private String codeQr;

    public Presence() {}

    public Presence(int idParticipation, boolean estPresent, LocalDateTime dateScan, String codeQr) {
        this.idParticipation = idParticipation;
        this.estPresent = estPresent;
        this.dateScan = dateScan;
        this.codeQr = codeQr;
    }

    // Getters et Setters
    public int getIdPresence() { return idPresence; }
    public void setIdPresence(int idPresence) { this.idPresence = idPresence; }

    public int getIdParticipation() { return idParticipation; }
    public void setIdParticipation(int idParticipation) { this.idParticipation = idParticipation; }

    public boolean isEstPresent() { return estPresent; }
    public void setEstPresent(boolean estPresent) { this.estPresent = estPresent; }

    public LocalDateTime getDateScan() { return dateScan; }
    public void setDateScan(LocalDateTime dateScan) { this.dateScan = dateScan; }

    public String getCodeQr() { return codeQr; }
    public void setCodeQr(String codeQr) { this.codeQr = codeQr; }

    @Override
    public String toString() {
        return "Présence{" +
                "id=" + idPresence +
                ", participation=" + idParticipation +
                ", présent=" + (estPresent ? "Oui" : "Non") +
                ", scan=" + dateScan +
                '}';
    }
}
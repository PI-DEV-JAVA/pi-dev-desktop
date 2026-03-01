package talentos.pidev.models;

import java.time.LocalDate;

public class Quiz {
    private int id;
    private String titre;
    private String description;
    private int dureeMinutes;
    private LocalDate dateCreation;
    private boolean actif;
    private Integer seanceId;


    public Quiz() {}

    public Quiz(int id, String titre, String description, int dureeMinutes, LocalDate dateCreation, boolean actif, Integer seanceId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dureeMinutes = dureeMinutes;
        this.dateCreation = dateCreation;
        this.actif = actif;
        this.seanceId=seanceId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public Integer getSeanceId() { return seanceId; }
    public void setSeanceId(Integer seanceId) { this.seanceId = seanceId; }

    @Override
    public String toString() {
        return "Quiz{id=" + id + ", titre='" + titre + "', dureeMinutes=" + dureeMinutes + ", actif=" + actif + "}";
    }
}

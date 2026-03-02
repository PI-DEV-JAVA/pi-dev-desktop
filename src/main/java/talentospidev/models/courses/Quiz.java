package talentospidev.models.courses;

import java.time.LocalDateTime;

public class Quiz {
    private int id;
    private String titre;
    private String description;
    private int dureeMinutes;
    private boolean actif;
    private LocalDateTime createdAt;
    private Integer seanceId;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getSeanceId() { return seanceId; }
    public void setSeanceId(Integer seanceId) { this.seanceId = seanceId; }
}

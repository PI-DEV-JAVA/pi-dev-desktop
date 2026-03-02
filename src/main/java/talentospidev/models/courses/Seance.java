package talentospidev.models.courses;

import java.time.LocalDateTime;

public class Seance {
    private int id;
    private int formationId;
    private String titre;
    private String type;           // PRESENTIEL / EN_LIGNE
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String adresse;
    private Double latitude;
    private Double longitude;
    private String videoPath;
    private Integer dureeMinutes;
    private String statut;         // PLANIFIEE / EN_COURS / TERMINEE

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFormationId() { return formationId; }
    public void setFormationId(int formationId) { this.formationId = formationId; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }

    public Integer getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}

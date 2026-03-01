package com.pi.services;

import com.pi.dao.PresenceDAO;
import com.pi.models.Presence;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PresenceService {

    private PresenceDAO presenceDAO;

    public PresenceService() {
        this.presenceDAO = new PresenceDAO();
    }

    // Générer QR code pour une participation
    public byte[] genererQRCode(int idParticipation) throws WriterException, IOException {
        String data = "PARTICIPATION:" + idParticipation;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    // Créer une présence (sans date de scan)
    public void creerPresence(Presence presence) throws SQLException {
        presenceDAO.ajouter(presence);
    }

    // Scanner présence - Met à jour avec date du scan
    public boolean scannerPresence(String codeQr) throws SQLException {
        if (codeQr.startsWith("PARTICIPATION:")) {
            int idParticipation = Integer.parseInt(codeQr.substring(14));
            presenceDAO.marquerPresent(idParticipation);
            return true;
        }
        return false;
    }

    // Vérifier si un participant est présent
    public boolean estPresent(int idParticipation) throws SQLException {
        Presence presence = presenceDAO.getByParticipation(idParticipation);
        return presence != null && presence.isEstPresent();
    }

    // Obtenir présence par participation
    public Presence getPresenceByParticipation(int idParticipation) throws SQLException {
        return presenceDAO.getByParticipation(idParticipation);
    }

    // Obtenir toutes les présences d'un événement
    public List<Presence> getPresencesByEvent(int idEvent) throws SQLException {
        return presenceDAO.getByEvent(idEvent);
    }

    // Compter les présents pour un événement
    public int compterPresents(int idEvent) throws SQLException {
        return presenceDAO.compterPresents(idEvent);
    }

    // Calculer taux de présence
    public double calculerTauxPresence(int idEvent, int totalInscrits) throws SQLException {
        int presents = compterPresents(idEvent);
        if (totalInscrits == 0) return 0;
        return (double) presents / totalInscrits * 100;
    }
}
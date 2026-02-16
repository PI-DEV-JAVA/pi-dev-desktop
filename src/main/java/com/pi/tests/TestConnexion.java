package com.pi.tests;

import com.pi.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class TestConnexion {
    public static void main(String[] args) {
        System.out.println("🔍 Test de connexion à la base de données...\n");


        Connection conn = DatabaseConnection.connect();


        if (conn != null) {
            System.out.println("\n SUCCÈS !");
            System.out.println("   Connexion établie avec :");
            System.out.println("   - Base : projet_pi");
            System.out.println("   - URL : jdbc:mysql://localhost:3306/projet_pi");
            System.out.println("   - User : root");


            try {
                if (!conn.isClosed()) {
                    System.out.println("   - Statut : Connexion ouverte");
                }
            } catch (SQLException e) {
                System.out.println("   - Erreur lors de la vérification");
            }


            try {
                conn.close();
                System.out.println("   - Connexion fermée proprement");
            } catch (SQLException e) {
                System.out.println("   - Erreur lors de la fermeture");
            }

        } else {
            System.out.println("\n ÉCHEC !");
            System.out.println("   Vérifie que :");
            System.out.println("   1. XAMPP est lancé (MySQL démarré)");
            System.out.println("   2. La base 'projet_pi' existe");
            System.out.println("   3. Le port 3306 n'est pas bloqué");
            System.out.println("   4. Les identifiants sont corrects (root sans mot de passe)");
        }
    }
}
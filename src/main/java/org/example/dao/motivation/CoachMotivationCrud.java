package org.example.dao.motivation;

import org.example.model.motivation.CoachMotivation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoachMotivationCrud implements CrudCoach<CoachMotivation> {

    private Connection cnx = DatabaseConnection.getConnection();

    // CREATE - MODIFIÉ pour inclure email
    @Override
    public void create(CoachMotivation c) {
        // MODIFIÉ : ajout de email dans la requête
        String sql = "INSERT INTO coach_motivation (nom_coach, email, style, description, actif) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getNomCoach());
            ps.setString(2, c.getEmail());        // NOUVEAU : index 2
            ps.setString(3, c.getStyle());         // maintenant index 3
            ps.setString(4, c.getDescription());   // maintenant index 4
            ps.setBoolean(5, c.isActif());         // maintenant index 5
            ps.executeUpdate();
            System.out.println("Coach ajouté avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ - MODIFIÉ pour inclure email
    @Override
    public List<CoachMotivation> readAll() {
        List<CoachMotivation> list = new ArrayList<>();
        String sql = "SELECT * FROM coach_motivation";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                CoachMotivation c = new CoachMotivation();
                c.setIdCoach(rs.getInt("id_coach"));
                c.setNomCoach(rs.getString("nom_coach"));
                c.setEmail(rs.getString("email"));        // NOUVEAU
                c.setStyle(rs.getString("style"));
                c.setDescription(rs.getString("description"));
                c.setActif(rs.getBoolean("actif"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // UPDATE - MODIFIÉ pour inclure email
    @Override
    public void update(CoachMotivation c) {
        // MODIFIÉ : ajout de email dans la requête
        String sql = "UPDATE coach_motivation SET nom_coach=?, email=?, style=?, description=?, actif=? WHERE id_coach=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getNomCoach());
            ps.setString(2, c.getEmail());        // NOUVEAU : index 2
            ps.setString(3, c.getStyle());         // maintenant index 3
            ps.setString(4, c.getDescription());   // maintenant index 4
            ps.setBoolean(5, c.isActif());         // maintenant index 5
            ps.setInt(6, c.getIdCoach());          // maintenant index 6
            ps.executeUpdate();
            System.out.println("Coach mis à jour avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE (soft) - PAS DE MODIFICATION NÉCESSAIRE
    @Override
    public void delete(int id) {
        String sql = "UPDATE coach_motivation SET actif = 0 WHERE id_coach = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Coach ID " + id + " supprimé (soft delete).");
            } else {
                System.out.println("Aucun coach trouvé avec l'ID " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE réel - PAS DE MODIFICATION NÉCESSAIRE
    @Override
    public void delete_reel(int id) {
        String sql = "DELETE FROM coach_motivation WHERE id_coach = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Coach ID " + id + " supprimé de la base !");
            } else {
                System.out.println("Aucun coach trouvé avec l'ID " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // NOUVELLE MÉTHODE OPTIONNELLE : Rechercher par email
    public CoachMotivation findByEmail(String email) {
        String sql = "SELECT * FROM coach_motivation WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                CoachMotivation c = new CoachMotivation();
                c.setIdCoach(rs.getInt("id_coach"));
                c.setNomCoach(rs.getString("nom_coach"));
                c.setEmail(rs.getString("email"));
                c.setStyle(rs.getString("style"));
                c.setDescription(rs.getString("description"));
                c.setActif(rs.getBoolean("actif"));
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
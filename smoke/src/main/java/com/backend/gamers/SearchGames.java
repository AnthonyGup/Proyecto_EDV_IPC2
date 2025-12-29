package com.backend.gamers;

import com.backend.db.DBConnection;
import com.backend.entities.Videogame;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author antho
 */
public class SearchGames {

    /**
     * Búsqueda multi-filtro de videojuegos. Todos los parámetros son opcionales.
     */
    public List<Videogame> searchByFilters(
            String name,
            Boolean available,
            Double minPrice,
            Double maxPrice,
            Integer companyId,
            Integer categoryId,
            Integer maxAge
    ) throws SQLException {
        List<Videogame> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT v.* FROM videogame v ");
        List<Object> params = new ArrayList<>();

        // Filtro por categoría mediante EXISTS sobre tabla de relación
        if (categoryId != null) {
            sql.append("WHERE EXISTS (SELECT 1 FROM videogameCategory vc WHERE vc.game_id = v.videogame_id AND vc.category_id = ?) ");
            params.add(categoryId);
        } else {
            sql.append("WHERE 1=1 ");
        }

        if (name != null && !name.isBlank()) {
            sql.append("AND LOWER(v.name) LIKE ? ");
            params.add("%" + name.toLowerCase() + "%");
        }
        if (available != null) {
            sql.append("AND v.available = ? ");
            params.add(available);
        }
        if (minPrice != null) {
            sql.append("AND v.price >= ? ");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append("AND v.price <= ? ");
            params.add(maxPrice);
        }
        if (companyId != null) {
            sql.append("AND v.company_id = ? ");
            params.add(companyId);
        }
        if (maxAge != null) {
            sql.append("AND v.age_restriction <= ? ");
            params.add(maxAge);
        }

        sql.append("ORDER BY v.name ASC");

        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                int idx = i + 1;
                if (p instanceof Integer) ps.setInt(idx, (Integer) p);
                else if (p instanceof Double) ps.setDouble(idx, (Double) p);
                else if (p instanceof Boolean) ps.setBoolean(idx, (Boolean) p);
                else if (p instanceof String) ps.setString(idx, (String) p);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Videogame game = new Videogame();
                    game.setVideogameId(rs.getInt("videogame_id"));
                    game.setName(rs.getString("name"));
                    game.setDescription(rs.getString("description"));
                    Date rel = rs.getDate("release_date");
                    game.setRelasedate(rel != null ? rel.toLocalDate() : null);
                    game.setMinimRequirements(rs.getString("min_requirements"));
                    game.setPrice(rs.getDouble("price"));
                    game.setAvailable(rs.getBoolean("available"));
                    game.setAgeRestriction(rs.getInt("age_restriction"));
                    game.setCompanyId(rs.getInt("company_id"));
                    results.add(game);
                }
            }
        }

        return results;
    }

    /**
     * Compatibilidad con la API anterior: búsqueda por un único tipo/valor.
     * Internamente delega a {@link #searchByFilters} con el filtro correspondiente.
     */
    public List<Videogame> search(String type, String value) throws SQLException {
        switch (type == null ? "" : type.toLowerCase()) {
            case "name":
                return searchByFilters(value, null, null, null, null, null, null);
            case "category":
                Integer catId = tryParseInt(value);
                return searchByFilters(null, null, null, null, null, catId, null);
            case "price":
                Double price = tryParseDouble(value);
                return searchByFilters(null, null, price, price, null, null, null);
            case "company":
                Integer compId = tryParseInt(value);
                return searchByFilters(null, null, null, null, compId, null, null);
            default:
                return new ArrayList<>();
        }
    }

    private static Integer tryParseInt(String s) {
        try { return s != null ? Integer.parseInt(s) : null; } catch (Exception e) { return null; }
    }
    private static Double tryParseDouble(String s) {
        try { return s != null ? Double.parseDouble(s) : null; } catch (Exception e) { return null; }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Company;
import com.backend.exceptions.AlreadyExistException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author antho
 */
public class CompanyDao extends Crud<Company> {

    public CompanyDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(Company entidad) throws SQLException, AlreadyExistException {
        String sql = "INSERT INTO "+tabla+" (description, name, commission) VALUES (?,?,?)";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        if (readByColumn(entidad.getName(), "name") == null) {
            stmt.setString(1, entidad.getDescription());
            stmt.setString(2, entidad.getName());
            stmt.setDouble(3, entidad.getCommission());
        }
        
        if(stmt.executeUpdate() == 0) {
            throw new AlreadyExistException();
        }
        
    }

    @Override
    public Company obtenerEntidad(ResultSet rs) throws SQLException {
        Company company = new Company();
        
        company.setCompanyId(rs.getInt("company_id"));
        company.setDescription(rs.getString("description"));
        company.setName(rs.getString("name"));
        company.setCommission(rs.getDouble("commission"));
        
        return company;
    }
    
    public List<Company> searchByName(String query) throws SQLException {
        String sql = "SELECT * FROM " + tabla + " WHERE LOWER(name) LIKE ?";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, "%" + query.toLowerCase() + "%");
        ResultSet rs = stmt.executeQuery();
        List<Company> results = new ArrayList<>();
        while (rs.next()) {
            results.add(obtenerEntidad(rs));
        }
        return results;
    }
    
}

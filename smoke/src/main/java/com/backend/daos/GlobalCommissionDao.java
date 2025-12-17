/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.GlobalCommission;
import com.backend.exceptions.AlreadyExistException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class GlobalCommissionDao extends Crud<GlobalCommission> {

    public GlobalCommissionDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(GlobalCommission entidad) throws SQLException, AlreadyExistException {
        //Esta tabla no crea registros nuevos, solo actualiza uno ya existente
        update(""+ entidad.getGCId(), "commission", entidad.getCommission());
                
    }

    @Override
    public GlobalCommission obtenerEntidad(ResultSet rs) throws SQLException {
        GlobalCommission com = new GlobalCommission();
        
        com.setGCId(rs.getInt("id"));
        com.setCommission(rs.getDouble("commission"));
        
        return com;
    }
    
}

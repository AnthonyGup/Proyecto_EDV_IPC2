/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.crud;

import com.backend.db.DBConnection;
import com.backend.exceptions.AlreadyExistException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author antho
 */
public abstract class Crud<T> {

    protected final String tabla;
    //codigo se refiere al nombre de la columna que contiene las pk
    protected final String codigo;
    protected final Connection CONNECTION;

    public Crud(String tabla, String codigo) {
        this.CONNECTION = DBConnection.getInstance().getConnection();
        this.tabla = tabla;
        this.codigo = codigo;
    }

    /**
     * Metodo que crea un nuevo dato sobre la base de datos, se utiliza en su
     * respectivo DAO
     *
     * @param entidad es el dato que se quiere guardar con el nuevo registro
     * creado
     * @throws java.sql.SQLException
     * @throws com.backend.exceptions.AlreadyExistException
     */
    public abstract void create(T entidad) throws SQLException, AlreadyExistException;

    /**
     * Metodo que lee un dato de la base de datos, se implementa en su
     * respectiva DAO
     *
     * @param id la identificacion por la cual se va a encontrar el registro
     * buscado
     * @return el objeto ya creado y leido de la base de datos
     * @throws java.sql.SQLException
     */
    public T readByPk(String id) throws SQLException {
        String sql = "SELECT * FROM " + tabla + " WHERE " + codigo + " = ?";

        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return obtenerEntidad(rs);
        }

        return null;
    }

    /**
     *
     * @param id valor por el cual se va a buscar los datos
     * @param columna la columna en la cual se va a buscar el valor
     * @return una entidad creada dependiendo de lo puesto en cada clase
     * @throws java.sql.SQLException
     */
    public T readByColumn(String id, String columna) throws SQLException {
        String sql = "SELECT * FROM " + tabla + " WHERE " + columna + " = ?";

        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return obtenerEntidad(rs);
        }

        return null;
    }

    /**
     * Metodo que lee una tabla entera de la base de datos
     *
     * @return un arreglo de todos los datos encontrados en la tabla
     * especificada
     * @throws java.sql.SQLException
     */
    public List<T> readAll() throws SQLException {
        List<T> entidades = new ArrayList<>();
        String sql = "SELECT * FROM " + tabla;

        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            entidades.add(obtenerEntidad(rs));
        }

        return entidades;
    }

    /**
     * Funcion que actualiza un dato de la base de datos basada en su primary
     * key (logica extra en cada implementacion)
     *
     * @param id identificacion del registro que queremos actualizar (pk)
     * @param columna columna que se quier actualizar
     * @param valor el valor que se va a actualizar
     * @return true si se logra actualizar correctamente, fase si no
     * @throws java.sql.SQLException
     */
    public boolean update(String id, String columna, Object valor) throws SQLException {
        String sql = "UPDATE " + tabla + " SET " + columna + " = ? WHERE " + codigo + " = ?";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        if (valor instanceof Integer) {
            int entero = (int) valor;
            stmt.setInt(1, entero);
        } else if (valor instanceof String) {
            String string = (String) valor;
            stmt.setString(1, string);
        } else if (valor instanceof Double) {
            double doble = (double) valor;
            stmt.setDouble(1, doble);
        } else if (valor instanceof LocalDate) {
            LocalDate fecha = (LocalDate) valor;
            stmt.setDate(1, Date.valueOf(fecha));
        } else if (valor instanceof LocalTime) {
            LocalTime time = (LocalTime) valor;
            stmt.setTime(1, Time.valueOf(time));
        } else if (valor instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) valor;
            stmt.setTimestamp(1, Timestamp.valueOf(dateTime));
        } else if (valor instanceof Boolean) {
            boolean boleano = (boolean) valor;
            stmt.setBoolean(1, boleano);
        }

        stmt.setString(2, id);

        return stmt.executeUpdate() > 0;
    }

    /**
     * Metodo que elimina un registro de la base de datos, mediante su primary
     * key
     *
     * @param id identificacion del registro que queremos eliminar
     * @return true si se elimina con exito, false si no
     * @throws java.sql.SQLException
     */
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM " + tabla + " WHERE " + codigo + " = ?";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, id);

        int filasEliminadas = stmt.executeUpdate();
        return filasEliminadas > 0;
    }

    public boolean deleteComposed(String id1, String id2, String columna1, String columna2) throws SQLException {
        String sql = "DELETE FROM " + tabla + " WHERE " + columna1 + " = ? AND " + columna2 + " = ?";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, id1);
        stmt.setString(2, id2);
        
        int filasEliminadas = stmt.executeUpdate();
        return filasEliminadas > 0;
    }

    /**
     * Metodo que devuelve la entidad buscada solo esa entidad
     *
     * @param rs el resultset creado
     * @return la entidad
     * @throws SQLException
     */
    public abstract T obtenerEntidad(ResultSet rs) throws SQLException;
}

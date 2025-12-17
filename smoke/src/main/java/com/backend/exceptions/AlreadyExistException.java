/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.exceptions;

/**
 *
 * @author antho
 */
public class AlreadyExistException extends Exception {

    public AlreadyExistException() {
        super("Este elemento ya existe");
    }
    
}

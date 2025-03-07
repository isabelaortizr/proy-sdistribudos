package org.example;

import BD.Database;

public class Main {
    public static void main(String[] args) {
        Database db = Database.getInstance();
        if (db.getConnection() != null) {
            System.out.println("Conexión exitosa!");
        } else {
            System.out.println("Error en la conexión.");
        }
        db.closeConnection();
    }
}

package org.example;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom {

    private final String name;
    private final String password;
    private final List<Conexion> customers = new ArrayList<>();

    //inicializado
    public ChatRoom(String name, String password) {

        this.name = name;
        this.password = password;
    }

    public boolean PasswordCorrecto(String inputpassword) {
        return password == null || password.equals(inputpassword);
    }
    //metodo para añadir cliente
    public synchronized void addCustomer(Conexion customer) {
        customers.add(customer);
        broadcast(customer.getUsername() + "se ha añadido al chat");
    }
    //metodo para eliminar cliente
    public synchronized void DeleteCustomer(Conexion customer) {
        customers.remove(customer);
        broadcast(customer.getUsername() + "se ha eliminado del chat");
    }
    //para enviar mensaje
    public synchronized void broadcast(String message) {
        for (Conexion client : customers ) {
            client.sendMessage(message);
        }
    }


}


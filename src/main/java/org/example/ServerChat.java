package org.example;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerChat {

    private static final int PORT = 8080;
    private static final Map<String, ChatRoom> chatRooms = new HashMap<>();
    private  final Map<Conexion, PublicKey> clientKeys = new HashMap<>();

    public void iniciar() {

        System.out.println("Servidor de chat iniciado en el puerto " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Conexion(clientSocket, this).start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    public synchronized void registroClienteKey(Conexion conexion, PublicKey publicKey) {
        clientKeys.put(conexion, publicKey);
    }

    public synchronized PublicKey getClienteKey(Conexion conexion) {
        return clientKeys.get(conexion);
    }

    public synchronized void createRoom(String roomName, String password,  DataOutputStream out ) throws IOException {
        String ID = roomName  + "#" + UUID.randomUUID().toString().substring(0,8);
        if (chatRooms.containsKey(ID)) {
            out.writeUTF("La sala ya existe.");
        } else {
            ChatRoom room = new ChatRoom(ID, password);
            chatRooms.put(ID, room);
            out.writeUTF("Sala creada: " + ID);
        }
    }

    public synchronized void joinRoom(String roomName,  String password, Conexion clientConnection,  DataOutputStream out) throws IOException {
        ChatRoom room = null;
        for (String key : chatRooms.keySet()) {
            if (key.startsWith(roomName + "#")) {
                room = chatRooms.get(key);
                break;
            }
        }

        if (room == null) {
            out.writeUTF("La sala no existe.");
        } else if (!room.PasswordCorrecto(password)) {
            out.writeUTF("contraseña incorrecto");
        } else {
            room.addCustomer(clientConnection);
            clientConnection.setCurrentRoom(room);
            out.writeUTF("se ha Unido a la sala: " + roomName);
        }
    }

    public synchronized void listRooms(DataOutputStream out) throws IOException {
        if (chatRooms.isEmpty()) {
            out.writeUTF("No hay salas disponibles.");
        } else {
            out.writeUTF("Salas disponibles:");
            for (String roomName : chatRooms.keySet()) {
                out.writeUTF(roomName);
            }
        }
    }

    }




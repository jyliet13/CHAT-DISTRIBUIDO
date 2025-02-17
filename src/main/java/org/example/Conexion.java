package org.example;

import javax.crypto.Cipher;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class Conexion  extends Thread {
    private final Socket socket;
    private final ServerChat server;
    private String username;
    private ChatRoom currentRoom;
    private DataInputStream in;
    private DataOutputStream out;
    private PrivateKey privateKey;
    private PublicKey publicKey;


    public Conexion(Socket socket, ServerChat server) {
        this.socket = socket;
        this.server = server;
        generateKeyPair();
    }

    public void run() {
        try {

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF("Bienvenido al chat! Ingresa tu nombre de usuario:");
            username = in.readUTF();
            System.out.println(username + " se ha conectado.");

            //cmenu comandos cliente

            out.writeUTF("Usuario: " + username + "eliga lo que quiera a traves de comandos\n   ");
            out.writeUTF(" crear <nombreSala> [contraseña]: crear una nueva sala");
            out.writeUTF(" unirse <nombreSala> [contraseña]: unirse a la sala");
            out.writeUTF("listar: muestra todas las salas");
            out.writeUTF(" message <texto>: enviar mensaje en la sala");
            out.writeUTF(" disconnect: desconectarse del chat \n");


            String command;
            while ((command = in.readUTF()) != null) {
                if (command.startsWith("crear")) {
                    String[] parts = command.split(" ", 3);
                    if (parts.length < 2) {
                        out.writeUTF("Uso: crear <nombreSala> [contraseña]");
                    } else {
                        String password = parts.length == 3 ? parts[2] : null;
                        server.createRoom(parts[1], password, out);
                    }
                } else if (command.startsWith("unirse")) {
                    String[] parts = command.split(" ", 3);
                    if (parts.length < 2) {
                        out.writeUTF("Uso: unise <nombreSala> [coontraseña]");
                    } else {
                        String password = parts.length == 3 ? parts[2] : null;
                        server.joinRoom(parts[1], password, this, out);
                    }
                } else if (command.startsWith("listar")) {
                    server.listRooms(out);
                } else if (command.startsWith("message")) {
                    if (currentRoom != null) {
                        String message = command.substring(9).trim();
                        if (message.length() > 140) {
                            message = message.substring(0, 140);
                        }
                        currentRoom.broadcast(username + ": " + message);
                    } else {
                        out.writeUTF("No estás en ninguna sala. Únete a una sala primero.");
                    }
                } else if (command.equals("disconnect")) {
                    disconnect();
                    break;
                } else {
                    out.writeUTF("Comando no reconocido.");
                }
            }

        } catch (IOException e) {
            System.out.println("error en la conexion del cliente" + e.getMessage());
        }
    }

    private void disconnect() {
        try {
            if (currentRoom != null) {
                currentRoom.DeleteCustomer(this);

            }
            socket.close();
            System.out.println(username + " se ha desconectado.");
        } catch (IOException e) {
            System.out.println("error al desconectar cliente" + e.getMessage());
        }

    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            System.out.println( " se ha generado el private key" + Base64.getEncoder().encodeToString(this.privateKey.getEncoded()));
            System.out.println(  " se ha generado el pUBLICK key" + Base64.getEncoder().encodeToString(this.publicKey.getEncoded()));
        } catch (Exception e) {
            System.out.println("error al generar las claves RSA" + e.getMessage());
        }
    }

    private String CifradoMessage(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherText = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }


    public String getUsername() {
        return username;
    }

    public void setCurrentRoom(ChatRoom currentRoom) {
        this.currentRoom = currentRoom;
    }

    public void sendMessage(String message) {
        try {
            PublicKey clientePublicKey = server.getClienteKey(this);
            if (clientePublicKey != null) {
                String cifradoMessage = CifradoMessage(message, clientePublicKey);
                System.out.println( "{MENSAJE CIFRADO }" + cifradoMessage);
                out.writeUTF(cifradoMessage);

            }else {
                out.writeUTF(message);
            }
        } catch (Exception e) {
            System.out.println("error en enviar mensaje (sendMessage)" + e.getMessage());
            e.printStackTrace();
        }
    }
}


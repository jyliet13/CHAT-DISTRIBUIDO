package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    private static final String host = "";
    private static final int Port = 8080;

    public Cliente() {
        try {
            Socket socket = new Socket(host, Port);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            System.out.println("Conectado al servidor chat");
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readUTF()) != null) {
                        System.out.println(msg);
                    }
                }catch (Exception e) {
                    System.out.println("No se puede conectar al servidor");
                }
            }).start();

            while (true) {
                Scanner sc = new Scanner(System.in);

                String userInput =  sc.nextLine();
                out.writeUTF(userInput);
                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.println("desconectado del chat");
                    break;
                }
            }
        }catch (IOException e) {
            System.out.println("No se puede conectar al servidor " + e.getMessage() );
        }
    }

}

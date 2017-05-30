/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import model.PomiarTemperatura;
import model.ProducentPomiarow;
import view.UrzadzeniaForm1;

/**
 *
 * @author Ulka
 */
public class SocketServer extends Thread {

    private ServerSocket serverSocket;
    private final ProducentPomiarow producent;
    private boolean running = true;

    public SocketServer(int port, boolean running) throws IOException {
        serverSocket = new ServerSocket(port);
        //serverSocket.setSoTimeout(10000);
        this.running = running;
        producent = new ProducentPomiarow();
    }

    public void run() {
        while (running) {
            try {
                System.out.println("Waiting for client on port "
                        + serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();

                System.out.println("Just connected to " + server.getRemoteSocketAddress());
                DataInputStream in = new DataInputStream(server.getInputStream());

                String read = in.readUTF();
                System.out.println(read);
                readData(read);

                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeUTF("Thank you for sending info to " + server.getLocalSocketAddress());
                //server.close();

            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void readData(String readUTF) {
        //odczyt temperatury
        System.out.println("Server wczytuje zbiór danych pomiarowych...");
        String[] zbior = readUTF.split("#");
        for (int i = 0; i < zbior.length; i++) {
            System.out.println("Wczytuję pomiar " + i + "...");
            String[] pomiar = zbior[i].split(";");
            int id = Integer.parseInt(pomiar[1]);
            if (zbior[i].startsWith("T")) {
                BigDecimal wynik = new BigDecimal(pomiar[2]);
                producent.newPomiarTemp(id, wynik);
            } else if (zbior[i].startsWith("G")){
                BigDecimal wynikNap = new BigDecimal(pomiar[2]);
                BigDecimal wynikPrad = new BigDecimal(pomiar[3]);
                BigDecimal wynikMoc = new BigDecimal(pomiar[4]);
                producent.newPomiarGniazdko(id, wynikNap, wynikPrad, wynikMoc);
            }
            else {
                System.out.println("Nieprawidłowy zapis pomiaru");
            }
        }

        //odczyt gniazdek
    }
}

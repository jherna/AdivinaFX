package uf5.mp3.adivinafx.net;

import uf5.mp3.adivinafx.model.EstatJoc;
import uf5.mp3.adivinafx.model.Jugada;
import uf5.mp3.adivinafx.model.SecretNum;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * Created by jordi on 26/02/17.
 * Exemple Servidor UDP extret dels apunts IOC i ampliat
 * El seu CLient és DatagramSocketClient
 */
public class DatagramSocketServer {
    private DatagramSocket socket;
    private SecretNum ns;
    private int fi;
    private boolean acabat;
    public EstatJoc estatJoc;

    //Instàciar el socket
    public void init(int port) throws SocketException {
        socket = new DatagramSocket(port);
        ns = new SecretNum(100);
        acabat = false;
        fi=-1;
        System.out.printf("Servidor obert pel port %d%n",port);
        estatJoc = new EstatJoc();
    }

    public void runServer() throws IOException {
        byte [] receivingData = new byte[1024];
        byte [] sendingData;
        InetAddress clientIP;
        int clientPort;

        while(!acabat) {
            DatagramPacket packet = new DatagramPacket(receivingData,1024);
            socket.receive(packet);
            sendingData = processData(packet.getData(),packet.getLength());
            //Llegim el port i l'adreça del client per on se li ha d'enviar la resposta
            clientIP = packet.getAddress();
            clientPort = packet.getPort();
            packet = new DatagramPacket(sendingData,sendingData.length,clientIP,clientPort);
            socket.send(packet);
        }
    }

    //El server retorna al client el mateix missatge que li arriba però en majúscules
    public byte[] processData(byte[] data, int lenght) {
        Jugada jugada;
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
            jugada = (Jugada) ois.readObject();
            updateEstatJoc(jugada);
            System.out.println(jugada);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        fi = ns.comprova(jugada.getTirada());
        if(fi==0) {
            acabat=true;
        }
        estatJoc.setResponse(ns.comprova(String.valueOf(jugada.getTirada())));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(os);
            oos.writeObject(estatJoc);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return os.toByteArray();
    }

    private void updateEstatJoc(Jugada jugada) {
        if(estatJoc.jugadors.containsKey(jugada.getNom())) { //Si existeix el jugador
            estatJoc.jugadors.put(jugada.getNom(),estatJoc.jugadors.get(jugada.getNom())+1);
        } else {
            estatJoc.jugadors.put(jugada.getNom(),1);
        }
    }


}

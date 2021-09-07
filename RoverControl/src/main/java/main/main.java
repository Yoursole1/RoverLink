package main;

import main.API.APImanager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class main {
    public static void main(String[] inputs){

        responseServer server = new responseServer();
        server.start();
    }

    public static void startTimer(){ //This function manages the keepalive packet system between the library and the rover.  It sends a packet containing the data
        //"keepalive" to the library and if it responds within a second with "keepalive" then the connection is sustained...otherwise the rover removes the connected 
        //library from the ConnectedUsers list
        
        
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final InetAddress[] a = new InetAddress[1];
                ExecutorService executor = Executors.newCachedThreadPool();
                Callable<Object> task = () -> {
                    for(InetAddress address : APImanager.connectedUsers){
                        a[0] = address;
                        try {
                            return keepAlive(address);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                    return false;
                };
                Future<Object> future = executor.submit(task);
                try {
                    Object result = future.get(1000, TimeUnit.MILLISECONDS);
                } catch (TimeoutException ex) {
                    APImanager.connectedUsers.remove(a[0]);
                    System.out.println(a[0] + " disconnected");
                } catch (InterruptedException | ExecutionException ignored) {
                } finally {
                    future.cancel(true);
                }


            }
        }, 0, 2000);
    }

    private static boolean keepAlive(InetAddress address) throws IOException, InterruptedException { //sends a packet and expects a return...the function above (startTimer)
        //handles timeout issues (as this can be treated as blocking because the socket#recieve blocks this function thus blocking the keepalive stuff)
        byte[] bytes;
        bytes = "keepalive".getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 7777);
        DatagramSocket socket = new DatagramSocket();
        socket.connect(address,7777);
        socket.send(packet);
        bytes = new byte[8192];
        packet = new DatagramPacket(bytes, bytes.length);
        socket.close();
        responseServer.ds.close();

        try{
            socket = new DatagramSocket(7777);
        }catch(SocketException e){

        }


        socket.receive(packet);
        String recieved = new String(packet.getData(),0, packet.getLength());
        socket.close();
        responseServer.ds = new DatagramSocket(7777);
        return recieved.equals("keepalive");

    }
}


class responseServer extends Thread{
    //This recieves packets that get sent to the APIManager class for parsing.  Its just a standerd UDP server - nothing special here
    public static DatagramSocket ds;
    public void run(){

        try {
            ds= new DatagramSocket(7777);
        } catch (SocketException e){
            e.printStackTrace();
        }
        byte[] receive = new byte[8192];
        DatagramPacket DpReceive;
        //SERVER HERE
        while(true){
            DpReceive = new DatagramPacket(receive, receive.length);
            try {
                ds.receive(DpReceive);
            } catch (IOException exception){
            }
            //OPERATION
            String request = getAsString(receive).toString();
            try {
                APImanager.respond(request,DpReceive.getAddress());
            } catch (IOException | InterruptedException exception) {
                exception.printStackTrace();
            }
            //OPERATION
            receive = new byte[8192];
            //65535
        }
        //SERVER HERE
    }
    //Just constructs a string from a byte[]
    private static StringBuilder getAsString(byte[] a){
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }


}

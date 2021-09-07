package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class RoverLink {

    private static InetAddress address;
    private static ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

    /**
     * The ideal location of the rover is the location that it after all queued commands are
     * executed and the rover has stopped.  It is NOT the current location of the rover unless the rover
     * is sitting still and has no commands queued
     */
    private static double[] idealLoc = {0,0}; //x,y location the rover is currently at.  (0,0 is the starting location)
    private static double idealRotation = 0; //rotation the rover is at measured clockwise(0 degrees is the starting rotation)


    public RoverLink(InetAddress ip, String validation) throws IOException {
        if(validate(validation,ip)){
            address = ip;
            responseServer server = new responseServer();
            server.start();

        }else{
            System.out.println("Invalid RoverLink Login Information");
        }
    }

    /**
     * Units in Centimeters
     * positive x is forward and negative x is backwards
     * @param x
     * positive y is right and negative y is left
     * @param y
     *
     * @param xFirst
     * if xfirst
     * x is executed first, then y.
     * Example: x = 10, y = 5
     * the rover would go 10 centimeters forward then it would go right 5 centimeters.
     *if !xfirst
     * y is executed first, then x
     */
    public void moveRover(double x, double y, boolean xFirst) throws IOException {
        if(xFirst)
            sendPacket("MOVEX("+x+","+y+")",address);
        else
            sendPacket("MOVEY("+x+","+y+")",address);

        idealLoc[0]+=x;
        idealLoc[1]+=y;

        /*
        im so sorry for someone trying to figure out what this does
        Its basically this

        if(xFirst){
            if(y>0){
                idealRotation+=90;
            }else{
                if(y==0){
                    idealRotation+=0;
                }else{
                    idealRotation+=-90;
                }
            }
        }else{
            idealRotation+=0;
        }
         */
        idealRotation+=(xFirst)?(y>0?90:(y==0)?0:-90):(0);
        //add if not xFirst for y first (did this on rover side just copy from there)


        idealRotation-=(idealRotation>=360)?360:0;
        /*
        if(xFirst)
        moveRover(10,5)
        packet sent:
            MOVEX(10,5)

        if(yFirst)
        moveRover(10,5)
        packet sent:
            MOVEY(10,5)
         */
    }


    /**
     *  reversePath choses if the rover will just follow a strait path back to where it started or if it will
     *  follow the path it took to the location it is at in reverse.
     *
     *  reversePath should be true if there are many obstacles in the area the rover is working in
     *  it should be false if you want the rover to return home quickly and in a strait line, but risking
     *  hitting something that it missed before
     * @param reversePath
     */
    public void returnHome(boolean reversePath) throws IOException {
        if(reversePath){
            sendPacket("RETURNPATH",address);
        }else{
            sendPacket("RETURNVECTOR",address);
        }
        idealLoc[0]=0;
        idealLoc[1]=0;
    }


    /**
     * returns the InetAddress of the rover (private IP)
     * @return
     */
    public InetAddress getAddress(){
        return address;
    }

    /**
     * DONT TOUCH THIS - this is for the response server ONLY
     * LEAVE THIS ALONE
     * This sometimes returns the incorrect IP address but I handle this issue elsewhere.
     * Using this will most likely break your code at some point
     * @return
     */
    public static InetAddress getAddressStatic(){
        return address;
    }

    /**
     * register a class that implements the ActionListener interface.
     * The listener methods will be called when things happen
     * @param c
     */
    public void registerEvent(ActionListener c){
        listeners.add(c);
    }

    /**
     * returns an ArrayList of all registered ActionListeners
     * @return Action Listeners
     */
    public static ArrayList<ActionListener> getEvents(){
        return listeners;
    }


    /**
     * The ideal location of the rover is the location that it after all queued commands are
     * executed and the rover has stopped.  It is NOT the current location of the rover unless the rover
     * is sitting still and has no commands queued
     */
    public static double[] getIdealLoc(){
        return idealLoc;
    }


    /**
     * sends a packet to the rover (this code can be copied into a public method if you want to use it but only
     * do this is you know what you are doing)
     * @param msg
     * @param address
     * @throws IOException
     */
    private static void sendPacket(String msg, InetAddress address)throws IOException {
        byte[] bytes;
        bytes = msg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 7777);
        DatagramSocket socket = new DatagramSocket();
        socket.connect(address,7777);
        socket.send(packet);
    }

    /**
     * validates the constructor parameters (RoverAddress and Validation Key)
     * @param validation
     * @param address
     * @return
     * @throws IOException
     */
    private static boolean validate(String validation, InetAddress address) throws IOException {
        byte[] bytes = validation.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 7777);
        DatagramSocket socket = new DatagramSocket(7777);
        socket.send(packet);
        bytes = new byte[8192];
        packet = new DatagramPacket(bytes, bytes.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength());
        socket.close();
        return received.equals("success");
    }
}

class responseServer extends Thread {
    public void run() {
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(7777);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] receive = new byte[8192];
        DatagramPacket DpReceive;
        //SERVER HERE
        while (true) {
            DpReceive = new DatagramPacket(receive, receive.length);
            try {
                ds.receive(DpReceive);
            } catch (IOException exception) {
            }
            //OPERATION

            if (DpReceive.getAddress().equals(RoverLink.getAddressStatic())) {
                String request = getAsString(receive).toString();

                if(request.equals("keepalive")){ //KEEPALIVE PACKET EXCHANGE
                    try {
                        sendPacket("keepalive",RoverLink.getAddressStatic());
                    } catch (IOException exception) {}


                }else if(request.equals("started")){ //ROVER STARTED
                    //call listeners

                    for(ActionListener chatListener : RoverLink.getEvents()){
                        chatListener.onRoverStart();
                    }
                }else if(request.equals("stopped")){ //ROVER STOPPED
                    for(ActionListener chatListener : RoverLink.getEvents()){
                        chatListener.onRoverStop();
                    }
                }
            }


            //OPERATION
            receive = new byte[8192];
            //65535
        }
        //SERVER HERE
    }

    private static StringBuilder getAsString(byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

    private static void sendPacket(String msg, InetAddress address)throws IOException {
        byte[] bytes;
        bytes = msg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 7777);
        DatagramSocket socket = new DatagramSocket();
        socket.connect(address,7777);
        socket.send(packet);
    }
}

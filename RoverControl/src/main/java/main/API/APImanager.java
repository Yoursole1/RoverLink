package main.API;

import com.pi4j.component.motor.impl.GpioStepperMotorComponent;
import main.utils.Actions;
import main.utils.Movement;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


import static main.main.startTimer;

public class APImanager {
    public static ArrayList<InetAddress> connectedUsers = new ArrayList<>();


    private static Actions actions = new Actions(5,12.7,20.28);


    public static void respond(String query, InetAddress address) throws IOException, InterruptedException {
        String q = query;
        if (q.equals("login") && !connectedUsers.contains(address)) {
            sendPacket("success", address);
            connectedUsers.add(address);
            System.out.println(address + " connected");

            Thread.sleep(500);
            startTimer();
        }else if(connectedUsers.contains(address)){
            System.out.println(q);
            if(q.contains("MOVE")){
                if(q.contains("MOVEX")){
                    double x;
                    double y;

                    final String[] locs = q.replace("MOVEX(", "").replace(")", "").split(",");
                    x = Double.parseDouble(locs[0]);
                    y = Double.parseDouble(locs[1]);


                    actions.moveRoverX(x,y);



                }else if(q.contains("MOVEY")){
                    double x;
                    double y;

                    final String[] locs = q.replace("MOVEY(", "").replace(")", "").split(",");
                    x = Double.parseDouble(locs[0]);
                    y = Double.parseDouble(locs[1]);

                    actions.moveRoverY(x,y);
                }
            }else if(q.equals("RETURNPATH")){
                actions.returnHomePath();
            }else if(q.equals("RETURNVECTOR")){
                actions.returnHomeVector();
            }
        }

    }
    private static void sendPacket(String msg, InetAddress address)throws IOException {
        byte[] bytes;
        bytes = msg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 7777);
        DatagramSocket socket = new DatagramSocket();
        socket.connect(address,7777);
        socket.send(packet);
        socket.close();
    }
}

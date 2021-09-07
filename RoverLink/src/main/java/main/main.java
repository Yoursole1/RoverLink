package main;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;

public class main {
    public static void main(String[] a) throws IOException {

        RoverLink link = new RoverLink(InetAddress.getByName("10.0.0.195"),"login");







        Scanner scan = new Scanner(System.in);
        for (int i = 0; i < 10; i++) {
            String in = scan.nextLine();

            link.moveRover(10,10,true);
        }





    }


}

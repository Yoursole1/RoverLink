//THIS IS AN EXAMPLE IMPLEMENTATION -- If you are planning on using this library please delete this file and compile the rest into a shaded jar

package main;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;

public class main {
    public static void main(String[] a) throws IOException {

        
        //testing ip
        RoverLink link = new RoverLink(InetAddress.getByName("15.0.1.69"),"login");







        Scanner scan = new Scanner(System.in);
        for (int i = 0; i < 10; i++) {
            String in = scan.nextLine();

            link.moveRover(10,10,true);
        }





    }


}

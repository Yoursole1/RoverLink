package main.utils;

import java.util.Arrays;
import java.util.Scanner;

public class Actions {

    private double wheelDiameter;
    private double width;
    private double length;
    private double wheelCircumference;
    private double pi = 3.14159265358979323;

    private double speed = 0.05; //meters/second

    private static double[] idealLoc = {0,0};
    private static double idealRotation = 0;


    /**
     * @param wheelDiameter Diameter of the wheels (cm)
     * @param width Measure from the back left wheel to the back right wheel, centered on both (cm)
     * @param length Measure from the axel of the back left wheel to the axel of the front left wheel (cm)
     */
    public Actions(double wheelDiameter, double width, double length){
        this.wheelDiameter = wheelDiameter;
        this.width = width;
        this.length = length;
        this.wheelCircumference = pi * wheelDiameter;
    }

    public void moveRoverX(double x, double y) throws InterruptedException {
        double[] wheelRotations = new double[4];
        int[] a = {0, 1, 2, 3};

        for (int i = 0; i < 4; i++) {
            wheelRotations[i] = (360*x)/wheelCircumference;
        }

        Movement.rotateWheel(a,wheelRotations);
        double timeToMoveMillisec = Math.abs(10*x/speed); //Due to the amazingness of math, this formula is like this due to a lot of canceling :D

        //this DOES stop the whole program.  The rover can't receive packets during this time (issue needs to be fixed)
        Thread.sleep((long) timeToMoveMillisec+500);


        if(y>0){
            rotateRover(90); //rotate left or right for positive or negative y
        }else{
            rotateRover(-90);
        }

        //Thread.sleep(5000); //soft code this later this is just 5000 for testing

        wheelRotations = new double[4];


        for (int i = 0; i < 4; i++) {
            wheelRotations[i] = (360*y)/wheelCircumference; //just add the movement distances to the control array
        }

        Movement.rotateWheel(a,wheelRotations);

        idealLoc[0]+=x;
        idealLoc[1]+=y;

        //I'm sorry I have explained this little bit of death code in the library in the moveRover function if you're curious what it does
        idealRotation+=(y>0?90:(y==0)?0:-90);
        idealRotation-=(idealRotation>=360)?360:0;





    }
    public void moveRoverY(double x, double y){ //NEEDS TO BE IMPLEMENTED 





        idealLoc[0]+=x;
        idealLoc[1]+=y;
        idealRotation+=(x<0)?180:0;
        idealRotation-=(idealRotation>=360)?360:0;
    }

    final int rotationMultiplier = 1;
    
    //This function scales using the angle and multiplying that by the rotationMultiplier.  I did it this way because I didnt want to figure out 
    //how to calculate how many wheel turns are needed to make the rover turn on different surfaces so just change the rotationMultiplier until the rover
    //turns the correct amount.  If it is underturning then make this number larger, and vice-versa
    public void rotateRover(double angle){ 
        System.out.println("rotating: "+angle);
        double[] angles = new double[4];
        int[] a = {0, 1, 2, 3};

        Arrays.fill(angles, angle);
        for (int i = 0; i < angles.length; i++) {
            angles[i] = angles[i]*rotationMultiplier;
        }

        angles[0] = (angle<0)?-angles[0]:angles[0]; //These make the wheels turn alternating to eachother 
        angles[1] = (angle<0)?-angles[1]:angles[1];
        angles[2] = (angle>0)?-angles[2]:angles[2];
        angles[3] = (angle>0)?-angles[3]:angles[3];

        Movement.rotateWheel(a,angles);


    }

    public void returnHomePath(){

    }

    /**
     * Rotate rover that puts it back to positive x seems to be acting up
     * @throws InterruptedException
     */
    public void returnHomeVector() throws InterruptedException {


        //This gets the amount the rover needs to turn to be able to to go back to 0,0 in a straight line.  Its just right triangle trig
        //with a little voodoo magic :D

        double v = (180+idealRotation)+(Math.atan(-idealLoc[1]/idealLoc[0]))*57.2957795130823210311;
        rotateRover((v>180)?360-v:-v);


        //delay for rotation ----------------------------------

        moveRoverX(Math.sqrt((idealLoc[0]*idealLoc[0])+(idealLoc[1]*idealLoc[1])),0);




    }


}

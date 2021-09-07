package main.utils;

import com.pi4j.component.motor.impl.GpioStepperMotorComponent;
import com.pi4j.io.gpio.*;
import com.pi4j.wiringpi.GpioUtil;

public class Movement {
    private double speed = 0.05; //meters/second


    /**
     * @param wheel clockwise from back left wheel (back left = 0, front left = 1, front right = 2, back right = 3)
     * @param deg angle measure to rotate the wheel clockwise, use a negative measure to rotate counterclockwise (degrees)
     */
    public static void rotateWheel(int wheel, double deg){

    }


    private static double[] degreesA;
    final static GpioController gpio = GpioFactory.getInstance();
    final static GpioPinDigitalOutput[] wheelA =  {
            //back left
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00),
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01),
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02),
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03),
    };
    final static GpioPinDigitalOutput[] wheelB =  {
            //front left
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04),
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05),
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06),
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07),
    };
    final static GpioPinDigitalOutput[] wheelC =  {
            //front right
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_08),
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_09),
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_10),
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11),
    };
    final static GpioPinDigitalOutput[] wheelD =  {
            //back right
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12),
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13),
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_14),
            gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15),
    };
    static GpioStepperMotorComponent motorA  = new GpioStepperMotorComponent(wheelA);
    static GpioStepperMotorComponent motorB  = new GpioStepperMotorComponent(wheelB);
    static GpioStepperMotorComponent motorC  = new GpioStepperMotorComponent(wheelC);
    static GpioStepperMotorComponent motorD  = new GpioStepperMotorComponent(wheelD);
    public static void rotateWheel(int[] wheels, double[] degrees){
        System.out.println("MOVE ROVER CALLED: " + degrees[0]);

        degreesA = degrees;
        GpioUtil.enableNonPrivilegedAccess();





        gpio.setShutdownOptions(true, PinState.LOW, wheelA);
        gpio.setShutdownOptions(true, PinState.LOW, wheelB);
        gpio.setShutdownOptions(true, PinState.LOW, wheelC);
        gpio.setShutdownOptions(true, PinState.LOW, wheelD);



        byte[] single_step_sequence = new byte[4];
        single_step_sequence[0] = (byte) 0b0001;
        single_step_sequence[1] = (byte) 0b0010;
        single_step_sequence[2] = (byte) 0b0100;
        single_step_sequence[3] = (byte) 0b1000;

        int oneRevolution = 2038;
        int halfRevolution = oneRevolution /2;
        int quarterRevolution = oneRevolution / 4;
        int oneDegreeRevolution = oneRevolution/360;
        motorA.setStepInterval(2);
        motorA.setStepSequence(single_step_sequence);
        motorB.setStepInterval(2);
        motorB.setStepSequence(single_step_sequence);
        motorC.setStepInterval(2);
        motorC.setStepSequence(single_step_sequence);
        motorD.setStepInterval(2);
        motorD.setStepSequence(single_step_sequence);

        motorA.setStepsPerRevolution(oneRevolution);
        motorB.setStepsPerRevolution(oneRevolution);
        motorC.setStepsPerRevolution(oneRevolution);
        motorD.setStepsPerRevolution(oneRevolution);







        motorA a = new motorA();
        a.start();
        motorB b = new motorB();
        b.start();
        motorC c = new motorC();
        c.start();
        motorD d = new motorD();
        d.start();




    }
    static class motorA extends Thread{
        public void run(){
            motorA.rotate(degreesA[0]/360);
        }
    }
    static class motorB extends Thread{
        public void run(){
            motorB.rotate(degreesA[1]/360);
        }
    }
    static class motorC extends Thread{
        public void run(){
            motorC.rotate(degreesA[2]/-360);
        }
    }
    static class motorD extends Thread{
        public void run(){
            motorD.rotate(degreesA[3]/-360);
        }
    }

}

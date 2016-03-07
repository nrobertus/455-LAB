/**
 * SimpleBot
 *
 * Copyright 2005 by RidgeSoft, LLC., PO Box 482, Pleasanton, CA  94566, U.S.A.
 * www.ridgesoft.com
 *
 * RidgeSoft grants you the right to use, modify, make derivative works and
 * redistribute this source file provided you do not remove this copyright notice.
 *
 * This class implements a simple robot contoller for a robot with
 * left and right wheels driven by continuous rotation servos.  The class is intended
 * to demonstrate basic Java programming techniques applied to robotics.
 *
 * The class includes methods to make the robot move forward, backward and stop.
 * The 'go' method makes the robot move forward for 5 seconds, stop for 5 seconds
 * and move backward for 5 seconds.
 *
 * The main method creates two Motor objects and a SimpleBot object.
 * The Motor objects are constructed by wrapping Servo objects in 
 * ContinuousRotationServo objects, which implement a Motor facade around a Servo
 * object.  These are passed as arguments when creating the SimpleBot object.
 * The Motor objects the main method creates are specific to the IntelliBrain and
 * control servos, but the SimpleBot object is unaware of this.  The SimpleBot class
 * could work equally well on a different robot controller using a different class
 * of Motor object provided the main method is modified accordingly.
 */

import com.ridgesoft.robotics.*;
import com.ridgesoft.intellibrain.*;
import com.ridgesoft.intellibrain.IntelliBrain;
import com.ridgesoft.io.Display;
import com.ridgesoft.intellibrain.IntelliBrain;
import javax.comm.SerialPort;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.String;

public class SimpleBot {
    private Motor mLeftMotor;			// The motor object controlling the left wheel
    private Motor mRightMotor;			// The motor object controlling the right wheel

    /**
     * Construct a SimpleBot object given left and right motor contoller objects.
     */
    public SimpleBot(Motor leftMotor, Motor rightMotor) {
        mLeftMotor = leftMotor;			// Initialize the left motor member variable
        mRightMotor = rightMotor;		// Initialize the right motor member variable
    }

    /**
     * Stop the robot by turning the power to both motors off.
     */
    public void stop() {
        mLeftMotor.setPower(Motor.STOP);			// Turn left motor power off
        mRightMotor.setPower(Motor.STOP);			// Turn right motor power off
    }

    /**
     * Make the robot go forward by turning both motors on at full power.
     */
    public void forward() {
        mLeftMotor.setPower(Motor.MAX_FORWARD);		// Turn left motor power on full
        mRightMotor.setPower(Motor.MAX_FORWARD);	// Turn right motor power on full
    }

    /**
     * Make the robot go backward by turning both motors on in reverse at
     * full power.
     */
    public void backward() {
        // Example of using the this reference explicitly
        this.mLeftMotor.setPower(Motor.MAX_REVERSE);	// Turn left motor power on full reverse
        this.mRightMotor.setPower(Motor.MAX_REVERSE);	// Turn right motor power on full reverse
    }

    /**
     * Make the robot turn left
     * full power.
     */
    public void left() {
        // Example of using the this reference explicitly
        this.mLeftMotor.setPower(Motor.MAX_FORWARD);	// Turn left motor power on full 
        this.mRightMotor.setPower(Motor.MAX_REVERSE);	// Turn right motor power on full reverse
    }

    /**
     * Make the robot turn right
     * full power.
     */
    public void right() {
        // Example of using the this reference explicitly
        this.mLeftMotor.setPower(Motor.MAX_REVERSE);	// Turn left motor power on full reverse
        this.mRightMotor.setPower(Motor.MAX_FORWARD);	// Turn right motor power on full
    }

    /**
     * Make the robot go.  This implementation makes the robot go forward
     * for 5 seconds, stop for 5 seconds then go backward for 5 seconds.
     * Subclasses can make the robot behave differently by overriding this
     * method.
     */
    public void go() throws InterruptedException {
		try
		{
		int x = 400;
		Display display = IntelliBrain.getLcdDisplay();
		SerialPort comPort = IntelliBrain.getCom1();
        comPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        InputStream inputStream = comPort.getInputStream();
		display.print(1, "");
		display.print(0, "CTRLS: W,A,S,D,Q");

		boolean running = true;
        while (running)
		{
			int d = inputStream.read();
        	if(d == 119){
				display.print(0, "FORWARD");
				forward();
			}else if(d == 115){
				display.print(0, "BACKWARD");
				backward();
			}else if(d == 97){
				display.print(0, "LEFT");
				left();
			}else if(d == 100){
				display.print(0, "RIGHT");
				right();
			}else if(d == 113){
				display.print(0, "DONE");
				running = false;
			}else if(d == 101){
				display.print(0, "STOP");
				stop();
			}
		}

		}
		catch (Exception ex) {}
    }

    /**
     * This function is where the program execution begins if SimpleBot
     * is specified as the main class.  This method demonstrates how to
     * create two Motor objects and a SimpleBot object, then instruct
     * the robot to go.  This method is IntelliBrain specific, though the
     * rest of this class is not.
     */
    public static void main(String args[]) {
        // Do everything in a try-catch block so if anything goes wrong
        // a stack trace can be printed.
        try {
            // Print a message out to indicate the program has started
            System.out.println("SimpleBot");

            // Construct the Motor objects and the SimpleBot object.
			// Use the ContinuousRotationServo class to create a Motor
			// facade around servos attached to the IntelliBrain servo ports.
            // The left servo is attached to servo port 1 on the IntelliBrain.
            // The right servo is attached to servo port 2 on the IntelliBrain.
            SimpleBot simpleBot = new SimpleBot(
					new ContinuousRotationServo(IntelliBrain.getServo(1), false),
					new ContinuousRotationServo(IntelliBrain.getServo(2), true));

            // Command the robot to do the behavior implemented in go.
            simpleBot.go();
        } catch (Throwable t) {		// catch anything that goes wrong
            t.printStackTrace();	// print a stack trace
        }
    }
}
package com.example.natha_000.androidsocketcli;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private Socket socket;

    private static final int SERVERPORT = 6000;
    private static final String SERVER_IP = "192.168.43.1";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new ClientThread()).start();
    }
    public void goForward(View v){
        sendData("w");
    }
    public void goBack(View v){
        sendData("s");
    }
    public void goLeft(View v){
        sendData("a");
    }
    public void goRight(View v){
        sendData("d");
    }
    public void goStop(View v){
        sendData(" ");
    }
    public void goSeek(View v){
        sendData("1");
    }
    public void cancelSeek(View v){
        sendData("2");
    }
    public void saveTarget(View v){
        sendData("x");
    }
    public void saveWaypoint(View v){
        sendData("z");
    }


    private void sendData(String input){
        if(socket == null){
            System.out.println("Socket is null, fool.");
        }
        else{
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                out.println(input);
            } catch (UnknownHostException e) {
                System.out.println("CRAP!");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("CRAP2!");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("CRAP3!");
                e.printStackTrace();
            }

        }
    }


    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket(serverAddr, SERVERPORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }
}

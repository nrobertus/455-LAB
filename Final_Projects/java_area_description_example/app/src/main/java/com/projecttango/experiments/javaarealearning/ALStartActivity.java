/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.projecttango.experiments.javaarealearning;

import com.google.atap.tangoservice.Tango;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.List;

/**
 * Start Activity for Area Description example. Gives the ability to choose a particular
 * configuration and also Manage Area Description Files (ADF).
 */

public class ALStartActivity extends Activity implements View.OnClickListener {

    private static final String ACTION_USB_PERMISSION = "com.multitools.andres.LCView";

    TextView grandMasterFunkJR;

    public static final String USE_AREA_LEARNING =
            "com.projecttango.areadescriptionjava.usearealearning";
    public static final String LOAD_ADF = "com.projecttango.areadescriptionjava.loadadf";
    private ToggleButton mLearningModeToggleButton;
    private ToggleButton mLoadADFToggleButton;
    private Button mStartButton;
    private boolean mIsUseAreaLearning;
    private boolean mIsLoadADF;

    UsbDevice device;
    //Pide permisos al usuario para comunicacion con el dispositivo USB
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                        }
                    }
                    else {
                        grandMasterFunkJR.setText("permission denied for device " + device);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        setTitle(R.string.app_name);
        mLearningModeToggleButton = (ToggleButton) findViewById(R.id.learningmode);
        mLoadADFToggleButton = (ToggleButton) findViewById(R.id.loadadf);
        mStartButton = (Button) findViewById(R.id.start);
        findViewById(R.id.ADFListView).setOnClickListener(this);
        mLearningModeToggleButton.setOnClickListener(this);
        mLoadADFToggleButton.setOnClickListener(this);
        grandMasterFunkJR = (TextView)findViewById(R.id.grandMasterFunk);

        grandMasterFunkJR.setText("Funky code set text");
        mStartButton.setOnClickListener(this);
        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE), 0);

        super.onCreate(savedInstanceState);


        //mUsbManager.requestPermission(device, mPermissionIntent);
    }

    // Move Left
    public void left(View v){
        // Make a serial thing
        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

// Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            byte buffer[] = new byte[16];
            buffer = "d".getBytes();
            port.write(buffer, 360);
            port.close();
        } catch (IOException e) {}
    }

    // Move Right
    public void right(View v){
        grandMasterFunkJR.setText("Made it into the matrix");
// Make a serial thing
        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            grandMasterFunkJR.setText("Emptiness and loneliness.");
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);

        PendingIntent mPermissionIntent;
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        manager.requestPermission(driver.getDevice(), mPermissionIntent);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());

        if (connection == null) {
            grandMasterFunkJR.setText("Connection nullness");
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

// Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            grandMasterFunkJR.setText("Trying stuff.");
            port.open(connection);
            port.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            byte buffer[] = new byte[16];
            buffer = "a".getBytes();
            port.write(buffer, 360);
            port.close();

        } catch (IOException e) {
            grandMasterFunkJR.setText(e.toString());
        }
        //grandMasterFunkJR.setText("Finished trying now. ");
    }

    // Forward
    public void forward(View v){
// Make a serial thing
        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

// Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            byte buffer[] = new byte[16];
            buffer = "w".getBytes();
            port.write(buffer, 360);
            port.close();
        } catch (IOException e) {}
    }

    // Back
    public void back(View v){
// Make a serial thing
        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

// Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            byte buffer[] = new byte[16];
            buffer = "s".getBytes();
            port.write(buffer, 360);
            port.close();
        } catch (IOException e) {}
    }

    public void stop(View v){
        // Make a serial thing
        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

// Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            byte buffer[] = new byte[16];
            buffer = "e".getBytes();
            port.write(buffer, 360);
            port.close();
        } catch (IOException e) {}
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loadadf:
                mIsLoadADF = mLoadADFToggleButton.isChecked();
                break;
            case R.id.learningmode:
                mIsUseAreaLearning = mLearningModeToggleButton.isChecked();
                break;
            case R.id.start:
                startAreaDescriptionActivity();
                break;


            case R.id.ADFListView:
                startADFListView();
                break;
        }
    }

    private void startAreaDescriptionActivity() {
        Intent startADIntent = new Intent(this, AreaLearningActivity.class);
        mIsUseAreaLearning = mLearningModeToggleButton.isChecked();
        mIsLoadADF = mLoadADFToggleButton.isChecked();
        startADIntent.putExtra(USE_AREA_LEARNING, mIsUseAreaLearning);
        startADIntent.putExtra(LOAD_ADF, mIsLoadADF);
        startActivity(startADIntent);
    }

    private void startADFListView() {
        Intent startADFListViewIntent = new Intent(this, ADFUUIDListViewActivity.class);
        startActivity(startADFListViewIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.arealearning_permission, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}

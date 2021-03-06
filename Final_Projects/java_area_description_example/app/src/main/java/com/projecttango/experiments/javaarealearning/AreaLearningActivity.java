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
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.speech.tts.TextToSpeech;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

/**
 * Main Activity class for the Area Description example. Handles the connection to the Tango service
 * and propagation of Tango pose data to OpenGL and Layout views. OpenGL rendering logic is
 * delegated to the {@link AreaLearningRajawaliRenderer} class.
 */
public class AreaLearningActivity extends Activity implements View.OnClickListener,
        SetADFNameDialog.CallbackListener, SaveAdfTask.SaveAdfListener {
    private static final String ACTION_USB_PERMISSION = "com.multitools.andres.LCView";
    private static final String TAG = AreaLearningActivity.class.getSimpleName();
    private static final int SECS_TO_MILLISECS = 1000;
    private Boolean funky_search_started = false;
    private Tango mTango;
    private TextToSpeech tts;
    private TangoConfig mConfig;
    private TextView mUuidTextView;
    private TextView mRelocalizationTextView;

    private Button mSaveAdfButton;
    private Button mFirstPersonButton;
    private Button mThirdPersonButton;
    private Button mTopDownButton;
    private Button funkyStartButton;
    private Button funkyRecordButton;
    private Button funkyStopButton;
    private Button funkyWaypointButton;

    private double mPreviousPoseTimeStamp;
    private double mTimeToNextUpdate = UPDATE_INTERVAL_MS;

    private boolean mIsRelocalized;
    private boolean mIsLearningMode;
    private boolean mIsConstantSpaceRelocalize;
    private boolean recordLocation = false;
    private boolean setWaypoint = false;
    private boolean found = false;
    private boolean stopped = false;

    private boolean landmarkAssigned = false;

    private final AtomicBoolean motion_running = new AtomicBoolean(true);

    private String stop_char = " ";
    private String left_char = "a";
    private String right_char = "d";
    private String back_char = "s";
    private String forward_char = "w";

    private String waypointString = "";

    private String messageOut = "";

    private String robot_movement = "";

    private String last_port_comand = " ";

    private int current_waypoint_index = 0;

    private List<double[]> waypoints = new ArrayList<double[]>();

    private List<double[]> landmarks = new ArrayList<double[]>();

    double yAngle;

    private String ip;

    private double[] funky_target = new double[]{0.0, 0.0, 0.0};

    private ServerSocket serverSocket;

    Handler updateConversationHandler;

    Thread serverThread = null;

    public static final int SERVERPORT = 6000;


    TextView grandMasterFunkRender;


    private AreaLearningRajawaliRenderer mRenderer;

    // Long-running task to save the ADF.
    private SaveAdfTask mSaveAdfTask;

    private static final double UPDATE_INTERVAL_MS = 100.0;
    private static final DecimalFormat FORMAT_THREE_DECIMAL = new DecimalFormat("00.000");

    private final Object mSharedLock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_learning);
        Intent intent = getIntent();
        mIsLearningMode = intent.getBooleanExtra(ALStartActivity.USE_AREA_LEARNING, false);
        mIsConstantSpaceRelocalize = intent.getBooleanExtra(ALStartActivity.LOAD_ADF, false);

        // Instantiate the Tango service
        mTango = new Tango(this);
        mIsRelocalized = false;
        mConfig = setTangoConfig(mTango, mIsLearningMode, mIsConstantSpaceRelocalize);
        setupTextViewsAndButtons(mConfig, mTango, mIsLearningMode, mIsConstantSpaceRelocalize);

        // Configure OpenGL renderer
        mRenderer = setupGLViewAndRenderer();
        grandMasterFunkRender = (TextView)findViewById(R.id.grandMasterFunkRender);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.UK);
                }
            }
        });

        ip = getIpAddress();



        updateConversationHandler = new Handler();

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();



    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    System.out.println("Listening");
                    socket = serverSocket.accept();

                    System.out.println("Accepted");

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();

                    messageOut = read + "\n";
                    read = "" + read.charAt(0);
                    last_port_comand = read;


                    if(read.contains("z")){
                        setWaypoint = true;
                    }
                    else if (read.contains(("x"))){
                        recordLocation = true;
                    }
                    else if(read.contains("1")){
                        found = false;
                        stopped = false;
                        funky_search_started = true;
                        funky_target = waypoints.get(0);
                        tts.speak("Starting", tts.QUEUE_FLUSH, null);

                    }
                    else if (read.contains("2")){
                        stopped = true;
                        serialAction(stop_char);
                        robot_movement = "STOP!";
                        tts.speak("Stopping", tts.QUEUE_FLUSH, null);
                    }

                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }

    }


    public static String getIpAddress() {
        String ipAddress = "Unable to Fetch IP..";
        try {
            Enumeration en;
            en = NetworkInterface.getNetworkInterfaces();
            while ( en.hasMoreElements()) {
                NetworkInterface intf = (NetworkInterface)en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress)enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                        ipAddress=inetAddress.getHostAddress().toString();
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return ipAddress;
    }

    /**
     * Implements SetADFNameDialog.CallbackListener.
     */
    @Override
    public void onAdfNameOk(String name, String uuid) {
        saveAdf(name);
    }

    /**
     * Implements SetADFNameDialog.CallbackListener.
     */
    @Override
    public void onAdfNameCancelled() {
        // Continue running.
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mTango.disconnect();
        } catch (TangoErrorException e) {
            Toast.makeText(getApplicationContext(), R.string.tango_error, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Clear the relocalization state: we don't know where the device has been since our app
        // was paused.
        mIsRelocalized = false;

        // Re-attach listeners.
        try {
            setUpTangoListeners();
        } catch (TangoErrorException e) {
            Toast.makeText(getApplicationContext(), R.string.tango_error, Toast.LENGTH_SHORT)
                    .show();
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), R.string.no_permissions, Toast.LENGTH_SHORT)
                    .show();
        }

        // Connect to the tango service (start receiving pose updates).
        try {
            mTango.connect(mConfig);
        } catch (TangoOutOfDateException e) {
            Toast.makeText(getApplicationContext(), R.string.tango_out_of_date_exception, Toast
                    .LENGTH_SHORT).show();
        } catch (TangoErrorException e) {
            Toast.makeText(getApplicationContext(), R.string.tango_error, Toast.LENGTH_SHORT)
                    .show();
        } catch (TangoInvalidException e) {
            Toast.makeText(getApplicationContext(), R.string.tango_invalid, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Listens for click events from any button in the view.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_waypoint_funk:
                setWaypoint = true;

                break;
            case R.id.record_target_funk:
                recordLocation = true;
                break;
            case R.id.start_search_button_funk:
                found = false;
                stopped = false;
                funky_search_started = true;
                funky_target = waypoints.get(0);
                tts.speak("Starting", tts.QUEUE_FLUSH, null);
                break;
            case R.id.stop_search_button_funk:
                stopped = true;
                tts.speak("Stopping", tts.QUEUE_FLUSH, null);
                break;
            case R.id.first_person_button:
                mRenderer.setFirstPersonView();
                break;
            case R.id.top_down_button:
                mRenderer.setTopDownView();
                break;
            case R.id.third_person_button:
                mRenderer.setThirdPersonView();
                break;
            case R.id.save_adf_button:
                // Query the user for an ADF name and save if OK was clicked.
                showSetADFNameDialog();
                break;
            default:
                Log.w(TAG, "Unknown button click");
                return;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mRenderer.onTouchEvent(event);
        return true;
    }

    /**
     * Sets Rajawalisurface view and its renderer. This is ideally called only once in onCreate.
     */
    private AreaLearningRajawaliRenderer setupGLViewAndRenderer() {
        // Configure OpenGL renderer
        AreaLearningRajawaliRenderer renderer = new AreaLearningRajawaliRenderer(this);
        // OpenGL view where all of the graphics are drawn
        RajawaliSurfaceView glView = (RajawaliSurfaceView) findViewById(R.id.gl_surface_view);
        glView.setEGLContextClientVersion(2);
        glView.setRenderMode(IRajawaliSurface.RENDERMODE_CONTINUOUSLY);
        glView.setSurfaceRenderer(renderer);
        return renderer;
    }

    /**
     * Sets Texts views to display statistics of Poses being received. This also sets the buttons
     * used in the UI. Please note that this needs to be called after TangoService and Config
     * objects are initialized since we use them for the SDK related stuff like version number
     * etc.
     */
    private void setupTextViewsAndButtons(TangoConfig config, Tango tango, boolean
            isLearningMode, boolean isLoadAdf) {

        mFirstPersonButton = (Button) findViewById(R.id.first_person_button);
        mThirdPersonButton = (Button) findViewById(R.id.third_person_button);
        mTopDownButton = (Button) findViewById(R.id.top_down_button);
        funkyStartButton = (Button) findViewById(R.id.start_search_button_funk);
        funkyRecordButton = (Button) findViewById(R.id.record_target_funk);
        funkyStopButton = (Button) findViewById(R.id.stop_search_button_funk);
        funkyWaypointButton = (Button) findViewById(R.id.record_waypoint_funk);

        mSaveAdfButton = (Button) findViewById(R.id.save_adf_button);
        mUuidTextView = (TextView) findViewById(R.id.adf_uuid_textview);
        mRelocalizationTextView = (TextView) findViewById(R.id.relocalization_textview);

        // Set up button click listeners and button state.
        mFirstPersonButton.setOnClickListener(this);
        mThirdPersonButton.setOnClickListener(this);
        mTopDownButton.setOnClickListener(this);
        funkyStartButton.setOnClickListener(this);
        funkyRecordButton.setOnClickListener(this);
        funkyStopButton.setOnClickListener(this);
        funkyWaypointButton.setOnClickListener(this);

        if (isLearningMode) {
            // Disable save ADF button until Tango relocalizes to the current ADF.
            mSaveAdfButton.setEnabled(false);
            mSaveAdfButton.setOnClickListener(this);
        } else {
            // Hide to save ADF button if leanring mode is off.
            mSaveAdfButton.setVisibility(View.GONE);
        }

        if (isLoadAdf) {
            ArrayList<String> fullUUIDList = new ArrayList<String>();
            // Returns a list of ADFs with their UUIDs
            fullUUIDList = tango.listAreaDescriptions();
            if (fullUUIDList.size() == 0) {
                mUuidTextView.setText(R.string.no_uuid);
            } else {
                mUuidTextView.setText(getString(R.string.number_of_adfs) + fullUUIDList.size()
                        + getString(R.string.latest_adf_is)
                        + fullUUIDList.get(fullUUIDList.size() - 1));
            }
        }
    }

    /**
     * Sets up the tango configuration object. Make sure mTango object is initialized before
     * making this call.
     */
    private TangoConfig setTangoConfig(Tango tango, boolean isLearningMode, boolean isLoadAdf) {
        TangoConfig config = new TangoConfig();
        config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        // Check if learning mode
        if (isLearningMode) {
            // Set learning mode to config.
            config.putBoolean(TangoConfig.KEY_BOOLEAN_LEARNINGMODE, true);

        }
        // Check for Load ADF/Constant Space relocalization mode
        if (isLoadAdf) {
            ArrayList<String> fullUUIDList = new ArrayList<String>();
            // Returns a list of ADFs with their UUIDs
            fullUUIDList = tango.listAreaDescriptions();
            // Load the latest ADF if ADFs are found.
            if (fullUUIDList.size() > 0) {
                config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION,
                        fullUUIDList.get(fullUUIDList.size() - 1));
            }
        }
        return config;
    }

    /**
     * Set up the callback listeners for the Tango service, then begin using the Motion
     * Tracking API. This is called in response to the user clicking the 'Start' Button.
     */
    private void setUpTangoListeners() {

        // Set Tango Listeners for Poses Device wrt Start of Service, Device wrt
        // ADF and Start of Service wrt ADF
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                TangoPoseData.COORDINATE_FRAME_DEVICE));
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE));

        mTango.connectListener(framePairs, new OnTangoUpdateListener() {
            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzij) {
                // Not using XyzIj data for this sample
            }

            // Listen to Tango Events
            @Override
            public void onTangoEvent(final TangoEvent event) {
            }

            @Override
            public void onPoseAvailable(TangoPoseData pose) {

                boolean updateRenderer = false;
                // Make sure to have atomic access to Tango Data so that
                // UI loop doesn't interfere while Pose call back is updating
                // the data.
                synchronized (mSharedLock) {
                    // Check for Device wrt ADF pose, Device wrt Start of Service pose,
                    // Start of Service wrt ADF pose (This pose determines if the device
                    // is relocalized or not).
                    if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
                            && pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE) {

                        if (mIsRelocalized) {
                            updateRenderer = true;


                        }
                    } else if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE
                            && pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE) {
                        if (!mIsRelocalized) {
                            updateRenderer = true;
                        }

                    } else if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
                            && pose.targetFrame == TangoPoseData
                            .COORDINATE_FRAME_START_OF_SERVICE) {
                        if (pose.statusCode == TangoPoseData.POSE_VALID) {

                            mIsRelocalized = true;

                            // Set the color to green
                        } else {
                            mIsRelocalized = false;
                            // Set the color blue
                        }
                    }
                }

                final double deltaTime = (pose.timestamp - mPreviousPoseTimeStamp) *
                        SECS_TO_MILLISECS;
                mPreviousPoseTimeStamp = pose.timestamp;
                mTimeToNextUpdate -= deltaTime;

                if (mTimeToNextUpdate < 0.0) {
                    mTimeToNextUpdate = UPDATE_INTERVAL_MS;
                    final TangoPoseData tPose = pose;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (mSharedLock) {
                                mSaveAdfButton.setEnabled(mIsRelocalized);

                                funkyNavigationFunction(tPose);

                                mRelocalizationTextView.setText(mIsRelocalized ?
                                        getString(R.string.localized) :
                                        getString(R.string.not_localized));
                            }
                        }
                    });
                }

                if (updateRenderer) {

                    mRenderer.updateDevicePose(pose, mIsRelocalized);
                }
            }

            @Override
            public void onFrameAvailable(int cameraId) {
                // We are not using onFrameAvailable for this application.
            }
        });
    }

    /**
     * Save the current Area Description File.
     * Performs saving on a background thread and displays a progress dialog.
     */
    private void saveAdf(String adfName) {
        mSaveAdfTask = new SaveAdfTask(this, this, mTango, adfName);
        mSaveAdfTask.execute();
    }

    /**
     * Handles failed save from mSaveAdfTask.
     */
    @Override
    public void onSaveAdfFailed(String adfName) {
        String toastMessage = String.format(
                getResources().getString(R.string.save_adf_failed_toast_format),
                adfName);
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        mSaveAdfTask = null;
    }

    /**
     * Handles successful save from mSaveAdfTask.
     */
    @Override
    public void onSaveAdfSuccess(String adfName, String adfUuid) {
        String toastMessage = String.format(
                getResources().getString(R.string.save_adf_success_toast_format),
                adfName, adfUuid);
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        mSaveAdfTask = null;
        finish();
    }

    /**
     * Shows a dialog for setting the ADF name.
     */
    private void showSetADFNameDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("name", "New ADF");
        bundle.putString("id", ""); // UUID is generated after the ADF is saved.

        FragmentManager manager = getFragmentManager();
        SetADFNameDialog setADFNameDialog = new SetADFNameDialog();
        setADFNameDialog.setArguments(bundle);
        setADFNameDialog.show(manager, "ADFNameDialog");
    }


    // Move Right
    public void serialAction(String input){

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
            PendingIntent mPermissionIntent;
            mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

            manager.requestPermission(driver.getDevice(), mPermissionIntent);
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

// Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);
        try {

            port.open(connection);
            port.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            byte buffer[] = new byte[16];
            buffer = input.getBytes();
            port.write(buffer, 360);
            port.close();

        } catch (IOException e) {

        }
        //grandMasterFunkJR.setText("Finished trying now. ");
    }


    public double roundToTwo(double number){
        number = Math.round(number * 100);
        number = number/100;
        return number;
    }





    /*******************************************************/
    //  Navigation code block. This is called from the UI thread. Should change that.
    /******************************************************/
    public void funkyNavigationFunction(TangoPoseData tPose){

        double r_tolerance = 0.3;
        double pos_tolerance = 0.1;

        double x_target = funky_target[0];
        double y_target= funky_target[1];
        double r_target = 0;

        double q[];
        q = tPose.rotation;
        yAngle = Math.atan2(2 * (q[3] * q[2] + q[0] * q[1]), 1 - 2 * (q[1] * q[1] + q[2] * q[2]));
        yAngle = yAngle + Math.PI / 2;
        double x_pos = roundToTwo(tPose.translation[0]);
        double y_pos = roundToTwo(tPose.translation[1]);



        double y_dif = (y_target-y_pos);
        double x_dif = (x_target-x_pos);

        double angle = Math.atan2(y_dif, x_dif);

        double test = roundToTwo(angle-yAngle);


        if(setWaypoint){
            double newPoint[] = new double[3];
            newPoint[0] = x_pos;
            newPoint[1] = y_pos;
            newPoint[2] = test;

            waypoints.add(newPoint); // Add the waypoint to the waypoints array

            waypointString += (Double.toString(newPoint[0]) +" , " + Double.toString(newPoint[1]) + " ," + Double.toString(newPoint[2]) + "\n");

            tts.speak("Waypoint recorded", tts.QUEUE_FLUSH, null);
            setWaypoint = false;
        }


        if(recordLocation){

            double newPoint[] = new double[3];
            newPoint[0] = x_pos;
            newPoint[1] = y_pos;
            newPoint[2] = test;

            landmarks.add(newPoint); // Add the waypoint to the waypoints array

            tts.speak("Target recorded", tts.QUEUE_FLUSH, null);
            recordLocation = false;
        }


        if(found==false && mIsRelocalized && funky_search_started && !stopped){
            if(test <= (r_target+r_tolerance) && test >= (r_target - r_tolerance)){
                if(x_pos <= (x_target + pos_tolerance) &&
                        x_pos >= (x_target - pos_tolerance)&&
                        y_pos <= (y_target + pos_tolerance)&&
                        y_pos >= (y_target - pos_tolerance)){
                    robot_movement = "Stop";
                    serialAction(stop_char);
                    tts.speak("Found Waypoint", tts.QUEUE_FLUSH, null);

                    if(current_waypoint_index >= (waypoints.size()-1)){
                        if((landmarks.size() > 0) && landmarkAssigned == false){

                            funky_target = landmarks.get(0);
                            landmarkAssigned = true;
                            tts.speak("Going to landmark", tts.QUEUE_FLUSH, null);
                        }
                        else{
                            tts.speak("Engaging target", tts.QUEUE_FLUSH, null);
                            found = true;
                        }

                    }
                    else{
                        current_waypoint_index = current_waypoint_index +1;
                        funky_target = waypoints.get(current_waypoint_index);
                        tts.speak("Finding Next Waypoint", tts.QUEUE_FLUSH, null);
                    }

                }
                else{
                    robot_movement = "go!";
                    serialAction(forward_char);
                }
            }
            else if(test > (r_target+r_tolerance)){
                robot_movement = "left";
                serialAction(left_char);
            }
            else if(test < (r_target - r_tolerance)) {
                robot_movement = "right";
                serialAction(right_char);
            }
        }
        else{
            if(last_port_comand.contains(stop_char)){
                robot_movement = "STOP!";
                serialAction(stop_char);
            }
            else if( last_port_comand.contains(forward_char)){
                robot_movement = "GO!";
                serialAction(forward_char);
            }
            else if(last_port_comand.contains(back_char)){
                robot_movement = "BACK!";
                serialAction(back_char);
            }
            else if( last_port_comand.contains(left_char)){
                robot_movement = "LEFT!";
                serialAction(left_char);
            }
            else if (last_port_comand.contains(right_char)){
                robot_movement = "RIGHT!";
                serialAction( right_char);
            }

        }
        if(ip == "Unable to Fetch IP.."){
            ip = getIpAddress();
        }





        grandMasterFunkRender.setText(
                "Position: "+Double.toString(x_pos) +
                        ", " + Double.toString(y_pos) +
                        "\nAngle: " + Double.toString(test) +
                        "\nMovement: " + robot_movement +
                        "\nTarget: " + Double.toString(x_target) +
                        ", " + Double.toString(y_target) +
                        "\nIP: " + ip+ "\n" + waypointString +
                        "\nPort in: " + messageOut
        );

    }
}

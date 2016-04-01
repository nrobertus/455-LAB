package gov.funkytown.funkehtowntooelectricboogalou;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoErrorException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;



public class boogiefever extends AppCompatActivity {

    TextView grandMasterFunkJR;

    private Tango mTango;
    private TangoConfig mConfig;
    private TextView mUuidTextView;
    private TextView mRelocalizationTextView;

    private double mPreviousPoseTimeStamp;
    private double mTimeToNextUpdate = UPDATE_INTERVAL_MS;

    private boolean mIsRelocalized;
    private boolean mIsLearningMode;
    private boolean mIsConstantSpaceRelocalize;

    private static final double UPDATE_INTERVAL_MS = 100.0;

    private static final DecimalFormat FORMAT_THREE_DECIMAL = new DecimalFormat("00.000");

    private final Object mSharedLock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boogiefever);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE),
                Tango.TANGO_INTENT_ACTIVITYCODE);

        grandMasterFunkJR = (TextView)findViewById(R.id.grandMasterFunk);

        grandMasterFunkJR.setText("Funky code set text");

        Tango mTango = new Tango(this);
        TangoConfig mcConfig = setTangoConfig(mTango);
        String areaByName = "room";
        String uuid = "room";

        Boolean error = false;

        ArrayList<String> fullUUIDList = new ArrayList<String>();
        // Returns a list of ADFs with their UUIDs
        fullUUIDList = mTango.listAreaDescriptions();

        grandMasterFunkJR.setText("Getting UUID");
        uuid = fullUUIDList.get(0);

        grandMasterFunkJR.setText("FOUND UUID NAME " + uuid );
        // Load the latest ADF if ADFs are found.
        if (fullUUIDList.size() > 0) {
            mcConfig.putString(TangoConfig.KEY_STRING_AREADESCRIPTION, uuid);
        }

        try {
            mcConfig.putString(TangoConfig.KEY_STRING_AREADESCRIPTION, uuid);
        } catch (TangoErrorException e) {
            error = true;
        }

        try {
            mcConfig = new TangoConfig();
            mcConfig = mTango.getConfig(TangoConfig.CONFIG_TYPE_CURRENT);
            mcConfig.putBoolean(TangoConfig.KEY_BOOLEAN_LEARNINGMODE, true);
        } catch (TangoErrorException e) {
            error = true;
        }

        if(error){
            grandMasterFunkJR.setText("SETUP COMPLETE WITH ERROR");
        }
        else{
            grandMasterFunkJR.setText("SETUP COMPLETE NO ERROR");
        }

        grandMasterFunkJR.setText("WALK AROUND FOR POSE DATA");
    }

    /**
     * Sets up the tango configuration object. Make sure mTango object is initialized before
     * making this call.
     */
    private TangoConfig setTangoConfig(Tango tango) {
        TangoConfig config = new TangoConfig();
        config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        // Check for Load ADF/Constant Space relocalization mode

        ArrayList<String> fullUUIDList = new ArrayList<String>();
        // Returns a list of ADFs with their UUIDs
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!! LOADING AREA DESCRIPTIONS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        fullUUIDList = tango.listAreaDescriptions();
        // Load the latest ADF if ADFs are found.
        if (fullUUIDList.size() > 0) {
            config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION,
                    fullUUIDList.get(fullUUIDList.size() - 1));
        }
        return config;
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
            buffer = "a".getBytes();
            port.write(buffer, 360);
            port.close();
        } catch (IOException e) {}
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_boogiefever, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

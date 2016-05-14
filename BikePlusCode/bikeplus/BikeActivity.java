package com.mg.mig.bikeplus;

import android.Manifest;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.KeyEvent;
import android.view.View;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Miguel Guevara
 * 04/29/16
 */
public class BikeActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback, LocationListener {
    private static final String TAG = "Bluetooth";
    private static final String TAG2 = "User";
    private static final String TAG3 = "db";

    private static final UUID U_SENSOR = UUID.fromString("9390eb2b-8613-4cd9-b9b2-9669ace7db51");
    //LED inside the Arduino
    private static final UUID LED_SERVICE = UUID.fromString("19B10010-E8F2-537E-4F6C-D104768A1214");
    private static final UUID LED_SWITCH_CHAR = UUID.fromString("19B10011-E8F2-537E-4F6C-D104768A1214");
    private static final UUID LED_BUTTON_CHAR = UUID.fromString("19B10012-E8F2-537E-4F6C-D104768A1214");

    private static final UUID LIGHT_SERVICE = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214");
    private static final UUID LIGHT_CHAR = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1214");
    //******NEW INSTANCES******
    private static final UUID SONAR_SERVICE = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1215");
    private static final UUID SONAR_CHAR = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1215");

    private static final UUID PULSE_SERVICE = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1216");
    private static final UUID PULSE_CHAR = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1216");

    private static final UUID FPS_SERVICE = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1217");
    private static final UUID FPS_CHAR = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1217");
    //______END OF NEW INSTANCES
    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    //my private instances
    private Button button;
    //private Button dButton;
    private BluetoothAdapter mBluetoothAdapter;
    private SparseArray<BluetoothDevice> mDevices;
    private Double lightState;
    private BluetoothGatt  lightSwitchChar;
    private int begginingStatus;
    private int sonarValue;
    private Double FPSvalue;
    private Double userId;
    private double oldValueFPS;
    //SCENES
    ViewGroup rootContainer;
    //Transition transitionMgr;
    Scene logIn;
    Scene newProfile;
    Scene fingerScanning1;
    Scene fingerScanning2;
    Scene fingerScanning3;
    Scene mainAct;
    private static final String DEVICE_NAME = "BikePlus";
    private String username;
    private String password;
    private boolean signToDb;
    private int lockValue;
    //end of my private instances

    private BluetoothGatt mConnectedGatt;

    private TextView mTemperature, mHumidity, mPressure, mLED;

    private ProgressDialog mProgress;

    List<BluetoothGattService> myList;

    List<BluetoothGattCharacteristic> charList;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG3, "onCreate:");
        setContentView(R.layout.activity_bike);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.w(TAG3, "onCreate: start********");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.w(TAG3, "**********onCreate");
        rootContainer = (ViewGroup)findViewById(R.id.rootContainer);
        Log.w(TAG3, "**********onCreate***********");
        //transitionMgr = TransitionInflater.from(this).inflateTransition(R.transition.transition);
        Log.i(TAG, "onCreate: after scene captured");
        /*
         * We are going to display the results in some text fields
         */
        Log.w(TAG3, "onCreate:");
        lightState = 0.0;
        begginingStatus = 0;

        Log.w(TAG3, "onCreate:");
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        mDevices = new SparseArray<BluetoothDevice>();
        Log.i(TAG3, "onCreate: before scene captured");
        //ON and OFF values
        sonarValue = 0;
        FPSvalue = 220.00;
        signToDb = false;
        lockValue = 0;
        //SCENES
        Log.w(TAG3, "onCreate:");
        logIn = Scene.getSceneForLayout(rootContainer, R.layout.login_scene, this);
        newProfile = Scene.getSceneForLayout(rootContainer, R.layout.new_profile,this);
        fingerScanning1 = Scene.getSceneForLayout(rootContainer, R.layout.fingerprint_creation,this);
        fingerScanning2 = Scene.getSceneForLayout(rootContainer,R.layout.fingerprint_creation2, this);
        fingerScanning3 = Scene.getSceneForLayout(rootContainer,R.layout.fingerprint_creation3,this);
        mainAct = Scene.getSceneForLayout(rootContainer, R.layout.main_panel_fragment,this);
        Log.w(TAG3, "onCreate: before last");
        /*
         * A progress dialog will be needed while the connection process is
         * taking place
         */
        Log.i(TAG3, "onCreate: after scene captured");
        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);
        Log.w(TAG3, "onCreate: last");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        logIn.enter();
        startScan();
        Log.w(TAG, "onCreate: startScan executed......");
        LocationManager lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                // TODO: Consider calling
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 13);
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(TAG3, "back button pressed");
            TransitionManager.go(logIn);
            mProgress.hide();
        }
        Log.d(TAG3, "back button pressed: KEY IS:" + event.getCharacters());
        return false;
    }
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        Log.w(TAG, "onSavedInstanceState");
        CharSequence myChar = null;
        if (lightState != null)
            myChar = lightState.toString();
        outState.putCharSequence("value", myChar);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        Log.w(TAG, "onRestoreInstanceState");
        CharSequence myChar = savedInstanceState.getCharSequence("value");
        if (myChar == "0")
            lightState = 0.0;
        else lightState = 1.0;
        mLED.setText("" + lightState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //clearDisplayValues();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Make sure dialog is hidden
        mProgress.dismiss();
        //Cancel any scans in progress
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.stopLeScan(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w(TAG, "onStrop: onStop beginning");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.mg.mig.btdevice/http/host/path")
        );
        //AppIndex.AppIndexApi.end(client, viewAction);
        //Disconnect from any active tag connection

        if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
            Log.w(TAG, "onStop: device disconnected");
        }



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();

        Log.w(TAG, "onStop: onStop ending");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.w(TAG, "onDestroy");
        //if (mConnectedGatt != null) {
        //    mConnectedGatt.disconnect();
        //    mConnectedGatt = null;
        //    Log.w(TAG, "onDestroy: device disconnected");
        //}

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add the "scan" option to the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Add any device elements we've discovered to the overflow menu
        Log.i(TAG, "************Adding name to the menu || mDevice.size()=" + mDevices.size());
        for (int i = 0; i < mDevices.size(); i++) {

            BluetoothDevice device = mDevices.valueAt(i);
            menu.add(0, mDevices.keyAt(i), 0, device.getName());
            if (device.getName().equals("BikePlus")) {
                Log.w(TAG, "onCreateOptionsMenu: BikePlus in the system, ready to be read. device:" + device.getName());
                //Obtain the discovered device to connect with
                Log.i(TAG, "Connecting to " + device.getName());
                /*
                 * Make a connection with the device using the special LE-specific
                 * connectGatt() method, passing in a callback for GATT events
                 */
                mConnectedGatt = device.connectGatt(this, false, mGattCallback);
                //Display progress UI
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to " + device.getName() + "..."));


                break;
            }
            else Log.w(TAG, "onCreateOptionsMenu: BikePlus NOT in the system. device.getName:" + device.getName());
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                Log.i(TAG, "***********CLEARS AND STARTS startScan()");
                mDevices.clear();
                startScan();
                return true;
            default:
                //Obtain the discovered device to connect with
                BluetoothDevice device = mDevices.get(item.getItemId());
                Log.i(TAG, "Connecting to " + device.getName());
                /*
                 * Make a connection with the device using the special LE-specific
                 * connectGatt() method, passing in a callback for GATT events
                 */
                mConnectedGatt = device.connectGatt(this, false, mGattCallback);
                //Display progress UI
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to " + device.getName() + "..."));
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearDisplayValues() {
        mLED.setText("---");
    }


    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    private Runnable myRun = new Runnable() {
        @Override
        public void run() {
            runTheApp();
        }
    };

    private void runTheApp(){
        mBluetoothAdapter.startLeScan(this);
    }

    private void startScan() {
        Log.w(TAG, "startScan()");
        mHandler.postDelayed(myRun, 0);
        //mBluetoothAdapter.startLeScan(this);
        setProgressBarIndeterminateVisibility(true);

        mHandler.postDelayed(mStopRunnable, 2500);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
        setProgressBarIndeterminateVisibility(false);
    }

    /* BluetoothAdapter.LeScanCallback */

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
        /*
         * We are looking for SensorTag devices only, so validate the name
         * that each device reports before adding it to our collection
        */
        //SECURE MEASUREMNETS*******
        Log.w(TAG, "**************onLeScan: device name is: " + device.getName());
        if (DEVICE_NAME.equals(device.getName())) {
            Log.w(TAG, "*************onLeScan: BikePlus == " + device.getName() + "? Y : N");
            mDevices.put(device.hashCode(), device);
            //Update the overflow menu
            invalidateOptionsMenu();
        }
        //mDevices.put(device.hashCode(), device);
        //Update the overflow menu
        //invalidateOptionsMenu();

    }



    /*
     * In this callback, we've created a bit of a state machine to enforce that only
     * one characteristic be read or written at a time until all of our sensors
     * are enabled and we are registered to get notifications.
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        /* State Machine Tracking */
        private int mState = 0;

        private void reset() {
            mState = 0;
        }

        private void advance() {
            mState++;
        }



        /*
         * Send an enable command to each sensor by writing a configuration
         * characteristic.  This is specific to the SensorTag to keep power
         * low by disabling sensors you aren't using.
         */
        private void enableNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            Log.w(TAG, "enableNextSensot: start up");
            switch (mState) {
                case 0:
                    Log.d(TAG, "*******Enabling Finger print scanner to read");
                    characteristic = gatt.getService(LIGHT_SERVICE)
                            .getCharacteristic(LIGHT_CHAR);
                    characteristic.setValue(new byte[]{0x03});
                    Log.i(TAG, "*****Fingerprint scanner enabled");
                    break;

                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }
            Log.i(TAG, "enabledNextSensor end of function");
            gatt.writeCharacteristic(characteristic);
        }

        /*
         * Read the data characteristic's value for each sensor explicitly
         */
        private void readNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.i(TAG, "*****Reading LED");
                    characteristic = gatt.getService(LIGHT_SERVICE)
                            .getCharacteristic(LIGHT_CHAR);
                    Log.w(TAG, "*******readNextSensor: read value...");
                    break;
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors READ");
                    return;
            }

            gatt.readCharacteristic(characteristic);
        }

        /*
         * Enable notification of changes on the data characteristic for each sensor
         * by writing the ENABLE_NOTIFICATION_VALUE flag to that characteristic's
         * configuration descriptor.
         */
        private void setNotifyNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Set notify LED");
                    characteristic = gatt.getService(LED_SERVICE)
                            .getCharacteristic(LED_BUTTON_CHAR);
                    break;
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }
            //Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true);
            //Enabled remote notifications
            BluetoothGattDescriptor desc = characteristic.getDescriptor(LED_BUTTON_CHAR);
            desc.setValue(new byte[]{0x01});
            gatt.writeDescriptor(desc);
            Log.i(TAG, "end of set notification sensor");
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.w(TAG, "***********Connection State Change: " + status + " -> " + connectionState(newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */
                Log.i(TAG, "********Full connection************");
                final BluetoothGatt gattTemp = gatt;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.w(TAG, "******Discovering SERVICES");
                        gattTemp.discoverServices();
                    }
                }, 500);
                //gatt.discoverServices();
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Discovering Services..."));
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                /*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 */
                Log.w(TAG, "**********onConnectionChanged: status is 'GATT_SUCCESS' and newState is 'DISCONNECTED'");
                gatt.disconnect();
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If there is a failure at any stage, simply disconnect
                 */
                Log.w(TAG, "**********onConnectionChanged: statis is not 'GATT_SUCCESS'");
                gatt.disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.w(TAG, "*********Services Discovered: " + status);
            mHandler.sendMessage(Message.obtain(null, MSG_DISMISS, "Enabling Sensors..."));
            /*
             * With services discovered, we are going to reset our state machine and start
             * working through the sensors we need to enable
             */
            reset();
            Log.w(TAG, "*******readNextSensor function kicks in");
            //**************************************************
            //**************************************************
            //**************************************************
            readNextSensor(gatt);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //For each read, pass the data up to the UI thread to update the display
            Log.w(TAG2, "characteristic value:" + characteristic.getUuid());
            if (LIGHT_CHAR.equals(characteristic.getUuid())) {

                mHandler.sendMessage(Message.obtain(null, MSG_LED, characteristic));
                Log.w(TAG, "*******onCharacteristicRead: UI thread for messeage passing");
            }
            else if(FPS_CHAR.equals(characteristic.getUuid())){
                Log.i(TAG2, "*******onCharacteristicRead: UI thread for messeage passing");
                mHandler.sendMessage(Message.obtain(null, MSG_FPS, characteristic));
            }
            else if(SONAR_CHAR.equals(characteristic.getUuid())){
                Log.i(TAG2, "******onCharacteristicRead...sonar");
                mHandler.sendMessage(Message.obtain(null, MSG_SONAR, characteristic));
            }

            else if(PULSE_CHAR.equals(characteristic.getUuid())){
                Log.i(TAG2, "******onCharacteristicRead...PULSE");
                mHandler.sendMessage(Message.obtain(null, MSG_PULSE, characteristic));
            }
            //advance();
            //readNextSensor(gatt);
            //enableNextSensor(gatt);
            //After reading the initial value, next we enable notifications
            //setNotifyNextSensor(gatt);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //After writing the enable flag, next we read the initial value
            Log.w(TAG, "onCharacteristicWrite: BEGIN");
            advance();
            readNextSensor(gatt);
            Log.w(TAG, "onCharacteristicWrite: END");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            /*
             * After notifications are enabled, all updates from the device on characteristic
             * value changes will be posted here.  Similar to read, we hand these up to the
             * UI thread to update the display.
             */
            Log.i(TAG, "onCharacteristicChanged called");
            if (LIGHT_CHAR.equals(characteristic.getUuid())) {
                Log.i(TAG, "onCharacteristicChanged: ight char is equal to char uuid");
                mHandler.sendMessage(Message.obtain(null, MSG_LED, characteristic));

            }

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Once notifications are enabled, we move to the next sensor and start over with enable
            advance();
            enableNextSensor(gatt);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "Remote RSSI: " + rssi);
        }

        private String connectionState(int status) {
            switch (status) {
                case BluetoothProfile.STATE_CONNECTED:
                    return "Connected";
                case BluetoothProfile.STATE_DISCONNECTED:
                    return "Disconnected";
                case BluetoothProfile.STATE_CONNECTING:
                    return "Connecting";
                case BluetoothProfile.STATE_DISCONNECTING:
                    return "Disconnecting";
                default:
                    return String.valueOf(status);
            }
        }
    };

    /*
     * We have a Handler to process event results on the main thread
     */
    private static final int MSG_LED = 401;
    private static final int MSG_FPS = 402;
    private static final int MSG_SONAR = 403;
    private static final int MSG_PULSE = 404;
    private static final int MSG_HUMIDITY = 101;
    private static final int MSG_PRESSURE = 102;
    private static final int MSG_PRESSURE_CAL = 103;
    private static final int MSG_PROGRESS = 201;
    private static final int MSG_DISMISS = 202;
    private static final int MSG_CLEAR = 301;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            BluetoothGattCharacteristic characteristic;
            switch (msg.what) {
                case MSG_LED:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaing LED value");
                        return;
                    }
                    Log.w(TAG, "No error obtaining LED value");
                    updateLEDValues(characteristic);
                    break;

                case MSG_FPS:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining humidity value");
                        return;
                    }
                    updateFPSvalue(characteristic);
                    break;
                case MSG_SONAR:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining pressure value");
                        return;
                    }
                    updateSonarValue(characteristic);
                    break;
                case MSG_PULSE:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining cal value");
                        return;
                    }
                    updatePulse(characteristic);
                    break;
                case MSG_PROGRESS:
                    mProgress.setMessage((String) msg.obj);
                    if (!mProgress.isShowing()) {
                        mProgress.show();
                    }
                    break;
                case MSG_DISMISS:
                    mProgress.hide();
                    break;
                case MSG_CLEAR:
                    clearDisplayValues();
                    break;
            }
        }
    };

    /* Methods to extract sensor data and update the UI */
    private void updateLEDValues(BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, "Before sending a thread to the user interface. the UI");
        double LED = SensorTagData.extractLED(characteristic);
        Log.w(TAG, "Done updating BEFOREthe value: LED value is:" + LED);
        //mLED.setText("" + LED);
        //final TextView mLED = (TextView)findViewById(R.id.ledValue);
        //mLED.setText("" + LED);
        Log.w(TAG2, "Done updating AFTER the values was changed");
        lightState = LED;
        //listenForLogin();   //listens for device to id the user
        //mLED.setText(String.format("%0.5f", LED));
        Log.i(TAG, "Sending a thread every 1/2 a second to read FPS");
    }
    /**
     * login LOOP**** runs ever 1/2 second to read values
     */
    public void listenForLogin(){
        Log.i(TAG3, "BEGINING: listenForLogin: FPSvalue is:" + FPSvalue);
        mProgress.show();
        if(FPSvalue >= 0.0 && FPSvalue < 200.00) {  //this is an authorize user
            //User loged in
            Log.i(TAG3, "IF: listenForLogin: FPSvalue is:" + FPSvalue);
            mProgress.hide();
            Log.w(TAG2, "listenForLogin: FPSvalue is:" + FPSvalue);
            sonarValue = 1;
            TransitionManager.go(mainAct);
            Log.i(TAG2, "Runnable is running");
            //add to database
            if(signToDb){
                Log.w(TAG3, "singToDb is true. name on the username is:" + username );
                deleteUser(FPSvalue.intValue());
                signUp(FPSvalue.intValue(), username, password);
                signToDb = false;
            }
            else Log.w(TAG3, "singToDb is false");
            final TextView welcomeText = (TextView)findViewById(R.id.welcomeText);
            String name = findUser(FPSvalue.intValue());
            Log.i(TAG3, "name of the user is: " + name + " FPSvalue:" + FPSvalue.intValue());
            welcomeText.setText("Welcome " + name);
            loopingTheSonar();
        }
        else if(FPSvalue == 201){
            Log.i(TAG3, "ELSE IF: id is 201 and sends a case 11 which changes the id value in the '101' FPSvalue is:" + FPSvalue);
            mProgress.setMessage("User not recognized");
            mProgress.show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.w(TAG3, "FPS value is been written into");
                    resetId();
                }
            }, 2000);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.w(TAG3, "FPS value is been read");
                    readFPS();
                    mProgress.hide();

                }
            }, 4000);

        }

        else {
            Log.i(TAG3, "ELSE: listenForLogin: FPSvalue is:" + FPSvalue);

            mHandler.postDelayed(loopingTheFPS, 500);  //looking for Finger print
        }
        //mProgress.shide();

    }

    private Runnable loopingTheFPS = new Runnable() {
        @Override
        public void run() {
            Log.w(TAG2, "runnable: the value is running");
            readFPS();
            listenForLogin();
        }
    };


    private void updateHumidityValues(BluetoothGattCharacteristic characteristic) {
        double humidity = SensorTagData.extractHumidity(characteristic);

        mHumidity.setText(String.format("%.0f%%", humidity));
    }

    /**
     * ********this is where the FPSvalue is received
     * @param characteristic
     */
    private void updateFPSvalue(BluetoothGattCharacteristic characteristic){
        Log.i(TAG2, "Before sending a thread to the user interface. the UI");
        double LED = SensorTagData.extractLED(characteristic);
        Log.w(TAG3, "FPSvalue changes to " + LED);
        FPSvalue = LED;
        Log.w(TAG2, "Done updating AFTER value: FPS value is:" + LED);

    }

    private void updatePulse(BluetoothGattCharacteristic characteristic){
        Log.i(TAG2, "Before sending a thread to the user interface. the UI");
        double LED = SensorTagData.extractLED(characteristic);
        Log.w(TAG2, "************************** HeartRate value:" + LED + "*********************");
        final TextView myHeart = (TextView)findViewById(R.id.hr);
        myHeart.setText("" + LED + " \u2661");
        if (lockValue == 1){
            final Button uButton = (Button)findViewById(R.id.unlockButton);
            if (uButton.getText().equals("Unlock")){    //this will unlock the device
                uButton.setText("Lock");
                unlockTheBike();
            }
            else {  //this will lock the device
                uButton.setText("Unlock");
                lockTheBike();
            }
            lockValue = 0;
        }
    }


    private void updateSonarValue(BluetoothGattCharacteristic characteristic){
        double LED = SensorTagData.extractLED(characteristic);
        Log.w(TAG2, "************************** SONAR value:" + LED + "*********************");
        final TextView backSensorText = (TextView)findViewById(R.id.sonar);
        if(LED < 130.00){
            String value = String.format("%.2f", (LED / 12));
            backSensorText.setText(value);
        }
        else backSensorText.setText("");
        readHR();
    }

    private int[] mPressureCals;

    private void updatePressureCals(BluetoothGattCharacteristic characteristic) {
        mPressureCals = SensorTagData.extractCalibrationCoefficients(characteristic);
    }

    private void updatePressureValue(BluetoothGattCharacteristic characteristic) {
        if (mPressureCals == null) return;
        double pressure = SensorTagData.extractBarometer(characteristic, mPressureCals);
        double temp = SensorTagData.extractBarTemperature(characteristic, mPressureCals);

        mTemperature.setText(String.format("%.1f\u00B0C", temp));
        mPressure.setText(String.format("%.2f", pressure));
    }

    /*
    When the user hits the button name "button", change the value of the LED
     */
    public void changeValue(View view){
        //mLED.setText("2.4");
        if(mConnectedGatt != null){
            BluetoothGattCharacteristic characteristic;
            if(lightState == 1.0){
                Log.d(TAG, "*******IF: changing LED character");
                characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                        .getCharacteristic(LIGHT_CHAR);
                characteristic.setValue(new byte[]{0x00});
                Log.i(TAG, "*****Sent value of 0 to the BLE");
                final TextView mLED = (TextView)findViewById(R.id.ledValue);
                mLED.setText("0");
                lightState = 0.0;
            }
            else{
                Log.d(TAG, "*******ELSE: changing LED character");
                characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                        .getCharacteristic(LIGHT_CHAR);
                characteristic.setValue(new byte[]{0x01});
                Log.i(TAG, "*****Sent value of 1 to the BLE");
                final TextView mLED = (TextView)findViewById(R.id.ledValue);
                mLED.setText("1");
                lightState = 1.0 ;
            }
            mConnectedGatt.writeCharacteristic(characteristic);
        }
        else Log.w(TAG, "mConnectedGatt is NULL");

    }
    public void goBiking(View view){
        //turn on sonar sensor, read speed, read sonar sensor every 2500 milli, write to LEDs
        //turnOnSonar();
        //run a runnable to read sonar value every 2500 milli seconds
        if(mConnectedGatt != null){
            mProgress.setMessage("Scan Finger");
            Log.w(TAG2, "************enabling the scanner to read");
            enableFPS();
            Log.i(TAG3, "FPSvalue is :" + FPSvalue + ". This is when the button gets clicked");
            listenForLogin();
        }
        else{
            Log.w(TAG2, "************Device not connected to the bluetooth server");
        }
    }
    public void loopingTheSonar(){
        Log.i(TAG2, "sonar value is:************      " + sonarValue);
        if (sonarValue != 0)
            mHandler.postDelayed(sonar, 500);
    }

    /**
     * Runns a thread to read the sonar value every X amount fo seconds
     */
    private Runnable sonar = new Runnable() {
        @Override
        public void run() {
            readValue();    //reads the sonar
            //add a global variable and set it here to stop the sonar
            loopingTheSonar();
        }
    };

    /**
     * changes the value of ***sonarValue****
     * @param view
     */
    public void setSonarValue(View view){
        if (sonarValue == 0)
            sonarValue = 1;
        else sonarValue = 0;
    }

    public void createNewUser(View view){
        TransitionManager.go(newProfile);
    }
    public void enrollFinger(){
        if (mConnectedGatt != null){
            BluetoothGattCharacteristic characteristic;
            characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                    .getCharacteristic(LIGHT_CHAR);
            characteristic.setValue(new byte[]{0x02});
            mConnectedGatt.writeCharacteristic(characteristic);
            Log.w(TAG2, "*******enrollFinger: send to case 02, enroll finger");
        }

    }

    public void setLeftSignal(){
        if (mConnectedGatt != null){
            BluetoothGattCharacteristic characteristic;
            characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                    .getCharacteristic(LIGHT_CHAR);
            characteristic.setValue(new byte[]{0x07});
            mConnectedGatt.writeCharacteristic(characteristic);
            Log.w(TAG2, "*******setLeftSignal: send to case 07, enroll finger");
        }
    }

    public void setRightSignal(){
        if (mConnectedGatt != null){
            BluetoothGattCharacteristic characteristic;
            characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                    .getCharacteristic(LIGHT_CHAR);
            characteristic.setValue(new byte[]{0x08});
            mConnectedGatt.writeCharacteristic(characteristic);
            Log.w(TAG, "*******setRightSiganl: send to case 08, enroll finger");
        }
    }

    public void setLights(){
        if (mConnectedGatt != null){
            BluetoothGattCharacteristic characteristic;
            characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                    .getCharacteristic(LIGHT_CHAR);
            characteristic.setValue(new byte[]{0x06});
            mConnectedGatt.writeCharacteristic(characteristic);
            Log.w(TAG, "*******setLights: send to case 06, enroll finger");
        }
    }


    public void createFingerId(View view){
        //userId = FPSvalue;  //user ID is no longer null
        //FPSvalue = 200.00;

        final EditText myText = (EditText)findViewById(R.id.username);
        final EditText myPass = (EditText)findViewById(R.id.password);
        username = myText.getText().toString();
        password = myPass.getText().toString();
        mProgress.setMessage("Scan Finger 3 Times");
        enrollFinger();
        signToDb = true;
        listenForLogin();
        //Log.i(TAG2, "createFingerId: username =" + username + " and password=" + password);
        //Log.i(TAG2, "createFingerId: enroll finger");
        //TransitionManager.go(fingerScanning1, transitionMgr);
    }
    public void createFingerId2(View view) {
        TransitionManager.go(fingerScanning2);
    }
    public void createFingerId3(View view) {
        TransitionManager.go(fingerScanning3);
    }

    public void backToLogin(View view){

        TransitionManager.go(logIn);
    }
    /**
     * Reads the value from the LIGHT_SERVICE
     */
    private void readValue(){
        if (mConnectedGatt != null) {
            Log.i(TAG, "*****Reading LED");
            BluetoothGattCharacteristic characteristic;
            characteristic = mConnectedGatt.getService(SONAR_SERVICE)
                    .getCharacteristic(SONAR_CHAR);
            Log.w(TAG, "*******readNextSensor: read value...");
            mConnectedGatt.readCharacteristic(characteristic);
        }
    }

    public void readHR(){
        if (mConnectedGatt != null) {
            Log.i(TAG2, "*****Reading PULSE");
            BluetoothGattCharacteristic characteristic;
            characteristic = mConnectedGatt.getService(PULSE_SERVICE)
                    .getCharacteristic(PULSE_CHAR);
            Log.w(TAG, "*******reading the Heart rate value");
            Log.w(TAG, "Pulse char is: "+ characteristic.getUuid());
            mConnectedGatt.readCharacteristic(characteristic);

        }
    }

    public void readFPS(){
        if (mConnectedGatt != null) {
            Log.i(TAG2, "*****Reading FPS");
            BluetoothGattCharacteristic characteristic;
            characteristic = mConnectedGatt.getService(FPS_SERVICE)
                    .getCharacteristic(FPS_CHAR);
            Log.w(TAG2, "*******readFPS: read value...");
            mConnectedGatt.readCharacteristic(characteristic);
        }
    }


    public void leftSignal(View view){
        setLeftSignal();
    }

    public void rightSignal(View view){
        setRightSignal();
    }

    public void turnOnLights(View view){
        setLights();
    }

    public void unlockTheBike(){
        BluetoothGattCharacteristic characteristic;
        Log.w(TAG2, "*******Enabling Finger print scanner to read");
        characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                .getCharacteristic(LIGHT_CHAR);
        characteristic.setValue(new byte[]{0x0A});
        Log.w(TAG3, "*****bike was unlock");
        mConnectedGatt.writeCharacteristic(characteristic);
    }

    public void lockTheBike(){
        BluetoothGattCharacteristic characteristic;
        Log.w(TAG2, "*******Enabling Finger print scanner to read");
        characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                .getCharacteristic(LIGHT_CHAR);
        characteristic.setValue(new byte[]{0x09});
        Log.w(TAG3, "*****bike was lock");
        mConnectedGatt.writeCharacteristic(characteristic);
    }

    public void enableFPS(){
        BluetoothGattCharacteristic characteristic;
        Log.w(TAG2, "*******Enabling Finger print scanner to read");
        characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                .getCharacteristic(LIGHT_CHAR);
        characteristic.setValue(new byte[]{0x03});
        Log.w(TAG2, "*****Fingerprint scanner enabled");
        mConnectedGatt.writeCharacteristic(characteristic);
    }
    public void deleteSelectedFinger(){
        BluetoothGattCharacteristic characteristic;
        Log.w(TAG2, "*******Enabling Finger print scanner to read");
        characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                .getCharacteristic(LIGHT_CHAR);
        characteristic.setValue(new byte[]{0x05});
        Log.w(TAG2, "*****Fingerprint scanner enabled");
        mConnectedGatt.writeCharacteristic(characteristic);
    }

    public void deleteAllFingers(){
        BluetoothGattCharacteristic characteristic;
        Log.w(TAG2, "*******Enabling Finger print scanner to read");
        characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                .getCharacteristic(LIGHT_CHAR);
        characteristic.setValue(new byte[]{0x04});
        Log.w(TAG2, "*****deleting all fingers");
        mConnectedGatt.writeCharacteristic(characteristic);
    }

    public void resetId(){
        BluetoothGattCharacteristic characteristic;
        Log.w(TAG2, "*******Enabling Finger print scanner to read");
        characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                .getCharacteristic(LIGHT_CHAR);
        characteristic.setValue(new byte[]{0x0B});
        Log.w(TAG3, "*****SENS A CASE 11");
        mConnectedGatt.writeCharacteristic(characteristic);
    }

    public void resetMachine(){
        BluetoothGattCharacteristic characteristic;
        Log.w(TAG2, "*******Enabling Finger print scanner to read");
        characteristic = mConnectedGatt.getService(LIGHT_SERVICE)
                .getCharacteristic(LIGHT_CHAR);
        characteristic.setValue(new byte[]{0x0C});
        Log.w(TAG2, "*****deleting all fingers");
        mConnectedGatt.writeCharacteristic(characteristic);
    }

    private void disconnectSystem(){
        if(mConnectedGatt == null)
            Log.w(TAG, "NULL VALUE ON GATT");
        else {
            Log.w(TAG, "DISCONNECTING");
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }

        /**
         if(mConnectedGatt!=null){
         mConnectedGatt.disconnect();
         mConnectedGatt = null;
         }
         */
    }
    public void removeUser(View view){
        mProgress.setMessage("Scan Finger To Remove");
        mProgress.show();
        mHandler.postDelayed(idGone, 200);
        mHandler.postDelayed(idReallyGone, 1800);
        deleteSelectedFinger();
        //deleteUser(1);


    }
    private Runnable idReallyGone = new Runnable() {
        @Override
        public void run() {
            mProgress.hide();
        }
    };

    private Runnable idGone = new Runnable() {
        @Override
        public void run() {
            mProgress.show();
        }
    };

    private boolean deleteUser(Integer id){
        MyDbHandler dbHandler = new MyDbHandler(this, null, null, 1);
        boolean result = dbHandler.deleterUser(id);
        return result;

    }

    private String findUser(Integer id){
        MyDbHandler dbHandler = new MyDbHandler(this, null, null, 1);
        Users user = dbHandler.findUser(id);
        if (user != null){
            return user.getUsername();
        }
        else return null;

    }

    private void signUp(int id, String username, String password){
        MyDbHandler dbHandler = new MyDbHandler(this, null, null, 1);
        Users user = new Users(id, username, password);
        dbHandler.addUser(user);
    }
    public void unLockBike(View view){
        lockValue = 1;
    }

    public void removeAllUsers(View view){
        deleteAllFingers();
    }

    /**
     * disconnects the user from the main activity and takes them to the login page
     * @param view
     */
    public void disconnetFromBike(View view){
        Log.i(TAG3, "Value of sonar change to 0");
        FPSvalue = 200.00;
        sonarValue = 0;
        username = "";
        password = "";
        mProgress.setMessage("Logging out");
        mHandler.postDelayed(progressToLogin, 700);
        mHandler.postDelayed(transitionToLogin, 1500);
    }
    private Runnable transitionToLogin = new Runnable() {
        @Override
        public void run() {
            resetId();
            TransitionManager.go(logIn);
        }
    };

    private Runnable progressToLogin = new Runnable() {
        @Override
        public void run() {
            mProgress.show();
        }
    };

    public void mapActivity(View view){
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        final TextView speed = (TextView) findViewById(R.id.speed);
        if (location == null){
            speed.setText("--.--");
        }
        else{
            float mySpeed = location.getSpeed();
            double mPerHour;
            mPerHour = mySpeed * 60 * 60 / 1609.34;
            speed.setText(String.format("%.2f ", mPerHour));
        }


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

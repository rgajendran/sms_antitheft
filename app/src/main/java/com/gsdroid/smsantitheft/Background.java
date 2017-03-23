package com.gsdroid.smsantitheft;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Background extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public DatabaseHelper myDb;
    protected DevicePolicyManager dPM;
    protected ComponentName cn;
    private GoogleApiClient mGoogleApiClient;
    private String phoneForLoc;
    private Context mContext;
    private String batPhone, pNuSend;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        myDb = new DatabaseHelper(getBaseContext());
        cn = new ComponentName(getBaseContext(), DeviceAdmin.class);
        dPM = (DevicePolicyManager) getBaseContext().getSystemService(DEVICE_POLICY_SERVICE);

        if (intent.getStringExtra("id").equals("phone")) {
            String phone = intent.getStringExtra("num");
            String message = intent.getStringExtra("message");
            handleSMS(phone, message);
            Message.deviceLog(getBaseContext(), "SMS from: " + phone + " , Message:" + message);
        } else if(intent.getStringExtra("id").equals("sms")){
            String message = intent.getStringExtra("message");
            String phone = intent.getStringExtra("phone");
            if(!phone.equals("null")){
                sendSMS(phone,message);
            }
        }
        return START_NOT_STICKY;
    }

    public void handleSMS(String phone, String message) {
        String storedPhone1 = Message.GetSP(getBaseContext(), "Welcome_Phone", "secure_phone1", "NO");
        String storedPhone2 = Message.GetSP(getBaseContext(), "Welcome_Phone", "secure_phone2", "NO");

        if (phone.equals(storedPhone1) || phone.equals(storedPhone2)) {
            String[] split = message.split(" ");
            if (split.length == 4 || split.length == 5) {
                if (split[0].equals(getBaseContext().getResources().getString(R.string.command_1))) {
                    String password = Message.GetSP(getBaseContext(), "Welcome_Password", "secure_pass", "NOPASS");
                    if (!password.equals("NOPASS")) {
                        if (split[1].equals(Message.md5(password+split[2]))) {
                            switchExecute(split, phone);
                        }else {
                            Message.tag("Secure Password and Received Password doesn't match");
                        }
                    }else {
                        Message.tag("Secure password is not set");
                    }
                }
            }else if(split.length == 3) {
                if(split[0].equals("smstheft")){
                    String password = Message.GetSP(getBaseContext(), "Welcome_Password", "secure_pass", "NOPASS");
                    if (!password.equals("NOPASS")) {
                        if(split[1].equals(password)){
                            String[] strings = {split[0],split[1],"2",split[2]};
                            switchExecute(strings,phone);
                        }else {
                            Message.tag("Unencrypted secure password and received password doesn't match");
                        }
                    }else{
                        Message.tag("Secure password is not set");
                    }

                } else {
                    Message.tag("Invalid base command : " + split[0]);
                }
            }else {
                Message.tag("Command length is inappropriate for ("+split[0]+")");
            }

            Message.tag(message);
        } else {
            Message.tag("Incorrect Phone");
        }
    }

    protected void switchExecute(String[] word, String phone) {

        switch (word[3]) {

            case "lock":
                if(word[0].equals("smsanti")) {
                    if (dPM.isAdminActive(cn)) {
                        if (!word[4].trim().isEmpty()) {
                            if (word[4].length() >= 4 && word[4].length() <= 10) {
                                dPM.resetPassword(word[4], DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                                dPM.lockNow();
                                sendSMS(phone, "Password Changed : " + word[4]);
                            } else {
                                Message.deviceLog(getBaseContext(), "lock password length should be 4-10");
                                sendSMS(phone, "lock password length should be 4-10");
                            }

                        } else {
                            Message.deviceLog(getBaseContext(), "Unauthorized lock password type");
                            sendSMS(phone, "Unauthorized lock password type");
                        }

                    } else {
                        NoActiveAdmin();
                    }
                }
                break;

            case "erase":
                if(word[0].equals("smsanti")) {
                    if (dPM.isAdminActive(cn)) {
                        dPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
                    } else {
                        NoActiveAdmin();
                    }
                }
                break;

            case "lockdisplay":
                if (dPM.isAdminActive(cn)) {
                    dPM.lockNow();
                } else {
                    NoActiveAdmin();
                }
                break;

            case "silent":
                AudioManager am = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                sendSMS(phone, "Activated SILENT");
                break;

            case "normal":
                AudioManager am1 = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                am1.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                sendSMS(phone, "Activated NORMAL");
                break;

            case "wifion":
                WifiManager wifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
                wifiManager.startScan();
                break;

            case "wifioff":
                WifiManager wifiManager1 = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager1.setWifiEnabled(false);
                break;

            case "bluetoothon":
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();
                }
                break;

            case "bluetoothoff":
                BluetoothAdapter bluetoothAdapter1 = BluetoothAdapter.getDefaultAdapter();
                bluetoothAdapter1.disable();
                break;

            case "wifiname":
                WifiManager wifiManager2 = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifiManager2.getConnectionInfo();
                if (info != null) {
                    sendSMS(phone, "Your device wifi ssid : " + info.getSSID());
                } else {
                    sendSMS(phone, "Wifi Info not available");
                }
                break;

            case "location":
                if (mGoogleApiClient == null) {
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .build();
                }
                mGoogleApiClient.connect();
                phoneForLoc = phone;
                break;

            case "siminfo":
                TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                sendSMS(phone, "SIM Serial Number : " + tm.getSimSerialNumber() + ", SIM Network Operator : " + tm.getSimOperatorName());
                break;

            case "phonenumber":
                String pNu = getPhoneNumber();

                if(pNu.equals("")){
                    pNuSend = "Not Defined";
                }else{
                    pNuSend = pNu;
                }
                sendSMS(phone,"Device Phone number :"+pNuSend);
                break;

            case "battery":
                mContext = getApplicationContext();
                IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                mContext.registerReceiver(mBroadcastReceiver, iFilter);
                batPhone = phone;
                break;
        }
        onDestroy();
    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
            float percentage = level/ (float) scale;
            sendSMS(batPhone,"Device battery level : "+String.valueOf(Math.round(percentage*100)+"%"));
            mContext.unregisterReceiver(mBroadcastReceiver);
        }
    };

    public String getPhoneNumber(){
        TelephonyManager tm1 = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        return tm1.getLine1Number();
    }

    public void NoActiveAdmin() {
        Message.adminLog(getBaseContext(), "Device Admin is not active on your device");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Message.tag("onDestroy Service");
    }

    public void sendSMS(String phoneNo, String msg) {

        if(Message.GetSP(getBaseContext(),"Settings","Response","ON").equals("ON")) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Message.tag("Message Sent"+phoneNo);

        } catch (Exception ex) {
            Message.tag(ex.getMessage());
            ex.printStackTrace();
        }
            Message.tag("Reply : " + msg);
        }else{
            Message.deviceLog(getBaseContext(), "SMS Response couldn't be sent, Turn on the service in settings option");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location != null){
            sendSMS(phoneForLoc,"http://www.google.com/maps/place/"+location.getLatitude()+","+location.getLongitude()+"/@"+
                    location.getLatitude()+","+location.getLongitude()+",17z");
        }else{
            sendSMS(phoneForLoc,"Location Not Available, but phone is active. Try other commands");
        }

        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Message.tag("Location Connection Failed");
    }

}

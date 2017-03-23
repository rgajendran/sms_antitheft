package com.gsdroid.smsantitheft;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Message {

    public static void toast(Context c, String s){
        Toast.makeText(c, s, Toast.LENGTH_LONG).show();
    }

    public static String GetSP(Context context,String lib,String key, String defaults){
        SharedPreferences sp = context.getSharedPreferences(lib, Context.MODE_PRIVATE);
        String s= sp.getString(key, defaults);
        return s;
    }

    public static void SetSP(Context context, String lib, String key, String value){
        SharedPreferences sp = context.getSharedPreferences(lib, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value).apply();
    }

    public static void tag(String s){
        Log.i("TAG", s);
    }

    public static void log(Context context,String s){
        Intent intent = new Intent(context,Background.class);
        intent.putExtra("id","log");
        intent.putExtra("message",s);
        context.startService(intent);
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    //log
    public static void deviceLog(Context context,String message) {
        if(Message.GetSP(context,"Settings","LogDevice","ON").equals("ON")) {
            DatabaseHelper myDb = new DatabaseHelper(context);
            myDb.insertData(Message.time(), message);
        }
    }

    public static void adminLog(Context context,String message) {
        if(Message.GetSP(context,"Settings","LogAdmin","ON").equals("ON")) {
            DatabaseHelper myDb = new DatabaseHelper(context);
            myDb.insertData(Message.time(), message);
        }
    }

    public static void appLog(Context context,String message) {
        if(Message.GetSP(context,"Settings","LogApp","ON").equals("ON")) {
            DatabaseHelper myDb = new DatabaseHelper(context);
            myDb.insertData(time(), message);
        }
    }

    public static String time() {
        String format = "dd-MM-yyyy HH:mm:ss";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(c.getTime());
    }

}

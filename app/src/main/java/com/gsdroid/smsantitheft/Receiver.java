package com.gsdroid.smsantitheft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;


public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Message.tag("BOOT COMPLETE");
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if(Message.GetSP(context,"Sim","SimNo","null").equals(tm.getSimSerialNumber())){
            Message.tag("SIM Number Match");
        }else {
            Intent intent1 = new Intent(context,Background.class);
            intent1.putExtra("id","sms");
            intent1.putExtra("message","Your sim card has been changed");
            intent1.putExtra("phone",Message.GetSP(context,"Welcome_Phone","secure_phone1","null"));
            context.startService(intent1);
            Message.tag("SIM Number Doesn't Match");
        }
    }

}

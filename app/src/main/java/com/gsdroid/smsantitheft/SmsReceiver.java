package com.gsdroid.smsantitheft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Message.tag("SMS Broadcast on Recieve");
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    if(Message.GetSP(context,"Welcome_Phone","secure_phone1","null").equals(phoneNumber) ||
                            Message.GetSP(context,"Welcome_Phone","secure_phone2","null").equals(phoneNumber)){
                        String senderNum = phoneNumber;
                        String message = currentMessage.getDisplayMessageBody();
                        Intent backgroundService = new Intent(context,Background.class);
                        backgroundService.putExtra("id","phone");
                        backgroundService.putExtra("num",senderNum);
                        backgroundService.putExtra("message",message);
                        context.startService(backgroundService);
                    }
                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Message.tag("Exception smsReceiver" + e);

        }
    }
}

package com.gsdroid.smsantitheft;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;


public class DeviceAdmin extends DeviceAdminReceiver {

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        super.onPasswordChanged(context, intent);
        Message.adminLog(context, "Password Changed");
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        super.onPasswordFailed(context, intent);
        Message.deviceLog(context, "Device Login Attempt Failed");
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Message.adminLog(context, "Device Admin Enabled");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Message.adminLog(context, "Device Admin Disabled");
    }
}

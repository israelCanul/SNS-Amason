package androidtest.amazonaws.com.androidmobilepushapp.GCM;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by icanul on 2/22/16.
 */
public class GCMBroadcastReceiver  extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        // This is the Intent to deliver to our service.
        Intent service = new Intent(context, GCMIntentService.class);
        service.putExtras(extras);
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
        setResultCode(Activity.RESULT_OK);
    }

}

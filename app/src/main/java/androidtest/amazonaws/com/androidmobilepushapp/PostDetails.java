package androidtest.amazonaws.com.androidmobilepushapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by icanul on 2/23/16.
 */
public class PostDetails extends Activity{

    private static final String TAG = "POST DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        Log.i(TAG, getIntent().getStringExtra("post_title"));
        Log.i(TAG, getIntent().getStringExtra("post_url"));
        Log.i(TAG, getIntent().getStringExtra("post_id"));
        Log.i(TAG, getIntent().getStringExtra("post_author"));
        NotificationManager mNotification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification.cancel(R.string.notification_number);
    }


}

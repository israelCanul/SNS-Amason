package androidtest.amazonaws.com.androidmobilepushapp.GCM;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidtest.amazonaws.com.androidmobilepushapp.AndroidMobilePushApp;
import androidtest.amazonaws.com.androidmobilepushapp.PostDetails;
import androidtest.amazonaws.com.androidmobilepushapp.R;

/**
 * Created by icanul on 2/22/16.
 */
public class GCMIntentService extends IntentService {
    private static final String TAG = " IntentService ";
    private GoogleCloudMessaging gcm;
    public static SharedPreferences savedValues;
    public Context context=null;
    public JSONObject mensaje = new JSONObject();


    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        final String preferences = getString(R.string.preferences);
        savedValues = getSharedPreferences(preferences, Context.MODE_PRIVATE);

        if(Build.VERSION.SDK_INT >  9){
            savedValues = getSharedPreferences(preferences, Context.MODE_APPEND);
        }

        SharedPreferences savedValues = PreferenceManager.getDefaultSharedPreferences(context);

        if(savedValues.getBoolean(getString(R.string.first_launch), true)){
            SharedPreferences.Editor editor = savedValues.edit();
            register();
            editor.putBoolean(getString(R.string.first_launch), false);
            editor.commit();
        }

    }




    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Entro");

        String message = "";
        String txtTitulo = "";
        String txtSubtitulo = "";
        String type="";

        Bundle extras = intent.getExtras();

        if (extras != null) {
            for (String key : extras.keySet()) {
                try {

                    //se guarda el JSON de los datos recibidos
                    mensaje.put(key, extras.getString(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (key.equals("from")) {
                    txtTitulo = "From: Royal Reporter - " + extras.getString(key);
                }
                if (key.equals("default") || key.equals("message")) {
                    txtSubtitulo = extras.getString(key);
                }
                if (key.equals("type")) {
                    type = extras.getString(key);
                }

            }

            //se envia la notificacion
            if (type.equals("post")) {
                //newPost(mensaje);
                postNotification(context, txtTitulo, txtSubtitulo,mensaje);
            }else{
                postNotification(context, txtTitulo, txtSubtitulo);
            }


        }

        Log.i(TAG, txtTitulo + " - " + txtSubtitulo);
        Log.i(TAG, String.valueOf(mensaje));
    }

     public static void setPreferences(Context context, String user, String regId)
    {
        final String preferences = context.getString(R.string.preferences);
        savedValues = context.getSharedPreferences(preferences, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = savedValues.edit();
        editor.putString(context.getString(R.string.PROPERTY_USER), user);
        editor.putString(context.getString(R.string.PROPERTY_REG_ID), regId);
        editor.putInt(context.getString(R.string.PROPERTY_APP_VERSION), 1);
        editor.commit();
    }

    private void register() {

        new AsyncTask(){
            public String token="";
            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                setPreferences(context, "Android", token);
            }

            protected Object doInBackground(final Object... params) {

                try {
                    String iid = InstanceID.getInstance(context).getId();
                    String authorizedEntity = getString(R.string.project_number); // Project id from Google Developer Console
                    String scope = "GCM"; // e.g. communicating using GCM, but you can use any

                    token = InstanceID.getInstance(context).getToken(authorizedEntity,scope);

                    String platformApplicationArn = getString(R.string.platformApplicationArn);
                    AWSCredentials awsCredentials = new BasicAWSCredentials(getString(R.string.credential), getString(R.string.secret_credential));
                    AmazonSNSClient pushClient = new AmazonSNSClient(awsCredentials);
                    CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
                    String customPushData = "my custom data";
                    platformEndpointRequest.setCustomUserData(customPushData);
                    platformEndpointRequest.setToken(token);

                    platformEndpointRequest.setPlatformApplicationArn(platformApplicationArn);
                    pushClient.setRegion(Region.getRegion(Regions.US_WEST_2));

                    CreatePlatformEndpointResult result = pushClient.createPlatformEndpoint(platformEndpointRequest);
                    Log.e("Registration result : ", result.toString());

                }
                catch (IOException e) {
                    Log.i("Registration Error", e.getMessage());
                }
                return true;
            }
        }.execute(null, null, null);

    }

    protected static void postNotification(Context context,String titulo,String subtitulo){
        Intent i=new Intent(context, AndroidMobilePushApp.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,0);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        CharSequence ticker="Notificacion";
        Notification notification = new NotificationCompat.Builder(context)
                .setTicker(ticker)
                .setContentTitle(titulo)
                .setContentText(subtitulo)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .addAction(R.drawable.ic_launcher, ticker, pendingIntent)
                .setVibrate(new long[]{100, 1000, 100, 500})
                .build();


        mNotificationManager.notify(R.string.notification_number, notification);
    }
    protected static void postNotification(Context context,String titulo,String subtitulo,JSONObject obj){
        String title="";
        String url="";
        String id="";
        String author="";
        try {

            title= obj.getString("post_title");
            url= obj.getString("post_url");
            id= obj.getString("post_id");
            author= obj.getString("post_author");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent i=new Intent(context, PostDetails.class);
        i.putExtra("post_title", title);
        i.putExtra("post_url", url);
        i.putExtra("post_id", id);
        i.putExtra("post_author", author);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,0);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        CharSequence ticker="Notificacion";
        Notification notification = new NotificationCompat.Builder(context)
                .setTicker(ticker)
                .setContentTitle(titulo)
                .setContentText(subtitulo)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .addAction(R.drawable.ic_launcher, ticker, pendingIntent)
                .setVibrate(new long[]{100, 1000, 100, 500})
                .build();

        mNotificationManager.notify(R.string.notification_number, notification);
    }



}

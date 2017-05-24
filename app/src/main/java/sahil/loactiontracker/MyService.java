package sahil.loactiontracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private GoogleApiClient apiClient;
    private Location location;
    private LocationRequest request;
    static String address;
    static boolean running = false;
    static boolean connectionStatus=false;
    private final int LOCATION_UPDATE_INTERVAL=10*1000;//(Milliseconds)
    private final int FAST_LOCATION_UPDATE_INTERVAL=5*1000;//(Milliseconds)
    private final int UPLOAD_INTERVAL=5*1000;//(Milliseconds)
    private final int DISCONNECTED_TIME=5*1000;
    private String name=null;
    private boolean checkServices() {
        int check = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (check != ConnectionResult.SUCCESS) {
            Toast.makeText(getApplicationContext(), "Get Google Play services first!", Toast.LENGTH_SHORT).show();
            running=false;
            return false;
        }
        return true;
    }

    protected void buildClient() {
        /*
        * Initial make up for callbacks
        * */
        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        /*
        * Permission check and Location Change Listener
        * */
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"Please provide necessary permissions",Toast.LENGTH_SHORT).show();
            running=false;
            return;
        }
        connectionStatus=true;
        Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_SHORT).show();
        location = LocationServices.FusedLocationApi.getLastLocation(apiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request,this);
    }



    @Override
    public void onConnectionSuspended(int i) {
        connectionStatus=false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        connectionStatus=false;
    }

    @Override
    public void onLocationChanged(Location location) {
//        Log.d("Location: ",""+location.getLatitude()+","+location.getLongitude());
        /*
        * Method is called whenever location is updated
        * */
        this.location=location;
    }

    private final class ServiceHandler extends Handler
    {
        public ServiceHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            final Geocoder geocoder= new Geocoder(getApplicationContext(), Locale.getDefault());
            final Handler h=new Handler();
            h.post(new Runnable() {
                @Override
                public void run() {
                    Calendar c=Calendar.getInstance();
                    String date=c.get(Calendar.DAY_OF_MONTH)+"|"+c.get(Calendar.MONTH)+"|"+c.get(Calendar.YEAR);
                    String time=c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND);

                    if(running)
                    {
                        List<android.location.Address> myAdd=null;

                        if(location!=null)

                        {
                            try
                            {
                                myAdd=geocoder.getFromLocation(
                                        location.getLatitude(),location.getLongitude(),1);
                            }
                            catch (IOException e)
                            {
                                Toast.makeText(getApplicationContext(),"Service not Available",Toast.LENGTH_SHORT).show();
                            }
                            catch (IllegalArgumentException e)
                            {
                                Toast.makeText(getApplicationContext(),"Invalid coordinates",Toast.LENGTH_SHORT).show();
                            }
                            if(myAdd==null || myAdd.size()==0)
                            {
                                Toast.makeText(getApplicationContext(),"Address Unavailable",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                StringBuilder builder=new StringBuilder();
                                for(int  i=0;i<myAdd.get(0).getMaxAddressLineIndex();i++)
                                {
                                    builder.append(myAdd.get(0).getAddressLine(i));
                                    builder.append("\n");
                                }
                                address=builder.toString();
                                Toast.makeText(getApplicationContext(),address,Toast.LENGTH_SHORT).show();
                                DatabaseReference ref=MainActivity.getDatabaseInstance("/"+name+"/"+date+"/"+"/"+time);
                                Log.d("Location: ","/"+name+"/"+date+"/"+"/"+time);
                                Log.d("Address", address);

                                ref.setValue(address);
                            }
                        }
                        if(connectionStatus)
                        {
                            h.postDelayed(this,UPLOAD_INTERVAL);
                        }
                        else
                        {
                            h.postDelayed(this,DISCONNECTED_TIME);
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Disconnected",Toast.LENGTH_SHORT).show();
                        stopSelf();
                    }
                }
            });

        }
    }
    @Override
    public void onCreate() {
        /*
        * Create a separate background thread
        * */
        HandlerThread thread=new HandlerThread("ARGS", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        SharedPreferences preferences=getSharedPreferences(RegisterationActivity.prefName, Context.MODE_PRIVATE);
        name=preferences.getString(RegisterationActivity.userName,"null");
        mServiceLooper=thread.getLooper();
        mServiceHandler=new ServiceHandler(mServiceLooper);
        if(checkServices())
        {
            request =new LocationRequest()
                    .setInterval(LOCATION_UPDATE_INTERVAL)
                    .setFastestInterval(FAST_LOCATION_UPDATE_INTERVAL)
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            buildClient();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg=mServiceHandler.obtainMessage();
        msg.arg1=startId;
        mServiceHandler.sendMessage(msg);
        running=true;
        if(apiClient!=null)
        {
            apiClient.connect();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }

    @Override
    public void onDestroy() {
        running=false;
    }
}

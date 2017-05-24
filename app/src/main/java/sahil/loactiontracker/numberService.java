package sahil.loactiontracker;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.os.Handler;
import android.widget.Toast;

public class numberService extends Service {

    public static int number;
    public static boolean status;
    Handler handler;
    @Override
    public void onDestroy() {
        super.onDestroy();
        status=false;
        Log.d("Service : ","Destroyed");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        status=false;
        Log.d("Service: ","Unbound");
        return false;
    }

    @Override
    public void onCreate() {
        status=true;
        Log.d("Sevice Oncreate: ","Called");
        handler=new Handler();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                number++;
                Log.d("Handler Thread Run: ","Number= "+number);
                if(status)
                handler.postDelayed(this,2000);
            }
        };
        handler.post(runnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

package sahil.loactiontracker;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


public class MyIntentService extends IntentService {
    public MyIntentService() {
        super("Hello Intent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Handler handler=new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<20;i++)
                {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("onHandleIntent: ","Running");
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(),"Service",Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }
}

package sahil.loactiontracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {


    TextView view;
    Button tracker;
    private final int ADDRESS_REFRESH_INTERVAL=1000;//(Milliseconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view=(TextView) findViewById(R.id.numVal);
        view.setText("No updates available");
        tracker=(Button) findViewById(R.id.tracker);
        tracker.setText(R.string.START_BUTTON_TEXT);
        if(!MyService.running)
        {
            /*
            * Begin the upload service if not already running
            * */
            startService(new Intent(this,MyService.class));
        }
        else
        {
            tracker.setText(R.string.START_BUTTON_TEXT);
        }

        /*
        * Regularly update current location
        * */
        watchNumber();
    }

    public void stateToggle(View view)
    {
        /*
        * Handle button click for starting or halting the upload service
        * */
        if(!MyService.running)
        {
            startService(new Intent(this,MyService.class));
        }
        else
        {
            /*
            * Set running status of service to false to terminate the thread
            * */
            MyService.running=false;
        }
    }


    private void watchNumber() {

        final Handler handler=new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                /*
                * Thread for regularly updating address visible to user
                * */
                if(MyService.address!=null)
                {
                    view.setText(""+MyService.address);
                }
                if(MyService.running)
                {
                    tracker.setText(R.string.STOP_BUTTON_TEXT);
                }
                else
                {
                    tracker.setText(R.string.START_BUTTON_TEXT);
                }
                handler.postDelayed(this,ADDRESS_REFRESH_INTERVAL);
            }
        });
    }

    public static DatabaseReference getDatabaseInstance(String path)
    {

        /*
        * Generate reference to absolute path in JSON TREE structured database
        * For storing and retrieving data
        * */
        DatabaseReference database=FirebaseDatabase.getInstance().getReference(path);
        return database;
    }
}

package sahil.loactiontracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterationActivity extends AppCompatActivity {
    EditText editText;
    Button start;
    SharedPreferences preferences;
    public static final String prefName="userDetails";
    public static final String userName="username";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        start=(Button) findViewById(R.id.submit);
        editText=(EditText) findViewById(R.id.nameField);
        final SharedPreferences preferences=getSharedPreferences(prefName, MODE_PRIVATE);
        String initial = preferences.getString(userName,"_empty");
        final Intent i=new Intent(this,MainActivity.class);
        if(initial.compareTo("_empty()")!=0)
        {
            startActivity(i);
        }
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name=editText.getText().toString();
                SharedPreferences.Editor editor= preferences.edit();
                editor.putString(userName, name);
                editor.commit();
                startActivity(i);
            }
        });
    }
}

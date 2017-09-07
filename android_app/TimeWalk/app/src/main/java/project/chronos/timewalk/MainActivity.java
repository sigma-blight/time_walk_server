package project.chronos.timewalk;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG = "TIME_WALK_LOG";

    Client _client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        _client = new Client();

        if (!_client.connect()) {
            Log.e(LOG, "Connection Failed");
        } else {
            collectData();
        }
    }

    private void collectData() {
        Client.Data<ArrayList<String>> landmarks;
        landmarks = _client.landmarks();

        if (landmarks.failed) {
            Log.e(LOG, "Failed to Get Landmarks: " + landmarks.error_msg);
        } else {
            for (String landmark : landmarks.result) {
                Log.i(LOG, landmark);

                Client.Data<Client.GPSCoord> gpsData;
                Client.Data<ArrayList<String>> imageNames;

                gpsData = _client.gpsCoord(landmark);
                imageNames = _client.imageNames(landmark);

                if (gpsData.failed) {
                    Log.e(LOG, "GPS Failed: " + gpsData.error_msg);
                } else {
                    Log.i(LOG, "   @ " + Double.toString(gpsData.result.longitude) +
                        ", " + Double.toString(gpsData.result.latitude));
                }

                if (imageNames.failed) {
                    Log.e(LOG, "Image Names Failed: " + imageNames.error_msg);
                } else {
                    for (String image_name : imageNames.result) {
                        Log.i(LOG, "   " + image_name);

                        Client.Data<String> text;
                        text = _client.getText(landmark, image_name);

                        if (text.failed) {
                            Log.e(LOG, "Text Failed: " + text.error_msg);
                        } else {
                            Log.i(LOG, "      - " + text.result);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

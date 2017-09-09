package project.chronos.timewalk;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class HomeScreenActivity extends AppCompatActivity {

    public static Client client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        //  Setup Button Callbacks

        setupButtonOnClicks();

        // Time Walk Setup Last

        startProjectTimeWalk(getApplicationContext());
    }

    private void setupButtonOnClicks() {

        Button search = (Button) findViewById(R.id.home_screen_search_btn);
        Button routes = (Button) findViewById(R.id.home_screen_routes_btn);
        Button person = (Button) findViewById(R.id.home_screen_personalise_btn);

        // setup search activity

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeScreenActivity.this, SearchActivity.class));
            }
        });

        // setup routes activity

        routes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(HomeScreenActivity.this, RoutesActivity.class));
            }
        });

        // setup personalise activity

        person.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(HomeScreenActivity.this, PersonaliseActivity.class));
            }
        });
    }

    public static void startProjectTimeWalk(Context context) {
        client = new Client();
        Data<Boolean> data = client.connect();

        if (data.failed)
            projectTimeWalkDown(context);
    }

    public static void projectTimeWalkDown(Context context) {
        Toast error_toast = Toast.makeText(context, "Project Time Walk is Down", Toast.LENGTH_LONG);
        error_toast.show();
    }
}

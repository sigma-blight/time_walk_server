package project.chronos.timewalk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class HomeScreen extends AppCompatActivity {

    public static Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        client = new Client();
        Data<Void> result = client.connect();

        if (result.failed) {
            Toast.makeText(HomeScreen.this,
                    "Could not connect to Server",
                    Toast.LENGTH_LONG).show();
        }


        setupBtnOnClicks();
    }

    private void setupBtnOnClicks()
    {
        //Button explore = (Button) findViewById(R.id.home_screen_btn_explore);
        Button discover = (Button) findViewById(R.id.home_screen_btn_discover);
        //Button personalise = (Button) findViewById(R.id.home_screen_btn_personalise);

        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeScreen.this, ExploreScreen.class));
            }
        });

        //discover.setOnClickListener();
    }
}

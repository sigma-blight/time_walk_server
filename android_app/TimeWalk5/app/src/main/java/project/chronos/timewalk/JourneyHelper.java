package project.chronos.timewalk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.DropBoxManager;
import android.os.Environment;
import android.service.wallpaper.WallpaperService;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Fletcher on 15/10/2017.
 */

public class JourneyHelper {

    private static final String TAG = "JourneyHelperTag";
    private static File path;

    private static File listFile() {
        return new File(path + "lists.txt");
    }

    private static void createNewFile(String filename) {
        try {
            new File(path + "/" + filename).createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "Failed to create file: " + filename
                    + " -- " + e.getMessage());
        }
    }

    static void setupFileSystem(Context context) {
        path = new File(context.getFilesDir().getAbsolutePath() + "/journeys");
        path.getParentFile().mkdirs();
        path.mkdirs();

        try {
            listFile().createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "Failed to create file -- " + e.getMessage());
        }
    }

    static ArrayList<String> getJourneys(Context context)  {
        ArrayList<String> journeys = new ArrayList<>();
        try {
            File file = listFile();
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            String journey;
            while ((journey = reader.readLine()) != null) {
                journeys.add(journey);
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to get Journeys: " + e.getMessage());
        }
        return journeys;
    }

    static void setJourneys(ArrayList<String> journeys, Context context) {
        try {
            File file = listFile();
            FileOutputStream fos = new FileOutputStream(file);
            for (int i = 0; i != journeys.size(); ++i) {
                fos.write(journeys.get(i).getBytes());
                if (i < journeys.size() - 1)
                    fos.write("\n".getBytes());
            }
            fos.close();

        } catch (Exception e) {
            Log.e(TAG, "Failed to set Journeys: " + e.getMessage());
        }
    }

    static ArrayList<String> getLandmarks(String journey, Context context) {
        ArrayList<String> landmarks = new ArrayList<>();
        try {
            File file = new File(path + "/" + journey);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            String landmark;
            while ((landmark = reader.readLine()) != null) {
                landmarks.add(landmark);
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to get Landmarks: " + e.getMessage());
        }
        return landmarks;
    }

    static void setLandmarks(String journey, ArrayList<String> landmarks, Context context) {
        try {
            File file = new File(path + "/" + journey);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            for (int i = 0; i != landmarks.size(); ++i) {
                fos.write(landmarks.get(i).getBytes());
                if (i < landmarks.size() - 1)
                    fos.write("\n".getBytes());
            }
            fos.close();

        } catch (Exception e) {
            Log.e(TAG, "Failed to set Landmarks: " + e.getMessage());
        }
    }


    static void createNewJourney(Context context, View.OnClickListener callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter New Journey Name");

        EditText input = new EditText(context);
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

        class Listener implements DialogInterface.OnClickListener {

            EditText input;
            Context context;
            View.OnClickListener callback;

            public void onClick(DialogInterface dialogInterface, int i) {
                Log.e(TAG, "Adding Journey: " + input.getText().toString());
                try {

                    ArrayList<String> journeys = getJourneys(context);
                    String newJourney = input.getText().toString();

                    if (!journeys.contains(newJourney)) journeys.add(newJourney);

                    createNewFile(newJourney);
                    setJourneys(journeys, context);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to Add new Journey: " + input.getText().toString() + " -- " + e.getMessage());
                }

                callback.onClick(null);
            }
        }

        Listener listener = new Listener();
        listener.input = input;
        listener.context = context;
        listener.callback = callback;

        builder.setPositiveButton("Create", listener);
        builder.setView(input);
        builder.show();
    }

    static void addLandmarkToJourney(final String landmark, Context context) {
        class AlertBox extends AlertDialog {
            protected AlertBox(Context context) {
                super(context);

                ScrollView scrollView = new ScrollView(context);
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                final ArrayList<String> journeys = getJourneys(context);
                for (String journey : journeys) {

                    CardView card = new CardView(context);
                    card.setRadius(20);
                    card.setUseCompatPadding(true);

                    TextView txt = new TextView(context);
                    txt.setText(journey);
                    txt.setLayoutParams(params);
                    txt.setTypeface(Typeface.SERIF);
                    txt.setTextSize(20);
                    txt.setPadding(20, 20, 20, 20);
                    txt.setGravity(Gravity.CENTER);
                    txt.setBackground(context.getResources().getDrawable(R.color.commonWhite));

                    class Listener implements View.OnClickListener {

                        String journey;
                        Context context;
                        String landmark;

                        @Override
                        public void onClick(View view) {

                            Log.e(TAG, "Adding: " + landmark + " to " + journeys);
                            ArrayList<String> landmarks = getLandmarks(journey, context);
                            if (!landmarks.contains(landmark))
                                landmarks.add(landmark);
                            setLandmarks(journey, landmarks, context);

                            dismiss();
                        }
                    }

                    Listener listener = new Listener();
                    listener.journey = journey;
                    listener.context = context;
                    listener.landmark = landmark;

                    card.setOnClickListener(listener);
                    card.addView(txt);
                    layout.addView(card);
                }

                scrollView.addView(layout);
                setView(scrollView);
                setView(scrollView);
            }
        }

        AlertBox builder = new AlertBox(context);
        builder.setTitle("Choose a journey");
        builder.show();
    }




//    static File listFile(Context context) {
//        File dir = context.getFilesDir();
//        File list = new File(dir + "/files/" + LIST_FILE);
//        return list;
//    }
//
//    static HashMap<String, ArrayList<String>> readJourneys(Context context) {
//
//        HashMap<String, ArrayList<String>> data = new HashMap<>();
//
//        try {
//            File list = listFile(context);
//            if (list.getParentFile().mkdirs()) {
//                list.createNewFile();
//            }
//
//            FileInputStream fileInputStream = new FileInputStream(list);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
//
//            String line;
//            ArrayList<String> files = new ArrayList<>();
//            while ((line = reader.readLine()) != null)
//                files.add(line);
//
//            for (String fileName : files) {
//                FileInputStream fstream = context.openFileInput(fileName);
//                BufferedReader fread = new BufferedReader(new InputStreamReader(fstream));
//
//                data.put(fileName, new ArrayList<String>());
//                String landmark;
//                while ((landmark = fread.readLine()) != null) {
//                    data.get(landmark).add(landmark);
//                }
//            }
//
//        } catch (IOException e) {
//            Log.e(TAG, "readJourneys Failed: " + e.getMessage());
//        }
//
//        return data;
//    }
//
//    static void writeJourneys(HashMap<String, ArrayList<String>> data, Context context) {
//        try {
//            File list = listFile(context);
//            if (list.getParentFile().mkdirs())
//                list.createNewFile();
//
//            for (Map.Entry<String, ArrayList<String>> entry : data.entrySet()) {
//
//                FileOutputStream fileOutputStream = new FileOutputStream(LIST_FILE);
//                PrintWriter writer = new PrintWriter(fileOutputStream);
//
//                writer.println(entry.getKey()); // add journey to list file
//                fileOutputStream = new FileOutputStream(entry.getKey());
//                writer = new PrintWriter(fileOutputStream);
//
//                for (String landmark : entry.getValue())
//                    writer.println(landmark);
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "writeJourneys Failed: " + e.getMessage());
//        }
//    }
//
//    static void addToJourney(String journey, String landmark, Context context) {
//        HashMap<String, ArrayList<String>> data = readJourneys(context);
//        if (!data.get(journey).contains(landmark))
//            data.get(journey).add(landmark);
//        writeJourneys(data, context);
//    }
//
//    static void createNewJourney(final Context context) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Enter New Journey Name");
//
//        EditText input = new EditText(context);
//
//        input.setInputType(InputType.TYPE_CLASS_TEXT);
//        builder.setView(input);
//
//        class Listener implements DialogInterface.OnClickListener {
//
//            String journey;
//            Context context;
//
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                HashMap<String, ArrayList<String>> data = readJourneys(context);
//                if (!data.containsKey(journey))
//                    data.put(journey, new ArrayList<String>());
//                writeJourneys(data, context);
//            }
//        }
//
//        Listener listener = new Listener();
//        listener.context = context;
//        listener.journey = input.getText().toString();
//        builder.setPositiveButton("OK", listener);
//
//        builder.show();
//    }
//
//    static void addLandmarkToJourney(String landmark, Context context) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Add " + landmark + " to a Journey");
//
//        HashMap<String, ArrayList<String>> data = readJourneys(context);
//        for (Map.Entry<String, ArrayList<String>> entry : data.entrySet()) {
//            Button btn = new Button(context);
//
//            class Listener implements View.OnClickListener {
//
//                String journey;
//                String newLandmark;
//                Context context;
//
//                @Override
//                public void onClick(View view) {
//                    addToJourney(journey, newLandmark, context);
//                }
//            }
//
//            Listener listener = new Listener();
//            listener.journey = entry.getKey();
//            listener.newLandmark = landmark;
//            listener.context = context;
//            btn.setOnClickListener(listener);
//
//            builder.setView(btn);
//        }
//
//        builder.show();
//    }
}

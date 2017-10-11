package project.chronos.timewalk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Fletcher on 5/10/2017.
 */

public class Client {

    static final String SERVER_NAME = "deco3801-Chronos.uqcloud.net";
    static final int SERVER_PORT = 5001;
    static final long TIMEOUT = 3; // seconds
    static final String SEPARATOR = "\\|";
    static final char SEPARATOR_CHAR = '|';
    static final String DEFAULT_REGION = "Brisbane";
    private static final String TAG = "ClientTag";

    private Socket _socket;
    private PrintWriter _output;
    private BufferedReader _input;

    private Bitmap httpGetImage(String url_str) throws IOException {
        URL url = new URL(url_str);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        InputStream input = conn.getInputStream();

        return BitmapFactory.decodeStream(input);
    }

    static private abstract class Work<Return_, Params_> {
        public abstract Data<Return_> run(Params_... params_s);
    }

    static private class Worker<Return_, Params_> {

        Work<Return_, Params_> work;
        AsyncTask<Params_, Void, Data<Return_>> asyncTask;

        Worker(Work<Return_, Params_> workParam) {
            this.work = workParam;

            asyncTask = new AsyncTask<Params_, Void, Data<Return_>>() {
                @Override
                protected Data<Return_> doInBackground(Params_... params_s) {
                    return work.run(params_s);
                }
            };
        }

        Data<Return_> getResult(Params_... params_s) {
            Data<Return_> data = new Data<>();

            try {
                data = asyncTask.execute(params_s).get(TIMEOUT, TimeUnit.SECONDS);
            } catch (Exception e) {
                data.result = null;
                data.failed = true;
                data.error_message = e.getMessage();
            }

            return data;
        }
    }


    Data<Void> connect() {
        return new Worker<>(new Work<Void, Void>() {

            @Override
            public Data<Void> run(Void... voids) {
                Data<Void> data = new Data<>();

                try {
                    InetAddress addr = InetAddress.getByName(SERVER_NAME);
                    _socket = new Socket(addr, SERVER_PORT);
                    _output = new PrintWriter(_socket.getOutputStream(), true);
                    _input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

                } catch (Exception e) {
                    data.failed = true;
                    data.error_message = e.getMessage();
                }

                return data;
            }
        }).getResult();
    }

    private static class RequestCode {
        private static final int LIST_REGIONS = 0;
        private static final int LIST_LANDMARKS = 1;
        private static final int LIST_IMAGES = 2;
        private static final int LIST_POPULAR = 3;
        private static final int GET_THEMES = 4;
        private static final int GET_REGION_IMG = 5;
        private static final int GET_LANDMARK_IMG = 6;
        private static final int GET_IMAGE = 7;
        private static final int GET_LANDMARK_GPS = 8;
        private static final int GET_REGION_GPS = 9;
        private static final int GET_TEXT = 10;
    }

    private void write(final int requestCode, String... args) {
        StringBuilder builder = new StringBuilder();

        builder.append(requestCode);
        builder.append(" ");

        for (String arg : args) {
            builder.append(arg);
            builder.append(' ');
        }

        builder.append(";");

        Log.e(TAG, "Sending: " + builder.toString());
        _output.println(builder.toString());
    }

    private Data<String> read() throws IOException {
        Data<String> data = new Data<>();

        int size = Integer.parseInt(_input.readLine());

        Log.e(TAG, "Raw size: " + Integer.toString(size));

        StringBuilder reader = new StringBuilder();
        while (size >= 0) {
            reader.append((char)_input.read());
            --size;
        }

        String raw = reader.toString();

        Log.e(TAG, "Raw Result: " + raw);

        if (raw.length() == 0) {
            data.failed = true;
            data.error_message = "invalid message length";
        }
        else {
            String[] split = raw.split(SEPARATOR);
            split[split.length - 1] = split[split.length - 1].substring(0, split[split.length - 1].length() - 1);

            if (split.length == 0 ||
                    Integer.parseInt(split[0]) != 0)
                data.failed = true;
            else {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i != split.length; ++i) {
                    builder.append(split[i]);
                    if (i != split.length - 1)
                        builder.append(SEPARATOR_CHAR);
                }
                data.result = builder.toString();
            }
        }

        return data;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    Data<ArrayList<String>> listLandmarks()  {
        return new Worker<>(new Work<ArrayList<String>, String>() {
            @Override
            public Data<ArrayList<String>> run(String... strings) {
                Data<ArrayList<String>> data = new Data<>();

                write(RequestCode.LIST_LANDMARKS, strings);

                try {

                    Data<String> raw = read();
                    if (raw.failed)
                        data.failed = true;
                    else
                    {
                        data.result = new ArrayList<>();
                        String[] split = raw.result.split(SEPARATOR);
                        for (String landmark : split)
                            data.result.add(landmark);
                    }

                } catch (Exception e) {
                    data.failed = true;
                    data.error_message = e.getMessage();
                }

                return data;
            }
        }).getResult(DEFAULT_REGION);
    }

    Data<ArrayList<String>> listImages(String landmark) {
        return new Worker<>(new Work<ArrayList<String>, String>(){
            @Override
            public Data<ArrayList<String>> run(String... strings) {
                Data<ArrayList<String>> data = new Data<>();

                write(RequestCode.LIST_IMAGES, strings);

                try {

                    Data<String> raw = read();
                    if (raw.failed)
                        data.failed = true;
                    else
                    {
                        data.result = new ArrayList<>();
                        String[] split = raw.result.split(SEPARATOR);
                        for (String image : split)
                            data.result.add(image);
                    }

                } catch (Exception e) {
                    data.failed = true;
                    data.error_message = e.getMessage();
                }

                return data;
            }
        }).getResult(DEFAULT_REGION, landmark);
    }

    Data<String> getImageText(String landmark, String image) {
        return new Worker<>(new Work<String, String>() {
            @Override
            public Data<String> run(String... strings) {
                Data<String> data = new Data<>();

                write(RequestCode.GET_TEXT, strings);

                try {
                    data = read();
                } catch (Exception e) {
                    data.failed = true;
                    data.error_message = e.getMessage();
                }

                return data;
            }
        }).getResult(DEFAULT_REGION, landmark, image);
    }

    Data<Bitmap> getLandmarkImage(String landmark) {
        return new Worker<>(new Work<Bitmap, String>() {

            @Override
            public Data<Bitmap> run(String... strings) {
                Data<Bitmap> data = new Data<>();

                write(RequestCode.GET_LANDMARK_IMG, strings);

                try {

                    Data<String> raw = read();
                    if (raw.failed) {
                        data.failed = true;
                        data.error_message = raw.error_message;
                    }
                    else {
                        data.result = httpGetImage(raw.result);
                    }

                } catch (Exception e) {
                    data.failed = true;
                    data.error_message = e.getMessage();
                }

                return data;
            }
        }).getResult(DEFAULT_REGION, landmark);
    }

    Data<Bitmap> getImage(String landmark, String image_name) {
        return new Worker<>(new Work<Bitmap, String>() {
            @Override
            public Data<Bitmap> run(String... strings) {
                Data<Bitmap> data = new Data<>();

                write(RequestCode.GET_IMAGE, strings);

                try {

                    Data<String> raw = read();
                    if (raw.failed) {
                        data.failed = true;
                        data.error_message = raw.error_message;
                    }
                    else {
                        data.result = httpGetImage(raw.result);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "getImage Failed: " + e.getMessage());
                    data.failed = true;
                    data.error_message = e.getMessage();
                }

                return data;
            }
        }).getResult(DEFAULT_REGION, landmark, image_name);
    }

    Data<GPSCoord> getLandmarkGPS(String landmark) {
        return new Worker<>(new Work<GPSCoord, String>() {
            @Override
            public Data<GPSCoord> run(String... strings) {
                Data<GPSCoord> data = new Data<>();

                write(RequestCode.GET_LANDMARK_GPS, strings);

                try {
                    Data<String> raw = read();
                    if (raw.failed) {
                        data.failed = true;
                        data.error_message = raw.error_message;
                    } else {
                        String[] split = raw.result.split(" ");
                        data.result = new GPSCoord(
                                Double.parseDouble(split[0]),
                                Double.parseDouble(split[1])
                        );
                    }
                } catch (Exception e) {
                    data.failed = true;
                    data.error_message = e.getMessage();
                }

                return data;
            }
        }).getResult(DEFAULT_REGION, landmark);
    }

    Data<ArrayList<String>> getPopular() {
        return new Worker<>(new Work<ArrayList<String>, String>() {
            @Override
            public Data<ArrayList<String>> run(String... strings) {
                Data<ArrayList<String>> data = new Data<>();

                write(RequestCode.LIST_POPULAR, strings);

                try {
                    Data<String> raw = read();
                    if (raw.failed) {
                        data.failed = true;
                        data.error_message = raw.error_message;
                    } else {
                        data.result = new ArrayList<>();
                        for (String landmark : raw.result.split(SEPARATOR))
                            data.result.add(landmark);
                    }
                } catch (Exception e) {
                    data.failed = true;
                    data.error_message = e.getMessage();
                }

                return data;
            }
        }).getResult(DEFAULT_REGION);
    }

    Data<ArrayList<String>> getTheme(String theme) {
        return new Worker<>(new Work<ArrayList<String>, String>() {
            @Override
            public Data<ArrayList<String>> run(String... strings) {
                Data<ArrayList<String>> data = new Data<>();

                write(RequestCode.GET_THEMES, strings);

                try {
                    Data<String> raw = read();
                    if (raw.failed) {
                        data.failed = true;
                        data.error_message = raw.error_message;
                    } else {
                        data.result = new ArrayList<>();
                        for (String landmark : raw.result.split(SEPARATOR))
                            data.result.add(landmark);
                    }
                } catch (Exception e) {
                    data.failed = true;
                    data.error_message = e.getMessage();
                }

                return data;
            }
        }).getResult(DEFAULT_REGION, theme);
    }
}


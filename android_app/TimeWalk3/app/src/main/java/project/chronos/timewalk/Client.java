package project.chronos.timewalk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.SerializablePermission;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Fletcher on 9/09/2017.
 */

public class Client {

    //  ** CONSTANTS **

    private static final String LOG = "TIME_WALK_CLIENT";
    private static final String SERVER_ADDR = "deco3801-Chronos.uqcloud.net";
    private static final int SERVER_PORT = 5001;
    private static final String SEPARATOR = "\\|";
    private static final int TIME_OUT = 3; // seconds

    //  ** VARIABLES **

    Socket _socket;
    PrintWriter _output;
    BufferedReader _input;

    //  ** METHODS **

    Data<Boolean> connect() {

        Data<Boolean> data;

        try {

            data = new AsyncTask<Void, Void, Data<Boolean>>() {

                @Override
                protected Data<Boolean>  doInBackground(Void... voids) {

                    Data<Boolean> data = new Data<>();

                    try {

                        Log.d(LOG, "Starting to Connect to Server");

                        InetAddress addr = InetAddress.getByName(SERVER_ADDR);
                        _socket = new Socket(addr, SERVER_PORT);
                        _output = new PrintWriter(_socket.getOutputStream(), true);
                        _input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
                        return data;

                    } catch (Exception e) {
                        data.error_msg = e.getMessage();
                        data.failed = true;
                        data.code = TransferCode.NETWORK_ERROR;

                        Log.d(LOG, "Failed to Connect: " + e.getMessage());
                    }
                    return data;
                }
            }.execute().get(TIME_OUT , TimeUnit.SECONDS);

        } catch (Exception e) {
            data = new Data<>();
            data.result = false;
            data.error_msg = e.getMessage();
            data.failed = true;
            data.code = TransferCode.ASYNC_THREAD_ERROR;

            Log.d(LOG, "Failed to Async: " + e.getMessage());
        }

        return data;
    }

    Data<ArrayList<String>> listLandmarks() {
        Data<ArrayList<String>> data;

        try {

            data = new AsyncTask<String, Void, Data<ArrayList<String>>>() {

                @Override
                protected Data<ArrayList<String>> doInBackground(String... strings) {

                    Data<ArrayList<String>> data = new Data<>();

                    try {

                        Log.d(LOG, "Getting Landmarks From Server");

                        // send request
                        output(TransferCode.LIST_LANDMARKS, strings[0]);

                        // get transfer code
                        data.code = getTransferCode();

                        Log.d(LOG, "Transfer Code: " + Integer.toString(data.code));

                        if (data.code != TransferCode.SUCESS) {
                            data.result = null;
                            data.failed = true;
                            data.error_msg = getNextLine();

                            Log.d(LOG, "Landmarks Failed: " + data.error_msg);

                        } else {
                            data.result = new ArrayList<>();
                            for (String landmark : getNextLine().split(SEPARATOR))
                                data.result.add(landmark);
                        }

                    } catch (Exception e) {
                        data.error_msg = e.getMessage();
                        data.result = null;
                        data.failed = true;
                        data.code = TransferCode.NETWORK_ERROR;

                        Log.d(LOG, "Landmarks Networking Failed: " + data.error_msg);
                    }

                    return data;
                }
            }.execute("Brisbane").get();

        } catch (Exception e) {
            data = new Data<>();
            data.result = null;
            data.error_msg = e.getMessage();
            data.failed = true;
            data.code = TransferCode.ASYNC_THREAD_ERROR;

            Log.d(LOG, "Landmarks Threading Failed: " + data.error_msg);
        }

        return data;
    }

    Data<ArrayList<String>> listImages(String landmark) {
        Data<ArrayList<String>> data;

        try {

            data = new AsyncTask<String, Void, Data<ArrayList<String>>>() {

                @Override
                protected Data<ArrayList<String>> doInBackground(String... strings) {
                    Data<ArrayList<String>> data = new Data<>();

                    try {

                        // send request
                        output(TransferCode.LIST_IMAGES, strings[0], strings[1]);

                        // get transfer code
                        data.code = getTransferCode();

                        // if success
                        if (data.code == TransferCode.SUCESS) {
                            data.result = new ArrayList<>();
                            for (String image_name : getNextLine().split(SEPARATOR))
                                data.result.add(image_name);
                        } else {
                            data.failed = true;
                            data.result = null;
                            data.error_msg = getNextLine();
                        }

                    } catch (Exception e) {
                        data.failed = true;
                        data.result = null;
                        data.error_msg = e.getMessage();
                        data.code = TransferCode.NETWORK_ERROR;
                    }

                    return data;
                }
            }.execute("Brisbane", landmark).get();

        } catch (Exception e) {
            data = new Data<>();
            data.result = null;
            data.failed = true;
            data.error_msg = e.getMessage();
            data.code = TransferCode.ASYNC_THREAD_ERROR;
        }

        return data;
    }

    Data<GPSCoord> getLandmarkGPS(String landmark) {
        return null;
    }

    Data<String> getImageText(String landmark, String image_name) {
        Data<String> data;

        try {

            data = new AsyncTask<String, Void, Data<String>>() {

                @Override
                protected Data<String> doInBackground(String... strings) {
                    Data<String> data = new Data<>();

                    try {

                        Log.d(LOG, "Getting Text");

                        // send request
                        output(TransferCode.GET_TEXT, strings[0], strings[1], strings[2]);

                        // get transfer code
                        data.code = getTransferCode();
                        Log.d(LOG, "Transfer Code: " + Integer.toString(data.code));

                        // success
                        if (data.code == TransferCode.SUCESS) {
                            // recieve text size
                            int size = getTransferCode();

                            StringBuilder builder = new StringBuilder();
                            while (size >= 0) {
                                builder.append((char)_input.read());
                                --size;
                            }
                            data.result = builder.toString();
                        }

                    } catch (Exception e) {
                        data.error_msg = e.getMessage();
                        data.result = null;
                        data.failed = true;
                        data.code = TransferCode.NETWORK_ERROR;

                        Log.d(LOG, "Text Networking Failed: " + e.getMessage());
                    }

                    return data;
                }
            }.execute("Brisbane", landmark, image_name).get();

        } catch (Exception e) {
            data = new Data<>();
            data.result = null;
            data.failed = true;
            data.error_msg = e.getMessage();
            data.code = TransferCode.ASYNC_THREAD_ERROR;

            Log.d(LOG, "Text Threading Failed: " + e.getMessage());
        }

        return data;
    }

    Data<Bitmap> getImage(String landmark, String image_name) {
        Data<Bitmap> data;

        try {

            data = new AsyncTask<String, Void, Data<Bitmap>>() {

                @Override
                protected Data<Bitmap> doInBackground(String... strings) {
                    Data<Bitmap> data = new Data<>();

                    try {

                        Log.d(LOG, "Getting Image Image");

                        // send request
                        // TODO: move "medium" to a settings option
                        output(TransferCode.GET_IMAGE, strings[0], strings[1], strings[2], "medium");

                        // get transfer code
                        data.code = getTransferCode();

                        Log.d(LOG, "Transfer Code: " + Integer.toString(data.code));

                        if (data.code != TransferCode.SUCESS) {
                            data.result = null;
                            data.failed = true;
                            data.error_msg = getNextLine();

                            Log.d(LOG, "Image Failed: " + data.error_msg);
                        } else {

                            // get URL
                            String url_str = getNextLine();

                            // decode data from a HTTP URL to a bitmap
                            URL url = new URL(url_str);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setDoInput(true);
                            conn.connect();
                            InputStream input = conn.getInputStream();
                            data.result = BitmapFactory.decodeStream(input);
                        }

                    } catch (Exception e) {
                        data.result = null;
                        data.failed = true;
                        data.error_msg = e.getMessage();
                        data.code = TransferCode.NETWORK_ERROR;

                        Log.d(LOG, "Image Networking Failed: " + data.error_msg);
                    }

                    return data;
                }
            }.execute("Brisbane", landmark, image_name).get();

        } catch (Exception e) {
            data = new Data<>();
            data.error_msg = e.getMessage();
            data.result = null;
            data.failed = true;
            data.code = TransferCode.ASYNC_THREAD_ERROR;

            Log.d(LOG, "Image Threading Failed: " + data.error_msg);
        }
        return data;
    }

    Data<Bitmap> getLandmarkPostcardImage(String landmark) {
        Data<Bitmap> data;

        try {

            data = new AsyncTask<String, Void, Data<Bitmap>>() {

                @Override
                protected Data<Bitmap> doInBackground(String... strings) {
                    Data<Bitmap> data = new Data<>();

                    try {

                        Log.d(LOG, "Getting Landmark Postcard Image");

                        // send request
                        output(TransferCode.GET_POSTCARD_IMAGE_LANDMARK,
                                strings[0], strings[1]);

                        // get transfer code
                        data.code = getTransferCode();

                        Log.d(LOG, "Transfer Code: " + Integer.toString(data.code));

                        if (data.code != TransferCode.SUCESS) {
                            data.result = null;
                            data.failed = true;
                            data.error_msg = getNextLine();

                            Log.d(LOG, "LandmarkPostcard Failed: " + data.error_msg);
                        } else {

                            // get URL
                            String url_str = getNextLine();

                            // decode data from a HTTP URL to a bitmap
                            URL url = new URL(url_str);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setDoInput(true);
                            conn.connect();
                            InputStream input = conn.getInputStream();
                            data.result = BitmapFactory.decodeStream(input);
                        }

                    } catch (Exception e) {
                        data.result = null;
                        data.failed = true;
                        data.error_msg = e.getMessage();
                        data.code = TransferCode.NETWORK_ERROR;

                        Log.d(LOG, "LandmarkPostcard Networking Failed: " + data.error_msg);
                    }

                    return data;
                }
            }.execute("Brisbane", landmark).get();

        } catch (Exception e) {
            data = new Data<>();
            data.error_msg = e.getMessage();
            data.result = null;
            data.failed = true;
            data.code = TransferCode.ASYNC_THREAD_ERROR;

            Log.d(LOG, "LandmarkPostcard Threading Failed: " + data.error_msg);
        }
        return data;
    }

    private void output(int transfer_code, String... strings) {

        StringBuilder builder = new StringBuilder();

        builder.append(transfer_code);
        builder.append(" ");

        for (String s : strings)
            builder.append(s + " ");
        builder.append(";");

        _output.println(builder.toString());
    }

    private int getTransferCode() throws IOException {
        return Integer.parseInt(getNextLine());
    }

    private String getNextLine() throws IOException {
        return _input.readLine();
    }

    public boolean isConnected() {
        return _socket.isConnected();
    }
}

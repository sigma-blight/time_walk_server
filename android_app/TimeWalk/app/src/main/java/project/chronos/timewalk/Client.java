package project.chronos.timewalk;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Fletcher on 5/09/2017.
 */

public class Client {

    public static final String CLIENT_LOG = "TIME_WALK_CLIENT_LOG";
    private static final String SERVER_ADDR = "deco3801-Chronos.uqcloud.net";
    private static final int SERVER_PORT = 5001;
    private static final String SEPERATOR = " ";

    public enum Transfer {
        TEXT(0),
        IMAGE(1),
        EMPTY_DIRECTORY(2),
        INVALID_REQUEST(10),
        INVALID_DIRECTORY(11),
        INVALID_FILE(12),
        EXCEPTION_FAIL(20)
        ;

        public final int code;
        Transfer(int i) {
            this.code = i;
        }
    }

    private enum Request {
        LIST_REGIONS(0),
        LIST_LANDMARKS(1),
        LIST_IMAGES(2),
        GET_GPS(10),
        GET_TEXT(11)
        ;

        public final int code;
        Request(int i) {
            this.code = i;
        }
    }

    private String to_string(Request request) {
        return Integer.toString(request.code);
    }

    private Transfer to_code(String code_str) {
        int code = Integer.parseInt(code_str);
        return Transfer.values()[code];
    }

    public class Data<T> {
        public T result;
        public Transfer code;
        public boolean failed = false;
        public String error_msg;
    }

    Socket _socket;
    PrintWriter _output;
    BufferedReader _input;

    //  **  CONNECTION **

    private class Connection extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try
            {
                InetAddress addr = InetAddress.getByName(SERVER_ADDR);
                _socket = new Socket(addr, SERVER_PORT);
                _output = new PrintWriter(_socket.getOutputStream(), true);
                _input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            } catch (Exception e) {
                Log.e(CLIENT_LOG, "Connection Failed: " + e.getMessage());
                return false;
            }
            return true;
        }
    }

    public boolean connect()
    {
        try {
            Connection conn = new Connection();
            conn.execute().get();
            return true;
        } catch (Exception e) {
            Log.e(CLIENT_LOG, "connection interrupted: " + e.getMessage());
        }
        return false;
    }

    //  **  LANDMARKS  **

    private class Landmarks extends AsyncTask<String, Void, Data<ArrayList<String>>> {

        @Override
        protected Data<ArrayList<String>> doInBackground(String... strings) {
            Data<ArrayList<String>> data = new Data<>();

            _output.println(to_string(Request.LIST_LANDMARKS) + " " + strings[0] + ";");

            try {
                Transfer code = to_code(_input.readLine());
                data.code = code;

                if (code != Transfer.TEXT) {
                    data.error_msg = _input.readLine();
                    data.failed = true;
                } else {
                    data.result = new ArrayList<>();
                    for (String land : _input.readLine().split(SEPERATOR))
                        data.result.add(land);
                }

            } catch (Exception e) {
                Log.e(CLIENT_LOG, "Landmarks Failed: " + e.getMessage());
                data.result = null;
                data.failed = true;
                data.error_msg = e.getMessage();
                data.code = Transfer.EXCEPTION_FAIL;
            }

            return data;
        }
    }

    public Data<ArrayList<String>> landmarks()
    {
        try {
            Landmarks land = new Landmarks();
            return land.execute("Brisbane").get();
        } catch (Exception e) {
            Log.e(CLIENT_LOG, "landmarks interrupted: " + e.getMessage());
        }
        return null;
    }

    //  ** IMAGE NAMES **

    private class ImageNames extends AsyncTask<String, Void, Data<ArrayList<String>>> {

        @Override
        protected Data<ArrayList<String>> doInBackground(String... strings) {
            Data<ArrayList<String>> data = new Data<>();

            _output.println(to_string(Request.LIST_IMAGES) + " " +
                    strings[0] + " " + strings[1] + " " + strings[2] + ";");

            try {
                Transfer code = to_code(_input.readLine());
                data.code = code;

                if (code != Transfer.TEXT) {
                    data.error_msg = _input.readLine();
                    data.failed = true;
                } else {
                    data.result = new ArrayList<>();
                    for (String image_name : _input.readLine().split(SEPERATOR))
                        data.result.add(image_name);
                }

            } catch (Exception e) {
                Log.e(CLIENT_LOG, "Images Failed: " + e.getLocalizedMessage());
                data.result = null;
                data.failed = true;
                data.error_msg = e.getMessage();
                data.code = Transfer.EXCEPTION_FAIL;
            }

            return data;
        }
    }

    public Data<ArrayList<String>> imageNames(String landmark) {
        try {
            ImageNames images = new ImageNames();
            return images.execute("Brisbane", landmark).get();
        } catch (Exception e) {
            Log.e(CLIENT_LOG, "Image Names Interrupted: " + e.getMessage());
        }
        return null;
    }

    //  ** GPS **

    public static class GPSCoord {
        public double longitude;
        public double latitude;
    }

    private class GPS extends AsyncTask<String, Void, Data<GPSCoord>> {

        @Override
        protected Data<GPSCoord> doInBackground(String... strings) {
            Data<GPSCoord> data = new Data<>();

            try {

                Transfer code = to_code(_input.readLine());
                data.code = code;

                if (code != Transfer.TEXT) {
                    data.failed = true;
                    data.error_msg = _input.readLine();
                } else {
                    String[] gps_str_arr = _input.readLine().split(" ");
                    data.result = new GPSCoord();
                    data.result.longitude = Double.parseDouble(gps_str_arr[0]);
                    data.result.latitude = Double.parseDouble(gps_str_arr[0]);
                }

            } catch (Exception e) {
                Log.e(CLIENT_LOG, "GPS Failed: " + e.getMessage());
                data.result = null;
                data.failed = true;
                data.error_msg = e.getMessage();
                data.code = Transfer.EXCEPTION_FAIL;
            }

            return data;
        }
    }

    public Data<GPSCoord> gpsCoord(String landmark) {
        try {
            GPS gps = new GPS();
            return gps.execute("Brisbane", landmark).get();
        } catch (Exception e) {
            Log.e(CLIENT_LOG, "GPS Coord Interrupted: " + e.getMessage());
        }
        return null;
    }
}

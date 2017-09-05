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
        INVALID_FILE(12)
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
            if (strings.length == 0) return null;

            _output.println(to_string(Request.LIST_LANDMARKS) + " " + strings[0] + ";");

            try {
                Transfer code = to_code(_input.readLine());
                Data<ArrayList<String>> data = new Data<>();
                data.code = code;

                if (code != Transfer.TEXT) {
                    data.error_msg = _input.readLine();
                    data.failed = true;
                } else {
                    data.result = new ArrayList<>();
                    for (String land : _input.readLine().split(SEPERATOR))
                        data.result.add(land);
                }

                return data;

            } catch (Exception e) {
                Log.e(CLIENT_LOG, "Landmarks Failed: " + e.getMessage());
            }

            return null;
        }
    }

    Data<ArrayList<String>> landmarks()
    {
        try {
            Landmarks land = new Landmarks();
            return land.execute("Brisbane").get();
        } catch (Exception e) {
            Log.e(CLIENT_LOG, "landmarks interrupted: " + e.getMessage());
        }
        return null;
    }
}

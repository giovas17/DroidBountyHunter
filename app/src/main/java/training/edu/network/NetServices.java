package training.edu.network;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import training.edu.interfaces.OnTaskListener;

/**
 * @author Giovani González
 * Created by darkgeat on 8/20/17.
 */

public class NetServices extends AsyncTask<String,Void,Boolean>{

    private static final String LOG_TAG = NetServices.class.getSimpleName();

    private OnTaskListener listener;
    private static final String endpoint_fugitivos = "http://201.168.207.210/services/droidBHServices.svc/fugitivos";
    private static final String endpoint_atrapados = "http://201.168.207.210/services/droidBHServices.svc/atrapados";
    private String JSONString;
    private boolean isFugitivos = true;
    private int code = 0;
    private String message;
    private String error;

    public NetServices(OnTaskListener listener) {
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        JSONString = null;

        try {

            isFugitivos = params[0].matches("Fugitivos");

            urlConnection = getStructuredRequest(isFugitivos ? TYPE.FUGITIVOS : TYPE.ATRAPADOS,
                    isFugitivos ? endpoint_fugitivos : endpoint_atrapados,isFugitivos ? "" : params[1]);

            assert urlConnection != null;
            InputStream is = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (is == null) {
                //No hay nada que hacer
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0)
                return true;
            JSONString = buffer.toString();
            Log.d(LOG_TAG, "Respuesta del Servidor: " + JSONString);
            return true;

        }catch (FileNotFoundException e){
            manageError(urlConnection);
            return false;
        }catch (IOException e) {
            manageError(urlConnection);
            return false;
        } catch (Exception e) {
            manageError(urlConnection);
            return false;
        } finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try {
                    reader.close();
                }catch (IOException e){
                    Log.e(LOG_TAG,"Error Closing Stream",e);
                }
            }
        }
    }

    private HttpURLConnection getStructuredRequest(TYPE type, String endpoint, String id) throws IOException, JSONException {
        int TIME_OUT = 5000;
        HttpURLConnection urlConnection = null;
        URL url = null;
        if (type == TYPE.FUGITIVOS) { //----------------------------- GET Fugitivos------------------------------------
            url = new URL(endpoint);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(TIME_OUT);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type","application/json");
            urlConnection.connect();
        }else { //------------------------ POST Atrapados----------------------------------
            url = new URL(endpoint);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setReadTimeout(TIME_OUT);
            urlConnection.setRequestProperty("Content-Type","application/json");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            JSONObject object = new JSONObject();
            object.put("UDIDString",id);
            DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            dataOutputStream.write(object.toString().getBytes());
            dataOutputStream.flush();
            dataOutputStream.close();
        }
        Log.d(LOG_TAG,url.toString());
        return urlConnection;
    }

    private void manageError(HttpURLConnection urlConnection) {
        if (urlConnection != null) {
            try {
                code = urlConnection.getResponseCode();
                if (urlConnection.getErrorStream() != null) {
                    InputStream is = urlConnection.getErrorStream();
                    StringBuilder buffer = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    message = buffer.toString();
                } else {
                    message = urlConnection.getResponseMessage();
                }
                error = urlConnection.getErrorStream().toString();
                Log.e(LOG_TAG, "Error: " + message + ", code: " + code);
            } catch (IOException e1) {
                e1.printStackTrace();
                Log.e(LOG_TAG, "Error");
            }
        }else {
            code = 105;
            message = "Error: No internet connection";
            Log.e(LOG_TAG, "code: " + code + ", " + message);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result){
            listener.OnTaskCompleted(JSONString);
        }else {
            listener.OnTaskError(code,message,error);
        }
    }

    enum TYPE{
        FUGITIVOS,ATRAPADOS
    }
}

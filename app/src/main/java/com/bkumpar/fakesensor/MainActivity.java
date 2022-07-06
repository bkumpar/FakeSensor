package com.bkumpar.fakesensor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.provider.Settings.Secure;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.HashMap;
import com.bkumpar.odooconnectorlib.NamedParameters;
import com.bkumpar.odooconnectorlib.OdooConnector;
import com.bkumpar.odooconnectorlib.PositionalParameters;

public class MainActivity extends AppCompatActivity {

    private ImageButton sensorButton;
    private TextView idTextView;
    private TextView messageTextView;

    private OdooConnector odooConnector = null;
    private String username = "admin";
    private String password = "admin";

    private String android_id;
//    private String url = "http://192.168.111.35:8069";
    private String url = "http://192.168.0.144:8069";
    private String database = "meca_demo_tvin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorButton = (ImageButton) findViewById(R.id.sensorButton);
        idTextView = (TextView) findViewById(R.id.idTextView);
        messageTextView = (TextView) findViewById(R.id.messageTextView);
        android_id = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        idTextView.setText(android_id);
        odooConnector = new OdooConnector(url,database);

        try {
            Boolean pass = odooConnector.authenticate(username, password);
        } catch (Exception e) {
            e.printStackTrace();
            messageTextView.setText(e.getMessage());
        }

    }

    public void sensorOnClick(View view) {
        if(view.getId()==R.id.sensorButton) {
            WriteQuantityTask task = new WriteQuantityTask();
            String producedQuantity = "1";
            task.execute(producedQuantity);
        }
    }

    class WriteQuantityTask extends AsyncTask<String, Void, Integer> {
        private final int RESULT_OK = 0;
        private final int CONNECTION_ERROR = -1;
        private final int AUTHENTICATION_ERROR = -2;
        //        private final int READ_DOCUMENT_ERROR = -3;
        private final int WRITE_DOCUMENT_ERROR = -4;

        private boolean isConnectionOK() {
            try {
                ArrayList<Object> ret = odooConnector.getServerInfo();
                HashMap<String, Object> info = (HashMap<String, Object>) ret.get(0);
                int protocolVersion = (int) info.get("protocol_version");
                String serverVersion = (String) info.get("server_version");
                return (protocolVersion == 1 && serverVersion.equals("14.0"));
            } catch (Exception e) {
                e.printStackTrace();
                messageTextView.setText(String.format("%s, %s", "1",e.getMessage()));
                return false;
            }
        }

        private boolean isAuthenticationOK() {
            try {
                return odooConnector.authenticate(username, password);
            } catch (Exception e) {
                e.printStackTrace();
                messageTextView.setText(String.format("%s, %s", "2",e.getMessage()));
                return false;
            }
        }

        private boolean writeProducedQuantity( String qty_str) {
            try {

                String modelName = "sensor";
                String methodName = "increaseQuantity";
                PositionalParameters pp = new PositionalParameters();
                NamedParameters np = new NamedParameters();
                int qty_int = Integer.parseInt(qty_str);

                pp.addFieldValue(android_id); //  production order name
                pp.addFieldValue(qty_int); //  quantity

                ArrayList<Object> ret = odooConnector.execute(modelName, methodName, pp, np);

            } catch (Exception e) {
                e.printStackTrace();
                messageTextView.setText(String.format("%s, %s", "3",e.getMessage()));
            }
            return true;
        }

        @Override
        protected Integer doInBackground(String... data) {

            if (!isConnectionOK()) {
                return CONNECTION_ERROR;
            }

            if (!isAuthenticationOK()) {
                return AUTHENTICATION_ERROR;
            }

            String producedQuantity = data[0];
            if (!writeProducedQuantity(producedQuantity)) {
                return WRITE_DOCUMENT_ERROR;
            }

            return RESULT_OK;
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch (result) {
                case CONNECTION_ERROR:
                    messageTextView.setText(R.string.connectionError);
                    break;
                case AUTHENTICATION_ERROR:
                    messageTextView.setText(R.string.authenticationError);
                    break;
                case WRITE_DOCUMENT_ERROR:
                    messageTextView.setText(R.string.writeDocumentError);
                    break;
                case RESULT_OK:
                    messageTextView.setText(R.string.updateOK);
                    break;
            }
        }

        @Override
        protected void onPreExecute (){
            messageTextView.setText(R.string.connectingMessage);
        }
    }


}
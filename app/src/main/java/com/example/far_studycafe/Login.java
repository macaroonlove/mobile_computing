package com.example.far_studycafe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Response;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {

    private static String IP_ADDRESS = "34.64.96.102";
    private static String TAG = "phptest";

    EditText userId, userPw;
    Button loginButton;
    TextView signupHrefButton, errortext;

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        View decorView = Login.this.getWindow().getDecorView();
        int uiOptions =  View.SYSTEM_UI_FLAG_IMMERSIVE|
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        userId = (EditText) findViewById(R.id.edit_userid);
        userPw = (EditText) findViewById(R.id.edit_userpw);

        loginButton = (Button) findViewById(R.id.button_login);
        signupHrefButton = (TextView) findViewById(R.id.button_signhref);
        errortext = (TextView)findViewById(R.id.errorlog);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uid = userId.getText().toString();
                String upw = userPw.getText().toString();

                SelectData task = new SelectData();
                task.execute("http://" + IP_ADDRESS + "/Login.php", uid, upw);

                userId.setText("");
                userPw.setText("");
            }
        });

        signupHrefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errortext.setText("");
                Intent intent = new Intent(Login.this, Signup.class);
                startActivity(intent);
            }
        });
    }

    class SelectData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(Login.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "POST response  - " + result);
            if(result.contains("성공")){
                Intent intent = new Intent(Login.this, findsca.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
            else{
                errortext.setText(result);
            }


        }


        @Override
        protected String doInBackground(String... params) {
            String id = (String)params[1];
            String pw = (String)params[2];
            String serverURL = (String)params[0];
            String postParameters = "id=" + id + "&pw=" + pw;

            try{
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    // 정상적인 응답 데이터
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    // 에러 발생
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();
                return sb.toString();
            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
                return new String("Error: " + e.getMessage());
            }
        }
    }
}
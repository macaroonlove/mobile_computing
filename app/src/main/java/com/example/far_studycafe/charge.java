package com.example.far_studycafe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class charge extends AppCompatActivity {

    private static String IP_ADDRESS = "34.64.96.102";
    private static String TAG = "phptest";

    private String uid;
    private String umoney;
    private String charmoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge);

        setTitle("충전하기");

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        umoney = intent.getStringExtra("umoney");

        TextView cm = findViewById(R.id.currentMoney);
        cm.setText(umoney);

        RadioButton rb1 = (RadioButton)findViewById(R.id.rb1);
        RadioButton rb2 = (RadioButton)findViewById(R.id.rb2);
        RadioButton rb3 = (RadioButton)findViewById(R.id.rb3);
        RadioButton rb4 = (RadioButton)findViewById(R.id.rb4);
        RadioButton rb5 = (RadioButton)findViewById(R.id.rb5);

        findViewById(R.id.paybutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rb1.isChecked()){
                    charmoney = "5000";
                }
                else if(rb2.isChecked()){
                    charmoney = "10000";
                }
                else if(rb3.isChecked()){
                    charmoney = "30000";
                }
                else if(rb4.isChecked()){
                    charmoney = "50000";
                }
                else if(rb5.isChecked()){
                    charmoney = "10000";
                }

                charge.UpdateData task = new charge.UpdateData();
                task.execute( "http://" + IP_ADDRESS + "/chargeMoney.php", "");
            }
        });
    }

    private class UpdateData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(charge.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response - " + result);
            if(result.contains("성공")){
                Toast.makeText(charge.this, "충전에 성공하셨습니다..", Toast.LENGTH_SHORT).show();
                finish();
            }
            else{
                Toast.makeText(charge.this, "충전에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected String doInBackground(String... params) {
            String serverURL = (String)params[0];
            String postParameters = "charge=" + charmoney + "&uid=" + uid;

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }
}
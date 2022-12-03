package com.example.far_studycafe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class locker_find extends AppCompatActivity {

    private static String IP_ADDRESS = "34.64.96.102";
    private static String TAG = "phptest";

    private String mJsonString;

    private String uid;
    private String lcn;
    private String lcw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker_find);

        setTitle("사물함 조회");

        locker_find.SelectData task = new locker_find.SelectData();
        task.execute( "http://" + IP_ADDRESS + "/selectLocker.php", "");
    }

    private class SelectData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(locker_find.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            Log.d(TAG, "response - " + result);

            if (result == null){

                //mTextViewResult.setText(errorString);
            }
            else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {
            Intent intent = getIntent();
            String title = intent.getStringExtra("title");
            String serverURL = (String)params[0];
            String postParameters = "title=" + title;

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

    private void showResult(){

        String TAG_JSON = "lockers";
        String TAG_SEAT = "seat";
        String TAG_WHO = "who";
        String TAG_EXPIRE ="expire";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            GridLayout grid = findViewById(R.id.grid_locker);
            grid.removeAllViews();
            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

//                String seat = item.getString(TAG_SEAT);
//                String who = item.getString(TAG_WHO);
                String expire = item.getString(TAG_EXPIRE);

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                GridLayout g = (GridLayout) inflater.inflate(R.layout.add_locker, grid, true);
                FrameLayout f = (FrameLayout) g.getChildAt(i);
                TextView t = (TextView) f.getChildAt(1);
                t.setText("" + (i+1));
                LinearLayout container = findViewById(R.id.lcp);
                ImageButton ib = (ImageButton) f.getChildAt(0);
                if(expire == "null"){
                    ib.setImageResource(R.drawable.locker_un);
                    f.getChildAt(0).setOnClickListener(new OnClickListenerPutIndex(i) {
                        @Override
                        public void onClick(View view) {
                            container.removeAllViews();
                            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            inflater.inflate(R.layout.lcp_item, container, true);
                            TextView ln = (TextView)findViewById(R.id.locker_num);
                            ln.setText((index + 1) + "번 사물함");
                            lcn = "사물함 " + (index+1);

                            Spinner spinner = (Spinner) findViewById(R.id.spinner);
                            TextView lcp = (TextView)findViewById(R.id.lc_price);

                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    String a = adapterView.getSelectedItem().toString();
                                    lcw = a;
                                    if(a.equals("2주일")){
                                        lcp.setText("6000원");
                                    }else if(a.equals("1개월")){
                                        lcp.setText("10000원");
                                    }
                                    else if(a.equals("3개월")){
                                        lcp.setText("29000원");
                                    }
                                    else if(a.equals("6개월")){
                                        lcp.setText("58000원");
                                    }
                                    else if(a.equals("1년")){
                                        lcp.setText("100000원");
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });

                            findViewById(R.id.lc_use).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    locker_find.UpdateData task = new locker_find.UpdateData();
                                    task.execute( "http://" + IP_ADDRESS + "/updateLocker.php", "");
                                }
                            });
                        }
                    });
                }
            }
        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }
    }

    public abstract class OnClickListenerPutIndex implements View.OnClickListener {
        protected int index;
        public OnClickListenerPutIndex(int index) {
            this.index = index;
        }
    }

    private class UpdateData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(locker_find.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response - " + result);
            if(result.contains("성공")){
                Toast.makeText(locker_find.this, "사물함 예약에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
            else if(result.contains("돈없음")){
                Toast.makeText(locker_find.this, "충전 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(locker_find.this, charge.class);
//                intent.putExtra("umoney", result.split("#")[1]);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
            else{
                Toast.makeText(locker_find.this, "사물함 예약에 실패하셨습니다.", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected String doInBackground(String... params) {
            Intent intent = getIntent();
            String title = intent.getStringExtra("title");
            uid = intent.getStringExtra("uid");
            String serverURL = (String)params[0];
            String postParameters = "title=" + title + "&uid=" + uid + "&locker=" + lcn + "&when=" + lcw;

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
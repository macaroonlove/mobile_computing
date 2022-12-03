package com.example.far_studycafe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class reservation extends AppCompatActivity {

    private static String IP_ADDRESS = "34.64.96.102";
    private static String TAG = "phptest";

    private String mJsonString;

    private String cafeName;
    private String uid;

    private TextView title;
    private TextView trans;
    private TextView time;
    private TextView tel;
    private TextView info;

    private ViewPager2 sliderViewPager;
    private LinearLayout layoutIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservation);

        setTitle("예약하기");

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        reservation.GetData task = new reservation.GetData();
        task.execute( "http://" + IP_ADDRESS + "/AddMarker.php", "");

        title = (TextView) findViewById(R.id.rtitle);
        trans = (TextView) findViewById(R.id.rtrans);
        time = (TextView) findViewById(R.id.rtime);
        tel = (TextView) findViewById(R.id.rtel);
        info = (TextView) findViewById(R.id.rinfo);

        sliderViewPager = findViewById(R.id.sliderViewPager);
        layoutIndicator = findViewById(R.id.layoutIndicators);

        findViewById(R.id.sf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(reservation.this, seat_find.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.lf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(reservation.this, locker_find.class);
                intent.putExtra("uid", uid);
                intent.putExtra("title", cafeName);
                startActivity(intent);
            }
        });
    }

    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(reservation.this,
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

            String serverURL = params[0];
            String postParameters = params[1];


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

        String TAG_JSON="markers";
        String TAG_TITLE = "title";
        String TAG_TRANS ="trans";
        String TAG_TIME ="rtime";
        String TAG_TEL ="tel";
        String TAG_INFO ="info";
        String TAG_IMAGE ="rimage";


        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            Intent intent = getIntent();
            String id = intent.getStringExtra("id");
            cafeName = intent.getStringExtra("title");
            JSONObject item = jsonArray.getJSONObject(Integer.parseInt(id.substring(1)));

            String jtitle = item.getString(TAG_TITLE);
            String jtrans = item.getString(TAG_TRANS);
            String jtime = item.getString(TAG_TIME);
            String jtel = item.getString(TAG_TEL);
            String jinfo = item.getString(TAG_INFO);
            String rimage = item.getString(TAG_IMAGE);

            title.setText(jtitle);
            trans.setText(jtrans);
            time.setText(jtime);
            tel.setText(jtel);
            info.setText(jinfo);

            sliderViewPager.setOffscreenPageLimit(1);
            sliderViewPager.setAdapter(new ImageSliderAdapter(this, rimage.split("#")));

            sliderViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    setCurrentIndicator(position);
                }
            });

            setupIndicators(rimage.split("#").length);



        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    private void setupIndicators(int count) {
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(16, 8, 16, 8);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setLayoutParams(params);
            layoutIndicator.addView(indicators[i]);
        }
        setCurrentIndicator(0);
    }

    private void setCurrentIndicator(int position) {
        int childCount = layoutIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutIndicator.getChildAt(i);
        }
    }
}
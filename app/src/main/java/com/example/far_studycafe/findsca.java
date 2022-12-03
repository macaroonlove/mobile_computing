package com.example.far_studycafe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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

public class findsca extends AppCompatActivity implements OnMapReadyCallback {

    private static String IP_ADDRESS = "34.64.96.102";
    private static String TAG = "phptest";

    private String mJsonString;
    private ArrayList<PersonalData> mArrayList;

    private String uid;

    private FragmentManager fragmentManager;
    private MapFragment mapFragment;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findsca);

        setTitle("스터디 카페 찾기");

        mArrayList = new ArrayList<>();

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        GetData task = new GetData();
        task.execute( "http://" + IP_ADDRESS + "/AddMarker.php", "");

        fragmentManager = getFragmentManager();
        mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(findsca.this);
    }

    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(findsca.this,
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
        String TAG_ID = "id";
        String TAG_TITLE = "title";
        String TAG_SNIPPET ="snippet";
        String TAG_LATITUDE ="latitude";
        String TAG_LONGITUDE ="longitude";


        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String id = item.getString(TAG_ID);
                String title = item.getString(TAG_TITLE);
                String snippet = item.getString(TAG_SNIPPET);
                String latitude = item.getString(TAG_LATITUDE);
                String longitude = item.getString(TAG_LONGITUDE);

                PersonalData personalData = new PersonalData();

                personalData.setMember_id(id);
                personalData.setMember_title(title);
                personalData.setMember_snippet(snippet);
                personalData.setMember_latitude(latitude);
                personalData.setMember_longitude(longitude);

                mArrayList.add(personalData);

                //Log.d(TAG, "" + mArrayList.get(0).getMember_title());
            }



        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(mMap!=null){
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoContents(Marker marker) {
                    View row = getLayoutInflater().inflate(R.layout.custom_info, null);
                    Button t0 = (Button) row.findViewById(R.id.sn);
                    TextView t1 = (TextView) row.findViewById(R.id.week);
                    TextView t2 = (TextView) row.findViewById(R.id.time);
                    TextView t3 = (TextView) row.findViewById(R.id.single);
                    TextView t4 = (TextView) row.findViewById(R.id.multi);

                    LatLng ll = marker.getPosition();
                    t0.setText(marker.getTitle());
                    String[] snip = marker.getSnippet().split("#");
                    t1.setText(snip[0]);
                    t2.setText(snip[1]);
                    t3.setText(snip[2]);
                    t4.setText(snip[3]);

                    return row;
                }

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }
            });
        }

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                Intent intent = new Intent(findsca.this, reservation.class);
                //Log.d(TAG, marker.getId());
                intent.putExtra("uid", uid);
                intent.putExtra("id", marker.getId());
                intent.putExtra("title", marker.getTitle());
                startActivity(intent);
            }
        });

        LatLng Myhouse = new LatLng(37.561767,127.021709);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MarkerOptions markerOptions = new MarkerOptions();         // 마커 생성

                for(int i = 0; i < mArrayList.size(); i++){
                    markerOptions.position(new LatLng(Float.parseFloat(mArrayList.get(i).getMember_latitude()), Float.parseFloat(mArrayList.get(i).getMember_longitude())));
                    Log.d(TAG, "" + mArrayList.size());
                    markerOptions.title(mArrayList.get(i).getMember_title());                         // 마커 제목
                    markerOptions.snippet(mArrayList.get(i).getMember_snippet());         // 마커 설명
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                    mMap.addMarker(markerOptions);
                }
            }
        }, 500);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Myhouse, 15));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);                           // 지도 유형 설정

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }else{
            checkLocationPermissionWithRationale();
        }
    }

    public static final int MY_PERMISSION_REQUEST_LOCATION = 99;

    private  void checkLocationPermissionWithRationale(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("위치정보")
                        .setMessage("이 앱을 사용하기 위해서는 위치정보에 접근이 필요합니다. 위치정보 접근을 허용하여 주세요.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(findsca.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_LOCATION);
                            }
                        }).create().show();
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

}
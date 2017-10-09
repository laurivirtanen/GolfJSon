package com.example.lavir.golfjson;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private JSONArray places;
    private GoogleMap mMap;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);





    }



    class FetchDataTask extends AsyncTask<String, Void, JSONObject>{
        @Override
        protected JSONObject doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            JSONObject json = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while((line=bufferedReader.readLine()) != null){
                    stringBuilder.append(line).append("\n");
                }

                bufferedReader.close();
                json = new JSONObject(stringBuilder.toString());
            }catch (IOException e){
                e.printStackTrace();
            }catch (JSONException e){
                e.printStackTrace();
            }finally {
                if(urlConnection != null) urlConnection.disconnect();
            }
            return json;
        }


        protected void onPostExecute(JSONObject json){
            StringBuffer text = new StringBuffer("");
            String kulta = "Kulta";
            String etu = "Etu";
            try{
                places = json.getJSONArray("courses");
                BitmapDescriptor icon;
                for(int i =0; i<places.length();i++){

                    JSONObject plc = places.getJSONObject(i);
                    String adr = "http://ptm.fi/materials/golfcourses/" + plc.getString("image");
                    if(plc.getString("type").equals(kulta)){
                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_golfred);
                    }else if(plc.getString("type").equals(etu)){
                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_money);

                    }else
                        icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_golfyellow);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(plc.getDouble("lat") , plc.getDouble("lng")))
                            .title(plc.getString("course"))
                            .icon(icon)
                            .snippet(plc.getString("address") +"\n"+ plc.getString("phone") +"\n"+  plc.getString("email") +"\n"+  plc.getString("web"))
                    );
                    marker.setTag(adr);


                }
            }catch (JSONException e) {
                Log.e("JSON", "ERROR getting data.");
            }
        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // store map object to member variable
        mMap = googleMap;
        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        FetchDataTask task = new FetchDataTask();
        task.execute("http://ptm.fi/materials/golfcourses/golf_courses.json");
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {


                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.info_window, null);
                ImageView imgV = (ImageView) v.findViewById(R.id.infoImg);


                Picasso.with(context)
                        .load(marker.getTag().toString())
                        .into(imgV);

                TextView title = (TextView) v.findViewById(R.id.title);
                title.setText(marker.getTitle());
                // Getting reference to the TextView to set title
                TextView snippet = (TextView) v.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());


                return v;
            }
        });

        LatLng ICT = new LatLng(62.24324,25.7597);
        // point to jamk/ict and zoom a little
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ICT, 5));
        // marker listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                return false;
            }
        });



    }

}

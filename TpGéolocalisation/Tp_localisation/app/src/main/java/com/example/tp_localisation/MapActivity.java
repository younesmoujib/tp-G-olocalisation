package com.example.tp_localisation;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String showUrl = "http://192.168.1.4/Localisation/ws/ShowPositions.php";
    private Timer refreshTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialisez les cartes
        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Créez un Timer pour rafraîchir la carte périodiquement
        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Appel à l'API GET pour obtenir les nouvelles positions
                fetchAndRefreshMap();
            }
        }, 0, 1000); // Rafraîchissement toute 1 seconde
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void fetchAndRefreshMap() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, showUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mMap.clear(); // Clear old markers
                            Log.d("re", response.toString());

                            // Assuming that the JSON object contains a key called "locations"
                            JSONArray locations = response.getJSONArray("positions");

                            for (int i = 0; i < locations.length(); i++) {
                                JSONObject position = locations.getJSONObject(i);
                                double latitude = position.getDouble("latitude");
                                double longitude = position.getDouble("longitude");
                                LatLng location = new LatLng(latitude, longitude);
                                mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title("Marker " + i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("errr", error.toString());
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Erreur lors de la récupération des données", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}
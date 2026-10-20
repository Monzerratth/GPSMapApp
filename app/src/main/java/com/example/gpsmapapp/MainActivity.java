package com.example.gpsmapapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String MAP_BUNDLE_KEY = "MapViewBundleKey";

    private MapView mapView;
    private GoogleMap googleMap;
    private ImageView imgCaptura;
    private FusedLocationProviderClient fusedClient;

    // Hilo (Thread) en segundo plano: evita congelar la pantalla al buscar direcciones
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // Handler del hilo principal: permite volver a la UI desde el hilo secundario
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Abre la cámara y recibe la foto (miniatura)
    private final ActivityResultLauncher<Void> camaraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    imgCaptura.setImageBitmap(bitmap);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgCaptura = findViewById(R.id.imgCaptura);
        Button btnFoto = findViewById(R.id.btnFoto);
        btnFoto.setOnClickListener(v -> camaraLauncher.launch(null));

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        Bundle mapBundle = savedInstanceState != null
                ? savedInstanceState.getBundle(MAP_BUNDLE_KEY) : null;
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapBundle);
        mapView.getMapAsync(this); // llama a onMapReady cuando el mapa está listo
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Al tocar el mapa se agrega un marcador
        googleMap.setOnMapClickListener(this::agregarMarcador);

        habilitarUbicacion();
    }

    // Permiso en tiempo de ejecución + ubicación actual
    private void habilitarUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        googleMap.setMyLocationEnabled(true); // punto azul de "mi ubicación"
        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng actual = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(actual).title("Estás aquí"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(actual, 15f));
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación. Activa el GPS.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            habilitarUbicacion();
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }

    // Busca la dirección en un Thread aparte y luego dibuja el marcador en la UI
    private void agregarMarcador(LatLng latLng) {
        executor.execute(() -> {
            String direccion = "Marcador";
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> res = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (res != null && !res.isEmpty()) {
                    direccion = res.get(0).getAddressLine(0);
                }
            } catch (IOException ignored) { }

            final String titulo = direccion;
            mainHandler.post(() ->
                    googleMap.addMarker(new MarkerOptions().position(latLng).title(titulo)));
        });
    }

    // ---- Ciclo de vida del MapView (obligatorio al usar MapView) ----
    @Override protected void onStart()   { super.onStart();   mapView.onStart(); }
    @Override protected void onResume()  { super.onResume();  mapView.onResume(); }
    @Override protected void onPause()   { mapView.onPause(); super.onPause(); }
    @Override protected void onStop()    { mapView.onStop();  super.onStop(); }
    @Override protected void onDestroy() {
        mapView.onDestroy();
        executor.shutdown();
        super.onDestroy();
    }
    @Override public void onLowMemory()  { super.onLowMemory(); mapView.onLowMemory(); }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapBundle = outState.getBundle(MAP_BUNDLE_KEY);
        if (mapBundle == null) {
            mapBundle = new Bundle();
            outState.putBundle(MAP_BUNDLE_KEY, mapBundle);
        }
        mapView.onSaveInstanceState(mapBundle);
    }
}

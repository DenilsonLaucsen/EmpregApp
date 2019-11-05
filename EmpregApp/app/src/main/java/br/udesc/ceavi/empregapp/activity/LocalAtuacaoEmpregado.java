package br.udesc.ceavi.empregapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import br.udesc.ceavi.empregapp.R;
import br.udesc.ceavi.empregapp.config.ConfiguracaoFirebase;
import br.udesc.ceavi.empregapp.helper.UsuarioFirebase;
import br.udesc.ceavi.empregapp.model.Usuario;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

public class LocalAtuacaoEmpregado extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker marker;
    private FirebaseAuth autenticacao;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localPassageiro;
    private LatLng localSelecionado;
    private Boolean marcadorPosicionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_atuacao_empregado);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        marcadorPosicionado = false;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapAtuacao);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }
            @Override
            public void onMarkerDrag(Marker marker) {
            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                localSelecionado = marker.getPosition();
            }
        });
        recuperarLocalizacaoUsuario();
    }


    private void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (!marcadorPosicionado) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    localPassageiro = new LatLng(latitude, longitude);

                    mMap.clear();
                    marker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(localPassageiro)
                                    .title("Meu local")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
                                    .draggable(true)
                    );
                    marker.setTag(0);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localPassageiro, 15));
                    marcadorPosicionado = true;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    10,
                    locationListener
            );
        }
    }

    public void selecionarLocal(View view) {
        if (marcadorPosicionado && localSelecionado != null) {
            Usuario usuario = UsuarioFirebase.getDadosUsuarioLogado();
            usuario.setLatitude(localSelecionado.latitude);
            usuario.setLongitude(localSelecionado.longitude);
            usuario.salvar();
            finish();
        }
    }
}

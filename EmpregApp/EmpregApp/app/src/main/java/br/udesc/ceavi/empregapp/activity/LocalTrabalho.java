package br.udesc.ceavi.empregapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.data.DataBufferSafeParcelable;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import br.udesc.ceavi.empregapp.R;
import br.udesc.ceavi.empregapp.config.ConfiguracaoFirebase;
import br.udesc.ceavi.empregapp.helper.UsuarioFirebase;
import br.udesc.ceavi.empregapp.model.Requisicao;
import br.udesc.ceavi.empregapp.model.Usuario;

public class LocalTrabalho extends AppCompatActivity implements OnMapReadyCallback {

    private Button buttonAceitarServico;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localEmpregado;
    private LatLng localPatrao;
    private Usuario empregado;
    private Usuario patrao;
    private String idRequisicao;
    private Requisicao requisicao;
    private DatabaseReference firebaseRef;
    private Marker marcadorEmpregado;
    private Marker marcadorPatrao;
    private String statusRequisicao;
    private boolean requisicaoAtiva;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_trabalho);

        inicializarComponentes();

        if (getIntent().getExtras().containsKey("idRequisicao") &&
                getIntent().getExtras().containsKey("empregado")) {
            Bundle extras = getIntent().getExtras();
            empregado = (Usuario) extras.getSerializable("empregado");
            localEmpregado = new LatLng(empregado.getLatitude(), empregado.getLongitude());
            idRequisicao = extras.getString("idRequisicao");
            requisicaoAtiva = extras.getBoolean("requisicaoAtiva");
            verificaStatusRequisicao();
        }
    }

    private void verificaStatusRequisicao() {

        DatabaseReference requisicoes = firebaseRef.child("requisicoes").child(idRequisicao);
        requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requisicao = dataSnapshot.getValue(Requisicao.class);
                if (requisicao != null) {
                    patrao = requisicao.getPatrao();
                    localPatrao = new LatLng(patrao.getLatitude(), patrao.getLongitude());
                    statusRequisicao = requisicao.getStatus();
                    alteraInterfaceRequisicao(statusRequisicao);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void alteraInterfaceRequisicao(String status) {
        switch (status) {
            case Requisicao.STATUS_AGUARDANDO:
                requisicaoAguardando();
                break;
            case Requisicao.STATUS_A_CAMINHO:
                requisicaoACaminho();
                break;
            case Requisicao.STATUS_EM_SERVICO:
                requisicaoEmServico();
                break;
            case Requisicao.STATUS_CANCELADA:
                requisicaoCancelada();
                break;
        }
    }

    private void requisicaoCancelada(){
        Toast.makeText(this, "Requisicao foi cancelada pelo Patrão", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LocalTrabalho.this, RequisicoesActivity.class));
    }
    

    private void requisicaoEmServico() {
        //TODO
        //TELA DE EM SERVIÇO (IDEIA: JOGAR PRA OUTRA, TELA COM BOTÃO FINALIZAR)]
            if (requisicaoAtiva){
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Você chegou ao local")
                        .setMessage("Não esqueça de avaliar o patrão");
                AlertDialog dialog = builder.create();
                dialog.show();
                requisicaoAtiva = false;
            }

            adicionarMarcadorPatrao(localPatrao, patrao.getNome());

            buttonAceitarServico.setText("Avalie seu patrão");

            mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(localPatrao, 20)
            );

    }

    private void requisicaoAguardando() {

        buttonAceitarServico.setText("Aceitar serviço");

        adicionarMarcadorPatrao(localPatrao, patrao.getNome());
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(localPatrao, 20)
        );


    }

    private void requisicaoACaminho() {
        buttonAceitarServico.setText("A caminho do local");

        adicionarMarcadorEmpregado(localEmpregado, empregado.getNome());

        adicionarMarcadorPatrao(localPatrao, patrao.getNome());

        centralizarDoisMarcadores(marcadorEmpregado, marcadorPatrao);

        iniciarMonitoramento();
    }

    private void iniciarMonitoramento() {
        //Geofire
        DatabaseReference localUsuario = ConfiguracaoFirebase.getFirebaseDatabase().child("local_usuario");
        GeoFire geoFire = new GeoFire(localUsuario);
        //Cicrula na local do trabalho
        final Circle circulo = mMap.addCircle(new CircleOptions().center(localPatrao).radius(50));

        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(localPatrao.latitude, localPatrao.longitude),
                0.05);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (key.equals(empregado.getId())) {

                    requisicao.setStatus(Requisicao.STATUS_EM_SERVICO);
                    requisicao.atualizar();



                    geoQuery.removeAllListeners();
                    circulo.remove();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void centralizarDoisMarcadores(Marker marcadorP, Marker marcadorE) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(marcadorE.getPosition());
        builder.include(marcadorP.getPosition());

        LatLngBounds bounds = builder.build();

        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) (largura * 0.20);

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, largura, altura, espacoInterno));


    }

    private void adicionarMarcadorEmpregado(LatLng localizacao, String titulo) {
        if (marcadorEmpregado != null)
            marcadorEmpregado.remove();
        marcadorEmpregado = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.vassoura01))
        );

    }

    private void adicionarMarcadorPatrao(LatLng localizacao, String titulo) {
        if (marcadorPatrao != null)
            marcadorPatrao.remove();
        marcadorPatrao = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
        );
    }

    private void inicializarComponentes() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Local do trabalho");

        buttonAceitarServico = findViewById(R.id.buttonAceitarServico);

        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        recuperarLocalizacaoUsuario();
    }

    private void recuperarLocalizacaoUsuario() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localEmpregado = new LatLng(latitude, longitude);

                UsuarioFirebase.atualizarDadosLocalizacao(latitude, longitude);

                empregado.setLatitude(latitude);
                empregado.setLongitude(longitude);
                requisicao.setEmpregado(empregado);
                requisicao.atualizarLocalizacao();

                alteraInterfaceRequisicao(statusRequisicao);

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    10,
                    locationListener
            );
        }

    }

    public void aceitarServico(View view) {
        if (statusRequisicao.equals(Requisicao.STATUS_AGUARDANDO)) {
            requisicao = new Requisicao();
            requisicao.setId(idRequisicao);
            requisicao.setEmpregado(empregado);
            requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);

            requisicao.atualizar();
        } else {
            if (statusRequisicao.equals(Requisicao.STATUS_EM_SERVICO)) {
                Intent i = new Intent(LocalTrabalho.this, AvaliacaoUsuarioActivity.class);
                startActivity(i);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (requisicaoAtiva) {
            Toast.makeText(LocalTrabalho.this, "Por favor, encerre a requisção atual", Toast.LENGTH_SHORT).show();

        } else {
            Intent i = new Intent(LocalTrabalho.this, RequisicoesActivity.class);
            startActivity(i);
        }
        if (statusRequisicao != null && !statusRequisicao.isEmpty() && !requisicao.getStatus().equals(Requisicao.STATUS_AGUARDANDO)) {
            requisicao.setStatus(Requisicao.STATUS_FINALIZADO);
            requisicao.atualizarStatus();
        }
        return false;
    }
}

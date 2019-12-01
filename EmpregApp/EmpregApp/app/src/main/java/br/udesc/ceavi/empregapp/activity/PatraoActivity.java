package br.udesc.ceavi.empregapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.udesc.ceavi.empregapp.R;
import br.udesc.ceavi.empregapp.config.ConfiguracaoFirebase;
import br.udesc.ceavi.empregapp.helper.UsuarioFirebase;
import br.udesc.ceavi.empregapp.model.Requisicao;
import br.udesc.ceavi.empregapp.model.Usuario;

public class PatraoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button buttonChamarServico;

    private GoogleMap mMap;
    private FirebaseAuth autenticacao;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localPatrao;
    private boolean servicoChamado = false;
    private DatabaseReference firebaseRef;
    private Requisicao requisicao;
    private Usuario patrao;
    private String statusRequisicao;
    private Marker marcadorPatrao;
    private Marker marcadorEmpregado;
    private Usuario empregado;
    private LatLng localEmpregado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patrao);

        //UsuarioFirebase.getUsuarios();

        inicializarComponentes();

        verificaStatusRequisicao();
    }

    private void verificaStatusRequisicao() {
        Usuario usuario = UsuarioFirebase.getDadoUsuarioLogado();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        Query requisicaoPesquisa = requisicoes.orderByChild("patrao/id").equalTo(usuario.getId());
        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Requisicao> lista = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    lista.add(ds.getValue(Requisicao.class));
                }

                Collections.reverse(lista);
                if (lista != null && lista.size() > 0) {
                    requisicao = lista.get(0);

                    if (requisicao != null) {
                        if (!requisicao.getStatus().equals(Requisicao.STATUS_FINALIZADO)) {
                            patrao = requisicao.getPatrao();
                            localPatrao = new LatLng(patrao.getLatitude(), patrao.getLongitude());
                            statusRequisicao = requisicao.getStatus();
                            if (requisicao.getEmpregado() != null) {
                                empregado = requisicao.getEmpregado();
                                localEmpregado = new LatLng(empregado.getLatitude(), empregado.getLongitude());
                            }
                            alteraInterfaceRequisicao(statusRequisicao);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void alteraInterfaceRequisicao(String status) {
        if (status != null && !status.isEmpty()) {
            servicoChamado = false;
            switch (requisicao.getStatus()) {
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
                case Requisicao.STATUS_FINALIZADO:
                    requisicaoCancelada();
                    break;
            }
        } else {
            adicionarMarcadorPatrao(localPatrao, "Seu local");
            centralizarMarcador(localPatrao);
        }
    }


    private void requisicaoCancelada() {
        buttonChamarServico.setText("Chamar serviço");
        servicoChamado = false;
        if (marcadorEmpregado != null)
            marcadorEmpregado.remove();
        adicionarMarcadorPatrao(localPatrao, "Seu local");
        centralizarMarcador(localPatrao);
    }

    private void requisicaoAguardando() {
        buttonChamarServico.setText("Cancelar");
        servicoChamado = true;
        adicionarMarcadorPatrao(localPatrao, patrao.getNome());
        centralizarMarcador(localPatrao);
    }

    private void requisicaoACaminho() {
        buttonChamarServico.setText("Empregado a caminho");
        buttonChamarServico.setEnabled(false);
        adicionarMarcadorPatrao(localPatrao, patrao.getNome());
        adicionarMarcadorEmpregado(localEmpregado, empregado.getNome());
        centralizarDoisMarcadores(marcadorEmpregado, marcadorPatrao);
    }

    private void requisicaoEmServico() {
        buttonChamarServico.setText("Avalie o empregado");
        buttonChamarServico.setEnabled(true);

        adicionarMarcadorPatrao(localPatrao, patrao.getNome());
        centralizarMarcador(localPatrao);
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

    private void centralizarMarcador(LatLng local) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 20));
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        recuperarLocalizacaoUsuario();
    }

    public void chamarServico(View view) {
        if (requisicao != null && requisicao.getStatus().equals(Requisicao.STATUS_EM_SERVICO)) {
            Intent i = new Intent(PatraoActivity.this, AvaliacaoUsuarioActivity.class);
            startActivity(i);
        } else {
            if (servicoChamado) {
                //cancelar requisicao
                requisicao.setStatus(Requisicao.STATUS_CANCELADA);
                requisicao.atualizarStatus();
            } else {
                servicoChamado = true;
                salvarRequisicao();

            }
        }

    }

    private void salvarRequisicao() {
        Requisicao requisicao = new Requisicao();

        Usuario usuarioPatrao = UsuarioFirebase.getDadoUsuarioLogado();
        usuarioPatrao.setLatitude(localPatrao.latitude);
        usuarioPatrao.setLongitude(localPatrao.longitude);

        requisicao.setPatrao(usuarioPatrao);
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);

        requisicao.salvar();

        buttonChamarServico.setText("Cancelar");
    }


    private void recuperarLocalizacaoUsuario() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localPatrao = new LatLng(latitude, longitude);

                UsuarioFirebase.atualizarDadosLocalizacao(latitude, longitude);

                alteraInterfaceRequisicao(statusRequisicao);

                if (statusRequisicao != null && !statusRequisicao.isEmpty()) {
                    if (statusRequisicao.equals(Requisicao.STATUS_EM_SERVICO)) {
                        locationManager.removeUpdates(locationListener);
                    } else {
                        if (ActivityCompat.checkSelfPermission(PatraoActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    10000,
                                    10,
                                    locationListener
                            );
                        }
                    }
                }
                mMap.clear();
                mMap.addMarker(
                        new MarkerOptions()
                                .position(localPatrao)
                                .title("Meu local")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
                );


   /*            for (Usuario u : UsuarioFirebase.usuarios) {
                    mMap.addMarker(
                            new MarkerOptions()
                                    .position(new LatLng(u.getLatitude(), u.getLongitude()))
                                    .title(u.getNome())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.vassoura01))
                    );
                }

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localPatrao, 15));
*/

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSair:
                autenticacao.signOut();
                finish();
                Intent i = new Intent(this, MainActivity.class);
                this.startActivity(i);
                break;
            case R.id.menuEditarDados:
                UsuarioFirebase.redirecionaEditarDados(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Buscar Faxineiro(a)");
        setSupportActionBar(toolbar);

        buttonChamarServico = findViewById(R.id.buttonChamarServico);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
}

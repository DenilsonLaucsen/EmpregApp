package br.udesc.ceavi.empregapp.helper;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.udesc.ceavi.empregapp.activity.EditarDadosEmpregado;
import br.udesc.ceavi.empregapp.activity.EditarDadosPatrao;
import br.udesc.ceavi.empregapp.activity.PatraoActivity;
import br.udesc.ceavi.empregapp.activity.RequisicoesActivity;
import br.udesc.ceavi.empregapp.config.ConfiguracaoFirebase;
import br.udesc.ceavi.empregapp.model.PerfilUsuario;
import br.udesc.ceavi.empregapp.model.Usuario;

public class UsuarioFirebase {

    public static Usuario usuarioAtual;
    public static List<Usuario> usuarios = new ArrayList();

    public static FirebaseUser getUsuarioAtual() {
        FirebaseAuth usuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return usuario.getCurrentUser();
    }


    public static String getIdentificadorUsuario() {
        return getUsuarioAtual().getUid();
    }

    public static Usuario getDadosUsuarioLogado() {
        FirebaseUser user = getUsuarioAtual();
        if (user != null) {
            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                    .child("usuarios").child(getIdentificadorUsuario());
            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    usuarioAtual = dataSnapshot.getValue(Usuario.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        return usuarioAtual;
    }

    public static Usuario getDadoUsuarioLogado() {
        FirebaseUser firebaseUser = null;
        Usuario usuario = new Usuario();
        while (firebaseUser == null) {
            firebaseUser = getUsuarioAtual();

            usuario.setId(firebaseUser.getUid());
            usuario.setEmail(firebaseUser.getEmail());
            usuario.setNome(firebaseUser.getDisplayName());


        }
        return usuario;
    }

        public static boolean atualizarNomeUsuario (String nome){
            try {
                FirebaseUser user = getUsuarioAtual();
                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(nome).build();
                user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.d("Perfil", "Erro ao atualizar nome de perfil.");
                        }
                    }
                });
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public static void redirecionaUsuarioLogado ( final Activity activity){
            FirebaseUser user = getUsuarioAtual();
            if (user != null) {
                Log.d("resultado", "onDataChange: " + getIdentificadorUsuario());
                DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                        .child("usuarios").child(getIdentificadorUsuario());
                usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("resultado", "onDataChange: " + dataSnapshot.toString());
                        usuarioAtual = dataSnapshot.getValue(Usuario.class);

                        if (usuarioAtual.getTipo().toString().equals(PerfilUsuario.EMPREGADO.toString())) {
                            Intent i = new Intent(activity, RequisicoesActivity.class);
                            activity.startActivity(i);
                        } else {
                            Intent i = new Intent(activity, PatraoActivity.class);
                            activity.startActivity(i);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        }

        public static void redirecionaEditarDados ( final Activity activity){
            FirebaseUser user = getUsuarioAtual();
            if (user != null) {
                Log.d("resultado", "onDataChange: " + getIdentificadorUsuario());
                DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                        .child("usuarios").child(getIdentificadorUsuario());
                usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("resultado", "onDataChange: " + dataSnapshot.toString());
                        usuarioAtual = dataSnapshot.getValue(Usuario.class);

                        if (usuarioAtual.getTipo().equals(PerfilUsuario.EMPREGADO.toString())) {
                            Intent i = new Intent(activity, EditarDadosEmpregado.class);
                            activity.startActivity(i);
                        } else {
                            Intent i = new Intent(activity, EditarDadosPatrao.class);
                            activity.startActivity(i);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        }

        public static void getUsuarios () {
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    usuarios.add(dataSnapshot.getValue(Usuario.class));
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios").orderByChild("nome")
                    .addChildEventListener(childEventListener);
        }

        public static void atualizarDadosLocalizacao ( double lat, double lon){

            DatabaseReference localUsuario = ConfiguracaoFirebase.getFirebaseDatabase().child("local_usuario");
            GeoFire geoFire = new GeoFire(localUsuario);

            Usuario usuarioLogado = UsuarioFirebase.getDadoUsuarioLogado();

            geoFire.setLocation(usuarioLogado.getId(), new GeoLocation(lat, lon), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        Log.d("Erro", "Erro ao salvar local");
                    }
                }
            });
        }


    }

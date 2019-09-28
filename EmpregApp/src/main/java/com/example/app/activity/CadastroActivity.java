package com.example.app.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.example.app.R;
import com.example.app.config.ConfiguracaoFirebase;
import com.example.app.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText campoNome, campoEmail, campoSenha;
    private Switch switchTipoUsuario;

    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        switchTipoUsuario = findViewById(R.id.switchTipoUsuario);
    }

    public void validarCadastroUsuario(View view) {
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if (!textoNome.isEmpty()) { // Verificando Nome
            if (!textoEmail.isEmpty()) { // Verificando Email
                if (!textoSenha.isEmpty()) { // Verificando Senha
                    Usuario usuario = new Usuario();
                    usuario.setNome(textoNome);
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);
                    usuario.setTipo(verificaTipoUsuario());

                    cadastrarUsuario(usuario);
                } else {
                    Toast.makeText(CadastroActivity.this, "Preencha a senha!",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CadastroActivity.this, "Preencha o email!",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CadastroActivity.this, "Preencha o nome!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void cadastrarUsuario(final Usuario usuario) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String idUsuario = task.getResult().getUser().getUid();
                            usuario.setId(idUsuario);
                            usuario.salvar();

                            if (verificaTipoUsuario() == "P") {
                                startActivity(new Intent(CadastroActivity.this,
                                        MapsActivity.class));
                                finish();
                                Toast.makeText(CadastroActivity.this,
                                        "Sucesso ao cadastrar Patrão(oa)!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                startActivity(new Intent(CadastroActivity.this,
                                        RequisicoesActivity.class));
                                finish();
                                Toast.makeText(CadastroActivity.this,
                                        "Sucesso ao cadastrar Faxineiro(a)!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    public String verificaTipoUsuario() {
        return switchTipoUsuario.isChecked() ? "F" : "P";
    }
}

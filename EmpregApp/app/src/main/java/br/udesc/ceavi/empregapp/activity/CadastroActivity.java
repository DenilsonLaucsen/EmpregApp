package br.udesc.ceavi.empregapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.provider.SelfDestructiveThread;
import br.udesc.ceavi.empregapp.R;
import br.udesc.ceavi.empregapp.config.ConfiguracaoFirebase;
import br.udesc.ceavi.empregapp.helper.UsuarioFirebase;
import br.udesc.ceavi.empregapp.model.PerfilUsuario;
import br.udesc.ceavi.empregapp.model.Usuario;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

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
                    usuario.setLatitude(0d);
                    usuario.setLongitude(0d);
                    usuario.setTipo(verificaTipoUsuario().toString());


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
                            try {
                                String idUsuario = task.getResult().getUser().getUid();
                                usuario.setId(idUsuario);
                                usuario.salvar();

                                UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                                if (usuario.getTipo().equals(PerfilUsuario.PATRAO.toString())) {
                                    Toast.makeText(CadastroActivity.this,
                                            "Sucesso ao cadastrar Patrão(oa)!",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(CadastroActivity.this,
                                            "Sucesso ao cadastrar Faxineiro(a)!",
                                            Toast.LENGTH_SHORT).show();
                                }

                                UsuarioFirebase.redirecionaUsuarioLogado(CadastroActivity.this);
                                finish();
                            } catch ( Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            String excecao = "";
                            try {
                                throw task.getException();
                            }catch ( FirebaseAuthWeakPasswordException e){
                                excecao = "Digite uma senha mais forte!";
                            }catch ( FirebaseAuthInvalidCredentialsException e){
                                excecao= "Por favor, digite um e-mail válido";
                            }catch ( FirebaseAuthUserCollisionException e){
                                excecao = "Este conta já foi cadastrada";
                            }catch (Exception e){
                                excecao = "Erro ao cadastrar usuário: "  + e.getMessage();
                                e.printStackTrace();
                            }

                            Toast.makeText(CadastroActivity.this,
                                    excecao,
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    public PerfilUsuario verificaTipoUsuario() {
        return switchTipoUsuario.isChecked() ? PerfilUsuario.EMPREGADO : PerfilUsuario.PATRAO;
    }
}

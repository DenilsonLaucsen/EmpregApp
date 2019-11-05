package br.udesc.ceavi.empregapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import br.udesc.ceavi.empregapp.R;
import br.udesc.ceavi.empregapp.config.ConfiguracaoFirebase;
import br.udesc.ceavi.empregapp.helper.UsuarioFirebase;
import br.udesc.ceavi.empregapp.model.Usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RequisicoesActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requisicoes);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSair :
                autenticacao.signOut();
                finish();
                Intent i = new Intent(this, MainActivity.class);
                this.startActivity(i);
                break;
            case R.id.menuEditarDados :
                UsuarioFirebase.redirecionaEditarDados(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

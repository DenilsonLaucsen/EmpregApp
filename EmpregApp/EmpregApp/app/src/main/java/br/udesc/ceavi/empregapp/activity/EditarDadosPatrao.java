package br.udesc.ceavi.empregapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import br.udesc.ceavi.empregapp.R;
import br.udesc.ceavi.empregapp.helper.UsuarioFirebase;
import br.udesc.ceavi.empregapp.model.Usuario;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class EditarDadosPatrao extends AppCompatActivity {

    private EditText editNome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_dados_patrao);
        editNome = findViewById(R.id.editEdicaoNomePatrao);
        Usuario usuario = UsuarioFirebase.getDadosUsuarioLogado();
        editNome.setText(usuario.getNome());
    }

    public void finalizar(View view) {
        Usuario usuario = UsuarioFirebase.getDadosUsuarioLogado();
        usuario.setNome(editNome.getText().toString());
        usuario.salvar();
        finish();
    }
}

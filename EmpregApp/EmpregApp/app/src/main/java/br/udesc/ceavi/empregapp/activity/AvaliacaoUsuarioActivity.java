package br.udesc.ceavi.empregapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import br.udesc.ceavi.empregapp.R;

public class AvaliacaoUsuarioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avaliacao_usuario);
        getSupportActionBar().setTitle("Avalição do usuário");

        final RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        Button btnEnviar = (Button) findViewById(R.id.btnEnviar);
        final TextView tvAvaliacao = (TextView) findViewById(R.id.tvAvaliacao);


        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvAvaliacao.setText("Sua avaliação é: " + ratingBar.getRating());
            }
        });

    }

}

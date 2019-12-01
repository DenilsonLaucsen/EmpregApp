package br.udesc.ceavi.empregapp.adapter;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.List;

import br.udesc.ceavi.empregapp.R;
import br.udesc.ceavi.empregapp.model.Requisicao;
import br.udesc.ceavi.empregapp.model.Usuario;

public class RequisicoesAdapter extends RecyclerView.Adapter<RequisicoesAdapter.MyViewHolder> {

    private List<Requisicao> requisicoes;
    private Context context;
    private Usuario empregado;

    public RequisicoesAdapter(List<Requisicao> requisicoes, Context context, Usuario empregado) {
        this.requisicoes = requisicoes;
        this.context = context;
        this.empregado = empregado;
    }

    private String formatarDistancia(float distancia){
        String distanciaFormatada;
        if (distancia < 1){
            distancia = distancia * 1000;
            distanciaFormatada = Math.round(distancia) + " M ";
        }else{
            DecimalFormat decimal = new DecimalFormat("0.0");
            distanciaFormatada = decimal.format(distancia) + " KM ";
        }

        return distanciaFormatada;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_requisicoes, parent, false);
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Requisicao requisicao = requisicoes.get(position);
        Usuario patrao = requisicao.getPatrao();
        Usuario empregado = requisicao.getEmpregado();

        holder.nome.setText(patrao.getNome());

        if (empregado != null && patrao != null){
            //calculo da distancia
            Location localInicio = new Location("Local inicial");
            localInicio.setLatitude(patrao.getLatitude());
            localInicio.setLongitude(patrao.getLongitude());

            Location localFinal = new Location("Local final");
            localFinal.setLongitude(empregado.getLongitude());
            localFinal.setLatitude(empregado.getLatitude());

            float distancia = localInicio.distanceTo(localFinal) / 1000;
            String distanciaFormatada = formatarDistancia(distancia);
            holder.distancia.setText(distanciaFormatada + " - aproximadamente");
        }

    }

    @Override
    public int getItemCount() {
        return requisicoes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nome, distancia;

        public MyViewHolder(View itemView){
            super(itemView);

            nome = itemView.findViewById(R.id.textRequisicaoNome);
            distancia = itemView.findViewById(R.id.textRequisicaoDistancia);
        }
    }

}

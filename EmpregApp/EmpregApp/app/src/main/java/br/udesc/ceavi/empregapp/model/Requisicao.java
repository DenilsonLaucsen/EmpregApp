package br.udesc.ceavi.empregapp.model;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import br.udesc.ceavi.empregapp.config.ConfiguracaoFirebase;

public class Requisicao {

    private String id;
    private String status;
    private Usuario patrao;
    private Usuario empregado;

    public static final String STATUS_AGUARDANDO = "aguardando";
    public static final String STATUS_A_CAMINHO = "acaminho";
    public static final String STATUS_EM_SERVICO = "emservico";
    public static final String STATUS_FINALIZADO = "finalizado";
    public static final String STATUS_CANCELADA = "cancleada";

    public Requisicao() {
    }

    public void salvar() {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        String idRequisicao = requisicoes.push().getKey();
        setId(idRequisicao);

        requisicoes.child(getId()).setValue(this);
    }

    public void atualizar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("empregado", getEmpregado());
        objeto.put("status", getStatus());

        requisicao.updateChildren(objeto);
    }

    public void atualizarStatus(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("status", getStatus());

        requisicao.updateChildren(objeto);
    }

    public void atualizarLocalizacao(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId()).child("empregado");

        Map objeto = new HashMap();
        objeto.put("latitude", getEmpregado().getLatitude());
        objeto.put("longitude", getEmpregado().getLongitude());

        requisicao.updateChildren(objeto);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPatrao() {
        return patrao;
    }

    public void setPatrao(Usuario patrao) {
        this.patrao = patrao;
    }

    public Usuario getEmpregado() {
        return empregado;
    }

    public void setEmpregado(Usuario empregado) {
        this.empregado = empregado;
    }
}

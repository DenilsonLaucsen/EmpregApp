package br.udesc.ceavi.empregapp.model;

public enum PerfilUsuario {

    PATRAO("Patrão",1),
    EMPREGADO("Empregado",2);

    private String nome;
    private int codigo;

    PerfilUsuario(String nome, int codigo) {
        this.nome = nome;
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}

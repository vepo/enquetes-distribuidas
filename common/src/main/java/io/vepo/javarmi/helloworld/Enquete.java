package io.vepo.javarmi.helloworld;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Enquete implements Serializable {
    private String id;
    private String titulo;
    private String local;
    private LocalDateTime dataLimite;
    private List<LocalDateTime> opcoes;
    private String criador;
    private boolean finalizada;

    public Enquete(String id,
                   String titulo, 
                   String local, 
                   String criador, 
                   LocalDateTime dataLimite, 
                   List<LocalDateTime> opcoes) {
        this.id = id;
        this.titulo = titulo;
        this.local = local;
        this.criador = criador;
        this.dataLimite = dataLimite;
        this.opcoes = opcoes;
        this.finalizada = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCriador() {
        return criador;
    }

    public void setCriador(String criador) {
        this.criador = criador;
    }

    public List<LocalDateTime> getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(List<LocalDateTime> opcoes) {
        this.opcoes = opcoes;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDateTime getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(LocalDateTime dataLimite) {
        this.dataLimite = dataLimite;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titulo, local, criador, dataLimite, opcoes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Enquete)) {
            return false;
        } else {
            Enquete other = (Enquete) obj;
            return Objects.equals(id, other.id) && 
                   Objects.equals(titulo, other.titulo) && 
                   Objects.equals(local, other.local) && 
                   Objects.equals(criador, other.criador) &&
                   Objects.equals(dataLimite, other.dataLimite) &&  
                   Objects.equals(opcoes, other.opcoes) &&
                   finalizada == other.finalizada;
        }
    }

    @Override
    public String toString() {
        return String.format("Enquete [id=%s, titulo=%s, local=%s, criador=%s, finalizada=%s, dataLimite=%s, opcoes=%s]", id, titulo, local, criador, finalizada, dataLimite, opcoes);
    }

}

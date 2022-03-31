package io.vepo.javarmi.helloworld;

import java.util.List;
import java.util.Objects;

public class Voto {
    private String nomeUsuario;
    private List<Integer> votos;

    public Voto(String nomeUsuario, List<Integer> votos) {
        this.nomeUsuario = nomeUsuario;
        this.votos = votos;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public List<Integer> getVotos() {
        return votos;
    }

    public void setVotos(List<Integer> votos) {
        this.votos = votos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nomeUsuario, votos);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof Voto)) {
            return false;
        } else {
            Voto other = (Voto) obj;
            return Objects.equals(nomeUsuario, other.nomeUsuario) &&
                   Objects.equals(votos, other.votos);
        }
    }

    @Override
    public String toString() {
        return String.format("Voto [nomeUsuario=%s, votos=%s]", nomeUsuario, votos);
    }
}

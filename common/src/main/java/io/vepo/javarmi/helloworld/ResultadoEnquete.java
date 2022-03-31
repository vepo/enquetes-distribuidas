package io.vepo.javarmi.helloworld;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ResultadoEnquete implements Serializable {
    private LocalDateTime opcao;
    private List<String> votantes;
    private int votos;

    public ResultadoEnquete(LocalDateTime opcao, List<String> votantes, int votos) {
        this.opcao = opcao;
        this.votantes = votantes;
        this.votos = votos;
    }

    public LocalDateTime getOpcao() {
        return opcao;
    }

    public void setOpcao(LocalDateTime opcao) {
        this.opcao = opcao;
    }

    public List<String> getVotantes() {
        return votantes;
    }

    public void setVotantes(List<String> votantes) {
        this.votantes = votantes;
    }

    public int getVotos() {
        return votos;
    }
    public void setVotos(int votos) {
        this.votos = votos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(opcao, votantes, votos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else {
            ResultadoEnquete other = (ResultadoEnquete) obj;
            return Objects.equals(opcao, other.opcao) && 
                   Objects.equals(votantes, other.votantes) && 
                   votos == other.votos;
        }
    }

    @Override
    public String toString() {
        return String.format("ResultadoEnquete [opcao=%s, votantes=%s, votos=%d]", opcao, votantes, votos);
    }
}

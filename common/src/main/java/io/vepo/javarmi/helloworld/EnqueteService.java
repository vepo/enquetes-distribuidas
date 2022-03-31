package io.vepo.javarmi.helloworld;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Interface do servidor de enquetes.
 */
public interface EnqueteService extends Remote {
    /**
     * Registra novo usuário.
     * 
     * @param nomeUsuario  nome do usuário
     * @param chavePublica chave pública do usuário que será usada para validar
     *                     acesso
     * @param cliente      cliente para subscrição de resultados
     * @throws RemoteException              Erro ao acessar cliente. Provavelmente o
     *                                      cliente não está disponível.
     * @throws UsuarioJaCadastradoException Usuário já cadastrado e ativo
     */
    public abstract void registrar(String nomeUsuario, PublicKey chavePublica, EnqueteSubscriber cliente)
            throws RemoteException, UsuarioJaCadastradoException;

    /**
     * Cria nova enquete.
     * 
     * @param titulo     título da enquete
     * @param local      local em que a enquete será realizada
     * @param criador    nome do usuário criador da enquete
     * @param dataLimite data limite da enquete
     * @param opcoes     opções de data para votar
     * @return nova enquete criada
     * @throws RemoteException Erro ao acessar cliente. Provavelmente o
     *                         cliente não está disponível.
     */
    public abstract Enquete criar(String titulo, String local, String criador, LocalDateTime dataLimite,
            List<LocalDateTime> opcoes) throws RemoteException;

    /**
     * Lista todas as enquetes cadastradas.
     * 
     * @return todas as enquetes cadastradas
     * @throws RemoteException Erro ao acessar cliente. Provavelmente o
     *                         cliente não está disponível.
     */
    public abstract List<Enquete> listar() throws RemoteException;

    /**
     * Votar na enquete.
     * 
     * @param nomeUsuario nome do usuário
     * @param idEnquete   id da enquete
     * @param votos       indeces das opções selecionadas
     * @throws RemoteException Erro ao acessar cliente. Provavelmente o
     *                         cliente não está disponível.
     */
    public abstract void votar(String nomeUsuario, String idEnquete, List<Integer> votos) throws RemoteException;

    /**
     * Lista todas as enquetes não votadas pelo usuário.
     * 
     * @param nomeUsuario nome do usuário
     * @return lista com todas as enquetes não votadas
     * @throws RemoteException Erro ao acessar cliente. Provavelmente o
     *                         cliente não está disponível.
     */
    public abstract Set<Enquete> enquetesNaoVotadas(String nomeUsuario) throws RemoteException;

    /**
     * Consulta resultado de enquete cadastrada.
     * 
     * @param enqueteId  Id da enquete
     * @param assinatura Assinatura contendo IdEnquete + Titulo + Nome do usuário
     *                   assinado pela chave privada do autor da enquete.
     * @return Resultados caso a assinatura seja validada. null caso contrário.
     * @throws RemoteException Erro ao acessar cliente. Provavelmente o
     *                         cliente não está disponível.
     */
    public abstract Set<ResultadoEnquete> verResultados(String enqueteId, byte[] assinatura) throws RemoteException;
}
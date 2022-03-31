package io.vepo.javarmi.helloworld;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de enquetes.
 * Para expirar as enquetes, ele possue um timer que a cada
 * minuto verificará as enquetes já expiradas.
 * A interface {@link AutoCloseable} é responsável por evitar leak desse timer.
 * 
 * <b>Observação</b>: O modelo de dados não está bom, precisa ser refatorado para ficar evitar inconsistências.
 */
public class EnqueteServiceImpl extends UnicastRemoteObject implements EnqueteService, AutoCloseable {

    private Map<String, EnqueteSubscriber> subscribers;
    private Map<String, PublicKey> chavesPublicas;
    private List<Enquete> enquetes;
    private Map<String, List<String>> enqueteSubscribers;
    private Map<String, List<Voto>> votos;
    private Timer timer;

    protected EnqueteServiceImpl() throws RemoteException {
        subscribers = Collections.synchronizedMap(new HashMap<>());
        chavesPublicas = Collections.synchronizedMap(new HashMap<>());
        enqueteSubscribers = Collections.synchronizedMap(new HashMap<>());
        enquetes = Collections.synchronizedList(new ArrayList<>());
        votos = Collections.synchronizedMap(new HashMap<>());
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                finalizar();
            }
        }, 0L, 1000L * 60);
    }

    @Override
    public void registrar(String nomeUsuario, PublicKey chavePublica, EnqueteSubscriber subscriber)
            throws RemoteException, UsuarioJaCadastradoException {
        /**
         * O cadastro do usuário é feito baseado no nome do usuário. Não será permitido 2 usuários com mesmo nome.
         * Para verificar se um usuário ainda está ativo, é feita a chamada para verificar se ele está responsivo.
         */
        if (subscribers.containsKey(nomeUsuario)) {
            try {
                if (subscribers.get(nomeUsuario).ativo()) {
                    System.err.println("Usuário ativo. Não é possível registrar novo usuário! usuario=" + nomeUsuario);
                    throw new UsuarioJaCadastradoException();
                }
            } catch (RemoteException re) {
                System.out.println("Usuário não disponível. Registrando novo usuário \"" + nomeUsuario
                        + "\" chave pública chave=\"" + chavePublica + "\"");
                subscribers.put(nomeUsuario, subscriber);
                chavesPublicas.put(nomeUsuario, chavePublica);
            }
        } else {
            System.out.println("Registrando usuário \"" + nomeUsuario + "\" chave pública chave=\"" + chavePublica + "\"");
            subscribers.put(nomeUsuario, subscriber);
            chavesPublicas.put(nomeUsuario, chavePublica);
        }
    }

    @Override
    public Enquete criar(String titulo, String local, String criador, LocalDateTime dataLimite,
            List<LocalDateTime> opcoes) throws RemoteException {
        Enquete enquete = new Enquete(UUID.randomUUID().toString(), titulo, local, criador, dataLimite, opcoes);
        System.out.println("Enquete criada! enquete=" + enquete);
        enquetes.add(enquete);
        List<String> subscriberList = new LinkedList<>();
        subscriberList.add(enquete.getCriador());
        enqueteSubscribers.put(enquete.getId(), subscriberList);
        subscribers.entrySet()
                   .stream()
                   .filter(entry -> !entry.getKey().equals(enquete.getCriador()))
                   .forEach(entry -> {
                       try {
                           entry.getValue().novaEnquete(enquete);
                       } catch (RemoteException e) {
                           System.err.println("Observador não acessível! Removendo...");
                           subscribers.remove(entry.getKey());
                       }
                   });
        return enquete;
    }

    @Override
    public List<Enquete> listar() throws RemoteException {
        return this.enquetes;
    }

    @Override
    public void votar(String nomeUsuario, String idEnquete, List<Integer> votos) {
        encontrarEnquete(idEnquete).ifPresent(enquete-> {
            this.enqueteSubscribers.computeIfAbsent(idEnquete, key -> new ArrayList<>()).add(nomeUsuario);
            this.votos.computeIfAbsent(idEnquete, key -> new ArrayList<>()).add(new Voto(nomeUsuario, votos));
            System.out.println("Voto registrado! nomeUsuario=" + nomeUsuario + " idEnquete=" + idEnquete);
            if (votacaoConcluida(enquete)) {
                System.out.println("Votação concluída!");
                enquete.setFinalizada(true);
                informaObservadores(enquete);
            }
        });
    }

    private Optional<Enquete> encontrarEnquete(String idEnquete) {
        return enquetes.stream()
                       .filter(enquete -> enquete.getId().equals(idEnquete))
                       .findFirst();
    }

    private void informaObservadores(Enquete enquete) {
        this.enqueteSubscribers.computeIfAbsent(enquete.getId(), key -> new ArrayList<>())
                               .stream()
                               .map(nomeUsuario -> this.subscribers.get(nomeUsuario))
                               .forEach(subscriber -> {
                                   try {
                                       subscriber.votacaoConcluida(enquete);
                                   } catch (RemoteException e) {
                                       System.err.println("Cliente não disponível!");
                                       subscribers.entrySet()
                                                  .stream()
                                                  .filter(entry -> !entry.getValue().equals(subscriber))
                                                  .findFirst()
                                                  .ifPresent(entry -> subscribers.remove(entry.getKey()));
                                   }
                               });
    }

    private boolean votacaoConcluida(Enquete enquete) {
        return votos.computeIfAbsent(enquete.getId(), key -> new ArrayList<>())
                    .stream()
                    .map(Voto::getNomeUsuario)
                    .collect(Collectors.toSet())
                    .equals(subscribers.keySet()
                                       .stream()
                                       .filter(nomeUsuario -> !nomeUsuario.equals(enquete.getCriador()))
                                       .collect(Collectors.toSet()));
    }

    @Override
    public Set<Enquete> enquetesNaoVotadas(String nomeUsuario) throws RemoteException {
        return enquetes.stream()
                       .filter(enquete -> !votos.containsKey(enquete.getId()) ||
                                          votos.get(enquete.getId())
                                               .stream()
                                               .noneMatch(voto -> voto.getNomeUsuario().equals(nomeUsuario)))
                       .collect(Collectors.toSet());
    } 

    private void finalizar() {
        System.out.println("Verificando prazo de validade das enquetes...");
        LocalDateTime now = LocalDateTime.now();
        this.enquetes.stream()
                     .filter(enquete -> !enquete.isFinalizada() && enquete.getDataLimite().isBefore(now))
                     .forEach(enquete -> {
                        System.out.println("Finalizando enquete por tempo! enquete=" + enquete);
                        enquete.setFinalizada(true);
                        informaObservadores(enquete);
                     });
    }

    @Override
    public void close() {
        if (Objects.nonNull(this.timer)) {
            this.timer.cancel();
        }
    }

    @Override
    public Set<ResultadoEnquete> verResultados(String idEnquete, byte[] assinatura) throws RemoteException {
        Optional<Enquete> talvezEnquete = encontrarEnquete(idEnquete);
        if (talvezEnquete.isPresent()) {
            Enquete enquete = talvezEnquete.get();
            try {
                Signature clientSig = Signature.getInstance("SHA256withRSA");
                clientSig.initVerify(chavesPublicas.get(enquete.getCriador()));
                clientSig.update((enquete.getId() + enquete.getTitulo() + enquete.getCriador()).getBytes());

                if (clientSig.verify(assinatura)) {
                    //Mensagem corretamente assinada
                    System.out.println("A Mensagem recebida foi assinada corretamente.");
                    return construirResultados(enquete);
                } else {
                    //Mensagem não pode ser validada
                    System.out.println("A Mensagem recebida NÃO pode ser validada.");
                    return null;
                }
            } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                System.err.println("Erro ao validar assinatura!");
                return null;
            }
        } else {
            System.err.println("Enquete não encontrada!");
            return null;
        }
    }

    private Set<ResultadoEnquete> construirResultados(Enquete enquete) {
        List<Voto> votosDaEnquete = votos.get(enquete.getId());
        System.out.println("Construindo resultados... enquete=" + enquete + " votos=" + votosDaEnquete);
        if (Objects.nonNull(votosDaEnquete)) {
            return enquete.getOpcoes()
                          .stream()
                          .map(opcao -> { 
                              AtomicInteger qtdVotos = new AtomicInteger(0);
                              List<String> votantes = new ArrayList<>();
                              votosDaEnquete.forEach(voto -> {
                                  if (voto.getVotos().contains(enquete.getOpcoes().indexOf(opcao))) {
                                      qtdVotos.incrementAndGet();
                                      votantes.add(voto.getNomeUsuario());
                                    }
                                });
                                return new ResultadoEnquete(opcao, votantes, qtdVotos.get());
                          })
                          .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }
}

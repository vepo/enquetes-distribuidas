package io.vepo.javarmi.helloworld;

import java.io.File;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
/**
 * A lógica do cliente é simples, imprime o menu na tela e o usuário deve escolher 
 * uma opção. Quando chegam notificações, elas são impressas na tela.
 */
@Command(name = "enquete-cli", mixinStandardHelpOptions = true, 
        version = "Cliente Enquete 1.0", description = "Cliente para acesso a enquetes.")
public class EnqueteCliente implements Callable<Integer> {

    private static final int ERRO_SERVIDOR_NAO_ACESSIVEL = 1;
    private static final int ERRO_SERVICO_ENQUETE_NAO_ENCONTRADO = 2;
    private static final int ERRO_USUARIO_JA_CADASTRADO = 3;
    private static final int ERRO_USUARIO_CHAVE_JA_CADASTRADA = 4;
    private static final int ERRO_SEGURANCA = 5;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    @Option(names = { "-p",
            "--porta" }, showDefaultValue = Visibility.ALWAYS, description = "Porta do servidor de enquetes", defaultValue = "1098")
    private int port;

    @Option(names = "--host", description = "Host do Servidor de enquetes", defaultValue = "localhost")
    private String host;

    @Option(names = { "-n", "--nome" }, required = true, description = "Nome do usuário do servidor de enquetes")
    private String nomeUsuario;

    @Option(names = { "-i", "--iniciar" }, description = "Iniciar base de dados com enquetes.", defaultValue = "false")
    private boolean iniciarBase;

    @Option(names = "--private-key", description = "Chave privada", required = true)
    private File privateKeyFile;

    @Option(names = "--public-key", description = "Chave pública", required = true)
    private File publicKeyFile;

    private AtomicBoolean running;
    private AtomicBoolean enqueteRecebida;
    private Registry referenciaServicoNomes;
    private EnqueteService enqueteService;
    private String id;
    private ConsoleInterface console;
    private List<Enquete> enquetesNaoRespondidas;
    private PrivateKey privateKey;

    public EnqueteCliente() {
        id = UUID.randomUUID().toString();
        console = new ConsoleInterface();
        enquetesNaoRespondidas = Collections.synchronizedList(new ArrayList<>());
    }

    private int conectar() {
        try {
            privateKey = readPrivateKey(privateKeyFile);
            running = new AtomicBoolean(true);
            enqueteRecebida = new AtomicBoolean(false);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> running.set(false)));
            referenciaServicoNomes = LocateRegistry.getRegistry(host, port);
            System.out.println("Conectando ao servidor de enquetes host=" + host + " port=" + port);
            enqueteService = (EnqueteService) referenciaServicoNomes.lookup("Enquete");
            return 0;
        } catch (RemoteException e) {
            System.err.println("Não foi possível conectar ao servidor de nomes!");
            e.printStackTrace();
            return ERRO_SERVIDOR_NAO_ACESSIVEL;
        } catch (NotBoundException e) {
            System.out.println("Serviço não encontrado no servidor de nome!");
            e.printStackTrace();
            return ERRO_SERVICO_ENQUETE_NAO_ENCONTRADO;
        } catch (InvalidKeyException e) {
            System.err.println("Erro ao iniciar criptografia!");
            e.printStackTrace();
            return ERRO_SEGURANCA;
        } catch (Exception e) {
            System.err.println("Erro ao iniciar criptografia!");
            e.printStackTrace();
            return ERRO_SEGURANCA;
        }
    }

    private static PrivateKey readPrivateKey(File file) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Files.readAllBytes(file.toPath()));
        return keyFactory.generatePrivate(keySpec);
    }

    private static PublicKey readPublicKey(File file) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Files.readAllBytes(file.toPath()));
        return keyFactory.generatePublic(keySpec);
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new EnqueteCliente()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        int conectarCodigo = conectar();
        if (conectarCodigo == 0) {
            try {
                if (iniciarBase) {
                    String idEnquete = UUID.randomUUID().toString();
                    enqueteService.criar("Enquete " + idEnquete,
                                         "Local",
                                         nomeUsuario,
                                         LocalDateTime.now().plusDays(1),
                                         Arrays.asList(LocalDateTime.now().plusMonths(1),
                                                 LocalDateTime.now().plusMonths(2),
                                                 LocalDateTime.now().plusMonths(3),
                                                 LocalDateTime.now().plusMonths(4)));
                }

                enquetesNaoRespondidas.addAll(enqueteService.enquetesNaoVotadas(nomeUsuario));
                enqueteRecebida.set(enquetesNaoRespondidas.size() > 0);

                EnqueteSubscriber local = new EnqueteSubscriberImpl(id, 
                                                                    nomeUsuario, 
                                                                    readPublicKey(publicKeyFile), 
                                                                    enqueteService, 
                                                                    this::novaEnquete,
                                                                    this::enqueteFinalizada);
                console.adicionarOpcao("Criar Enquete", this::criarEnequete);
                console.adicionarOpcao("Responder enquete", this::responderEnquete);
                console.adicionarOpcao("Listar enquete cadastradas", this::listarEnquetes);
                console.adicionarOpcao("Ver resultado", this::verResultado);
                console.adicionarOpcao("Sair", this::sair);

                AtomicBoolean telaLimpa = new AtomicBoolean(false);
                while (running.get()) {
                    Thread.sleep(500);
                    if (enqueteRecebida.get()) {
                        System.out.println("Novas enquetes foram recebidas.");
                        enqueteRecebida.set(false);
                        telaLimpa.set(false);
                    }

                    if (!telaLimpa.get()) {
                        System.out.println("Existem " + enquetesNaoRespondidas.size() + " não respondidas!");
                        console.exibirMenu();
                        telaLimpa.set(true);
                    }
                    console.selecao(5).ifPresent(opcao -> {
                        telaLimpa.set(false);
                        opcao.run();
                    });
                }
                System.out.println("Finalizando cliente nome=" + nomeUsuario);
                return 0;
            } catch (UsuarioJaCadastradoException ujce) {
                return ERRO_USUARIO_JA_CADASTRADO;
            } catch (ChavePublicaInvalidaException cpie) {
                return ERRO_USUARIO_CHAVE_JA_CADASTRADA;
            }
        } else {
            return conectarCodigo;
        }
    }

    private void sair() {
        this.running.set(false);
    }

    private void novaEnquete(Enquete enquete) {
        System.out.println();
        System.out.println("-----------------------------------------------------");
        System.out.println("Nova enquete! enquete=" + enquete);
        System.out.println("-----------------------------------------------------");
        enqueteRecebida.set(true);
        enquetesNaoRespondidas.add(enquete);
    }

    private void enqueteFinalizada(Enquete enquete) {
        System.out.println();
        System.out.println("-----------------------------------------------------");
        System.out.println("Enquete finalizada! enquete=" + enquete);
        System.out.println("-----------------------------------------------------");
        enqueteRecebida.set(true);
        enquetesNaoRespondidas.removeIf(e -> e.getId().equals(enquete.getId()));
    }

    private void responderEnquete() {
        while (enquetesNaoRespondidas.size() > 0) {
            System.out.println("Listando enquetes...");
            System.out.println("-----------------------------------------------------");
            for (int index = 0; index < enquetesNaoRespondidas.size(); index++) {
                System.out.println("Enquete " + (index + 1) + ": " + enquetesNaoRespondidas.get(index).getTitulo());
            }
            System.out.print("Escolha uma enquete (0 para finalizar): ");
            try {
                int opcao = Integer.parseInt(console.proximaLinha(false));
                if (opcao > enquetesNaoRespondidas.size()) {
                    System.err.println("Valor inválido! Escolha um valor inteiro entre [0,"
                            + enquetesNaoRespondidas.size() + "].");
                } else {
                    if (opcao == 0) {
                        break;
                    }

                    Enquete enquete = enquetesNaoRespondidas.get(opcao - 1);
                    for (int index = 0; index < enquete.getOpcoes().size(); index++) {
                        System.out.println(
                                "Opção [" + (index + 1) + "]: " + FORMATTER.format(enquete.getOpcoes().get(index)));
                    }
                    System.out.print("Escolha todas as possíveis opções (use \",\" como separador): ");
                    try {
                        List<Integer> voto = Arrays.asList(console.proximaLinha(false).split(","))
                                .stream()
                                .map(String::trim)
                                .mapToInt(Integer::parseInt)
                                .map(value -> value - 1)
                                .boxed()
                                .collect(Collectors.toList());
                        Optional<Integer> talvezMax = voto.stream().max(Comparator.naturalOrder());
                        if (voto.size() == 0 || talvezMax.get() >= enquete.getOpcoes().size()) {
                            System.err.println("Valor inválido! Escolha um valor inteiro entre [0,"
                                    + enquete.getOpcoes().size() + "].");
                        } else {
                            try {
                                enqueteService.votar(nomeUsuario, enquete.getId(), voto);
                                enquetesNaoRespondidas.remove(enquete);
                                System.out.println("Voto registrado! enquete=" + enquete);
                            } catch (RemoteException re) {
                                System.err.println("Servidor não acessível!");
                                System.exit(ERRO_SERVIDOR_NAO_ACESSIVEL);
                            }
                        }
                    } catch (NumberFormatException nfe) {
                        System.err.println("Valor inválido! Escolha um valor inteiro entre [0,"
                                + enquete.getOpcoes().size() + "].");
                    }
                }
            } catch (NumberFormatException nfe) {
                System.err.println(
                        "Valor inválido! Escolha um valor inteiro entre [0," + enquetesNaoRespondidas.size() + "].");
            }
            System.out.println("-----------------------------------------------------");
        }
    }

    private void verResultado() {
        try {
            System.out.println("Selectione uma enquete enquetes...");
            System.out.println("-----------------------------------------------------");
            List<Enquete> enquetes = this.enqueteService.listar();
            
            for (int index = 0; index < enquetes.size(); index++) {
                System.out.println("Enquete " + (index + 1) + ": " + enquetes.get(index).getTitulo());
            }
            
            System.out.print("Escolha uma enquete (0 para finalizar): ");
            try {
                int opcao = Integer.parseInt(console.proximaLinha(false));
                if (opcao > enquetes.size()) {
                    System.err.println("Valor inválido! Escolha um valor inteiro entre [0,"
                            + enquetes.size() + "].");
                } else {
                    Enquete enquete = enquetes.get(opcao - 1);
                    try {
                        Set<ResultadoEnquete> resultados = enqueteService.verResultados(enquete.getId(), nomeUsuario, assinar(enquete.getId() + enquete.getTitulo() + nomeUsuario));
                        if (Objects.nonNull(resultados)) {
                            resultados.forEach(resultado -> System.out.println("Opção " + FORMATTER.format(resultado.getOpcao()) + 
                                                                                ": #votos=" + resultado.getVotos() + 
                                                                                " votantes=" + resultado.getVotantes()
                                                                                                        .stream()
                                                                                                        .collect(Collectors.joining(", "))));;
                        } else {
                            System.err.println("Resultado não pode ser acessado!");
                        }
                    } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
                        System.err.println("Erro ao gerar assinatura!");
                        e.printStackTrace();
                    }
                }
            } catch (NumberFormatException nfe) {
                System.err.println("Valor inválido! Escolha um valor inteiro entre [0," + enquetes.size() + "].");
            }
        } catch (RemoteException e) {
            System.err.println("Servidor não acessível!");
            System.exit(ERRO_SERVIDOR_NAO_ACESSIVEL);
        }
        System.out.println("-----------------------------------------------------");
    }

    private byte[] assinar(String mensagem) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(mensagem.getBytes());
        return sig.sign();
    }

    private void listarEnquetes() {
        System.out.println("Listando enquetes...");
        System.out.println("-----------------------------------------------------");
        try {
            this.enqueteService.listar()
                    .forEach(System.out::println);
        } catch (RemoteException e) {
            System.err.println("Servidor não acessível!");
            System.exit(ERRO_SERVIDOR_NAO_ACESSIVEL);
        }
        System.out.println("-----------------------------------------------------");
    }

    private void criarEnequete() {
        console.limpaBuffer();
        System.out.println("Criando Enquete...");
        System.out.print("Titulo: ");
        String titulo = console.proximaLinha(false);
        System.out.print("Local: ");
        String local = console.proximaLinha(false);
        LocalDateTime dataLimite = null;
        do {
            try {
                System.out.print("Data limite (HH:mm dd/MM/yyyy): ");
                dataLimite = LocalDateTime.parse(console.proximaLinha(true), FORMATTER);
            } catch (DateTimeParseException dtpe) {
                System.err.println("Erro ao processar data! Formato inválido. Utilize HH:mm dd/MM/yyyy!");
            }
        } while (dataLimite == null);
        List<LocalDateTime> opcoes = new ArrayList<>();
        String opcao;
        int index = 1;
        System.out.println(
                "Insira as opções de data, use o formato \"HH:mm dd/MM/yyyy\". Caso queira finalizar, insira um valor vazio.");
        do {
            console.limpaBuffer();
            System.out.print("Opção [" + index + "]: ");
            opcao = console.proximaLinha(true);
            if (!opcao.isEmpty()) {
                try {
                    opcoes.add(LocalDateTime.parse(opcao, FORMATTER));
                } catch (DateTimeParseException dtpe) {
                    System.err.println("Erro ao processar data! Formato inválido. Utilize HH:mm dd/MM/yyyy!");
                    continue;
                }
            }
            if (opcao.isEmpty() && opcoes.isEmpty()) {
                System.err.println("Não é possível criar uma enquete sem opções de data!");
            } else {
                index++;
            }
        } while (opcoes.isEmpty() || !opcao.isEmpty());

        try {
            Enquete enquete = this.enqueteService.criar(titulo, local, nomeUsuario, dataLimite, opcoes);
            System.out.println("Enquete criada! enquete=" + enquete);
        } catch (RemoteException re) {
            System.err.println("Servidor indisponível!");
            System.exit(ERRO_SERVIDOR_NAO_ACESSIVEL);
        }
    }
}

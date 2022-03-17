package io.vepo.javarmi.helloworld;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "enquete-cli", mixinStandardHelpOptions = true, version = "Cliente Enquete 1.0", description = "Cliente para acesso a enquetes.")
public class EnqueteCliente implements Callable<Integer> {

    @Option(names = { "-p", "--porta" }, description = "Porta do servidor de enquetes", defaultValue = "1098")
    private int port;

    @Option(names = "--host", description = "Host do Servidor de enquetes", defaultValue = "localhost")
    private String host;

    @Option(names = { "-n", "--nome" }, required = true, description = "Nome do usuário do servidor de enquetes")
    private String nome;

    public static void main(String... args) {
        int exitCode = new CommandLine(new EnqueteCliente()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        AtomicBoolean running = new AtomicBoolean(true);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> running.set(false)));

        System.out.println("Conectando ao servidor de enquetes host=" + host + " port=" + port);
        Registry referenciaServicoNomes = LocateRegistry.getRegistry(host, port);
        InterfaceServ referenciaServidor = (InterfaceServ) referenciaServicoNomes.lookup("HelloWorld");
        InterfaceCli client = new CliImpl("AAA", 10, referenciaServidor);
        while (running.get()) {
            Thread.sleep(500);
            client.notificar("AAA");
        }
        System.out.println("Finalizando cliente nome=" + nome);
        return 0;
    }

}

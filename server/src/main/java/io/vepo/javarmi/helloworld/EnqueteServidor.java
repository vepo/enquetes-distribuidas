package io.vepo.javarmi.helloworld;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

/**
 * O servidor será responsável por cadastrar os serviços.
 */
@Command(name = "enquete-server", mixinStandardHelpOptions = true, 
         version = "Servidor Enquete 1.0", description = "Servidor de enquetes.")
public class EnqueteServidor implements Callable<Integer> {

    @Option(names = { "-p", "--porta" }, 
            showDefaultValue = Visibility.ALWAYS,
            description = "Porta do servidor de enquetes", 
            defaultValue = "1098")
    private int port;

    private AtomicBoolean running;
    
    public static void main(String[] args)  {
        int exitCode = new CommandLine (new EnqueteServidor()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        running = new AtomicBoolean(true);
        System.out.println("Iniciando servidor... porta=" + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> running.set(false)));
        
        Registry referenciaServicoNomes = LocateRegistry.createRegistry(port);
        try(EnqueteServiceImpl referenciaServidor = new EnqueteServiceImpl()) {
            referenciaServicoNomes.rebind("Enquete", referenciaServidor);
            while (running.get()) {
                Thread.sleep(500);
            }
        }
        return 0;
    }
}

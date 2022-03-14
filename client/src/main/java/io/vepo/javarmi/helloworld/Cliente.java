package io.vepo.javarmi.helloworld;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Cliente {
    public static void main(String[] args) throws RemoteException, NotBoundException, InterruptedException {
        Registry referenciaServicoNomes = LocateRegistry.getRegistry();
        InterfaceServ referenciaServidor = (InterfaceServ) referenciaServicoNomes.lookup("HelloWorld");
        InterfaceCli client = new CliImpl("AAA", 10, referenciaServidor);
        while (true) {
            Thread.sleep(500);
            client.notificar("AAA");
        }
    }
}

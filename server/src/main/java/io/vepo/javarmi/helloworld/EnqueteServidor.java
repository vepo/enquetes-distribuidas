package io.vepo.javarmi.helloworld;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class EnqueteServidor {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, InterruptedException {
        Registry referenciaServicoNomes = LocateRegistry.createRegistry(1099);
        InterfaceServ referenciaServidor = new ServImpl();
        referenciaServicoNomes.rebind("HelloWorld", referenciaServidor);

        while (true) {
            Thread.sleep(500);
        }
    }
}

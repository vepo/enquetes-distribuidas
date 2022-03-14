package io.vepo.javarmi.helloworld;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CliImpl extends UnicastRemoteObject implements InterfaceCli {

    CliImpl(String nome, int id, InterfaceServ referenciaServer) throws RemoteException {
        referenciaServer.registrarInteresse(nome, this);
    }

    @Override
    public void notificar(String texto) {
        System.out.println(texto);
    }

}

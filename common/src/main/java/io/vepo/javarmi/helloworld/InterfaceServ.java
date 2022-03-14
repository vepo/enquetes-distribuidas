package io.vepo.javarmi.helloworld;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceServ extends Remote {
    public abstract void registrarInteresse(String texto, InterfaceCli referenciaCliente) throws RemoteException;
}
package io.vepo.javarmi.helloworld;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceCli extends Remote {
    public abstract void notificar(String texto) throws RemoteException;
}

package io.vepo.javarmi.helloworld;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EnqueteSubscriber extends Remote {
    public abstract boolean ativo() throws RemoteException;
    public abstract void novaEnquete(Enquete enquete) throws RemoteException;
    public abstract void votacaoConcluida(Enquete enquete) throws RemoteException;
}

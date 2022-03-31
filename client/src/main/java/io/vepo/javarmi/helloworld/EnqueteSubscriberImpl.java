package io.vepo.javarmi.helloworld;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.function.Consumer;

public class EnqueteSubscriberImpl extends UnicastRemoteObject implements EnqueteSubscriber {

    private Consumer<Enquete> novaEnqueteCallback;
    private Consumer<Enquete> enqueteFinalizadaCallback;

    EnqueteSubscriberImpl(String idUsuario, 
                          String nomeUsuario, 
                          PublicKey chavePublica,
                          EnqueteService referenciaServer, 
                          Consumer<Enquete> novaEnqueteCallback,
                          Consumer<Enquete> enqueteFinalizadaCallback)
            throws RemoteException, UsuarioJaCadastradoException, ChavePublicaInvalidaException {
        referenciaServer.registrar(nomeUsuario, chavePublica, this);
        this.novaEnqueteCallback = novaEnqueteCallback;
        this.enqueteFinalizadaCallback = enqueteFinalizadaCallback;
    }

    @Override
    public boolean ativo() throws RemoteException {
        return true;
    }

    @Override
    public void novaEnquete(Enquete enquete) throws RemoteException{
        System.out.println("Nova enquete criada! enquete: " + enquete);
        novaEnqueteCallback.accept(enquete);
    }

    @Override
    public void votacaoConcluida(Enquete enquete) throws RemoteException {
        System.out.println("Enquete finalizada! enquete=" + enquete);   
        enqueteFinalizadaCallback.accept(enquete);
    }

}

package io.vepo.javarmi.helloworld;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class ServImpl extends UnicastRemoteObject implements InterfaceServ {

    private Map<String, InterfaceCli> clientes;

    protected ServImpl() throws RemoteException {
        clientes = new HashMap<>();
    }

    @Override
    public void registrarInteresse(String texto, InterfaceCli referenciaCliente) {
        clientes.put(texto, referenciaCliente);
    }

}

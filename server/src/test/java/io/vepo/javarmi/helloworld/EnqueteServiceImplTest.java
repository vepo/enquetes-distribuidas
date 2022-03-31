package io.vepo.javarmi.helloworld;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.rmi.RemoteException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Enquete Service")
public class EnqueteServiceImplTest {

    private class LocalEnqueteSubscriber implements EnqueteSubscriber {

        private AtomicInteger contadorNovaEnquete;
        private AtomicInteger contadorVotaçãoFinalizada;

        public LocalEnqueteSubscriber(AtomicInteger contadorNovaEnquete, AtomicInteger contadorVotaçãoFinalizada) {
            this.contadorNovaEnquete = contadorNovaEnquete;
            this.contadorVotaçãoFinalizada = contadorVotaçãoFinalizada;
        }

        @Override
        public boolean ativo() throws RemoteException {
            return true;
        }

        @Override
        public void novaEnquete(Enquete enquete) throws RemoteException {
            contadorNovaEnquete.incrementAndGet();
        }

        @Override
        public void votacaoConcluida(Enquete enquete) throws RemoteException {
            contadorVotaçãoFinalizada.incrementAndGet();
        }

    }

    private EnqueteServiceImpl enqueteService;
    private KeyPair keyPair;

    @BeforeEach
    void createService() throws RemoteException, NoSuchAlgorithmException {
        enqueteService = new EnqueteServiceImpl();

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        SecureRandom secRan = new SecureRandom();
        kpg.initialize(512, secRan);
        keyPair = kpg.generateKeyPair();
    }

    @AfterEach
    void shutdownService() {
        enqueteService.close();
    }

    @Test
    @DisplayName("Criar")
    void criarEnequeteTest() throws RemoteException, UsuarioJaCadastradoException, ChavePublicaInvalidaException {
        AtomicInteger usuario1NotificadoNovaEnquete = new AtomicInteger(0);
        AtomicInteger usuario1NotificadoVotacaoConcluida = new AtomicInteger(0);
        enqueteService.registrar("usuario-1", keyPair.getPublic(),
                new LocalEnqueteSubscriber(usuario1NotificadoNovaEnquete, usuario1NotificadoVotacaoConcluida));

        AtomicInteger usuario2NotificadoNovaEnquete = new AtomicInteger(0);
        AtomicInteger usuario2NotificadoVotacaoConcluida = new AtomicInteger(0);
        enqueteService.registrar("usuario-2", keyPair.getPublic(),
                new LocalEnqueteSubscriber(usuario2NotificadoNovaEnquete, usuario2NotificadoVotacaoConcluida));

        enqueteService.criar("titulo", "local", "usuario-1", LocalDateTime.now(), Arrays.asList(LocalDateTime.now()));

        assertEquals(0, usuario1NotificadoNovaEnquete.get());
        assertEquals(0, usuario1NotificadoVotacaoConcluida.get());
        assertEquals(1, usuario2NotificadoNovaEnquete.get());
        assertEquals(0, usuario2NotificadoVotacaoConcluida.get());
    }

    @Test
    @DisplayName("Registrar")
    void registrarTest() throws RemoteException, UsuarioJaCadastradoException, ChavePublicaInvalidaException {
        AtomicInteger usuario1NotificadoNovaEnquete = new AtomicInteger(0);
        AtomicInteger usuario1NotificadoVotacaoConcluida = new AtomicInteger(0);
        enqueteService.registrar("usuario-1", keyPair.getPublic(),
                new LocalEnqueteSubscriber(usuario1NotificadoNovaEnquete, usuario1NotificadoVotacaoConcluida));
        assertThrows(UsuarioJaCadastradoException.class, () -> enqueteService.registrar("usuario-1", keyPair.getPublic(),
                new LocalEnqueteSubscriber(usuario1NotificadoNovaEnquete, usuario1NotificadoVotacaoConcluida)));
    }

    @Test
    @DisplayName("Listar")
    void listarTest() throws RemoteException, UsuarioJaCadastradoException, ChavePublicaInvalidaException {
        AtomicInteger usuario1NotificadoNovaEnquete = new AtomicInteger(0);
        AtomicInteger usuario1NotificadoVotacaoConcluida = new AtomicInteger(0);
        enqueteService.registrar("usuario-1", keyPair.getPublic(), new LocalEnqueteSubscriber(usuario1NotificadoNovaEnquete, usuario1NotificadoVotacaoConcluida));

        AtomicInteger usuario2NotificadoNovaEnquete = new AtomicInteger(0);
        AtomicInteger usuario2NotificadoVotacaoConcluida = new AtomicInteger(0);
        enqueteService.registrar("usuario-2", keyPair.getPublic(), new LocalEnqueteSubscriber(usuario2NotificadoNovaEnquete, usuario2NotificadoVotacaoConcluida));

        LocalDateTime now = LocalDateTime.now();
        Enquete enquete1 = enqueteService.criar("titulo-1", "local-1", "usuario-1", now, Arrays.asList(now));
        Enquete enquete2 = enqueteService.criar("titulo-2", "local-2", "usuario-2", now, Arrays.asList(now));
        List<Enquete> expected = Arrays.asList(new Enquete(enquete1.getId(), "titulo-1", "local-1", "usuario-1", now, Arrays.asList(now)),
                                               new Enquete(enquete2.getId(), "titulo-2", "local-2", "usuario-2", now, Arrays.asList(now)));
        assertEquals(expected, enqueteService.listar());
    }

    @Test
    @DisplayName("Votar")
    void votarTest() throws RemoteException, UsuarioJaCadastradoException, ChavePublicaInvalidaException {
        AtomicInteger usuario1NotificadoNovaEnquete = new AtomicInteger(0);
        AtomicInteger usuario1NotificadoVotacaoConcluida = new AtomicInteger(0);
        enqueteService.registrar("usuario-1", keyPair.getPublic(), new LocalEnqueteSubscriber(usuario1NotificadoNovaEnquete,usuario1NotificadoVotacaoConcluida));

        AtomicInteger usuario2NotificadoNovaEnquete = new AtomicInteger(0);
        AtomicInteger usuario2NotificadoVotacaoConcluida = new AtomicInteger(0);
        enqueteService.registrar("usuario-2", keyPair.getPublic(), new LocalEnqueteSubscriber(usuario2NotificadoNovaEnquete, usuario2NotificadoVotacaoConcluida));

        AtomicInteger usuario3NotificadoNovaEnquete = new AtomicInteger(0);
        AtomicInteger usuario3NotificadoVotacaoConcluida = new AtomicInteger(0);
        enqueteService.registrar("usuario-3", keyPair.getPublic(), new LocalEnqueteSubscriber(usuario3NotificadoNovaEnquete, usuario3NotificadoVotacaoConcluida));

        AtomicInteger usuario4NotificadoNovaEnquete = new AtomicInteger(0);
        AtomicInteger usuario4NotificadoVotacaoConcluida = new AtomicInteger(0);
        enqueteService.registrar("usuario-4", keyPair.getPublic(), new LocalEnqueteSubscriber(usuario4NotificadoNovaEnquete, usuario4NotificadoVotacaoConcluida));


        Enquete enquete = enqueteService.criar("titulo-1", "local-1", "usuario-1", LocalDateTime.now(), Arrays.asList(LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)));

        enqueteService.votar("usuario-2", enquete.getId(), Arrays.asList(1));
        enqueteService.votar("usuario-3", enquete.getId(), Arrays.asList(2));
        enqueteService.votar("usuario-4", enquete.getId(), Arrays.asList(2));
        assertEquals(1, usuario1NotificadoVotacaoConcluida.get());
        assertEquals(1, usuario2NotificadoVotacaoConcluida.get());
        assertEquals(1, usuario3NotificadoVotacaoConcluida.get());
        assertEquals(1, usuario4NotificadoVotacaoConcluida.get());
    }


    @Test
    @DisplayName("Não votadas")
    void naoVotadasTest() throws RemoteException, UsuarioJaCadastradoException, ChavePublicaInvalidaException {
        AtomicInteger usuario1NotificadoNovaEnquete = new AtomicInteger(0);
        AtomicInteger usuario1NotificadoVotacaoConcluida = new AtomicInteger(0);
        enqueteService.registrar("usuario-1", keyPair.getPublic(), new LocalEnqueteSubscriber(usuario1NotificadoNovaEnquete,usuario1NotificadoVotacaoConcluida));

        AtomicInteger usuario2NotificadoNovaEnquete = new AtomicInteger(0);
        AtomicInteger usuario2NotificadoVotacaoConcluida = new AtomicInteger(0);
        enqueteService.registrar("usuario-2", keyPair.getPublic(), new LocalEnqueteSubscriber(usuario2NotificadoNovaEnquete, usuario2NotificadoVotacaoConcluida));

        Enquete enquete1 = enqueteService.criar("titulo-1", "local-1", "usuario-1", LocalDateTime.now(), Arrays.asList(LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)));
        Enquete enquete2 = enqueteService.criar("titulo-2", "local-2", "usuario-1", LocalDateTime.now(), Arrays.asList(LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)));
        Enquete enquete3 = enqueteService.criar("titulo-3", "local-3", "usuario-1", LocalDateTime.now(), Arrays.asList(LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2))); 
      
        enqueteService.votar("usuario-2", enquete2.getId(), Arrays.asList(0));
        assertEquals(new HashSet<>(Arrays.asList(enquete1, enquete3)), enqueteService.enquetesNaoVotadas("usuario-2"));
    }
}

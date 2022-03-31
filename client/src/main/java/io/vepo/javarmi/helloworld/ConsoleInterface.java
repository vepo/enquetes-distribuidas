package io.vepo.javarmi.helloworld;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleInterface {

    private final ExecutorService threadPool;
    private final List<String> buffer;
    private final List<Runnable> opcoesFn;
    private final List<String> opcoes;
    private final AtomicBoolean running;

    public ConsoleInterface() {
        running = new AtomicBoolean(true);
        opcoesFn = new ArrayList<>();
        opcoes = new ArrayList<>();
        buffer = Collections.synchronizedList(new ArrayList<>());
        threadPool = Executors.newFixedThreadPool(1);
        threadPool.submit(() -> {
            try (Scanner scanner = new Scanner(System.in, "UTF-8")) {
                while (running.get()) {
                    buffer.add(scanner.nextLine().trim());
                }
            }
        });
    }

    public void exibirMenu() {
        System.out.println("Opções:");
        for (int i = 0; i < opcoes.size(); ++i) {
            System.out.println("\t(" + Integer.toString(i + 1) + ") " + opcoes.get(i));
        }
        System.out.print("Selecione uma opção: ");
    }

    public void adicionarOpcao(String label, Runnable fn) {
        this.opcoes.add(label);
        this.opcoesFn.add(fn);
    }

    public Optional<Runnable> selecao(int timeout) {
        LocalDateTime start = LocalDateTime.now();
        while (ChronoUnit.SECONDS.between(start, LocalDateTime.now()) < timeout) {
            if (!buffer.isEmpty()) {
                String choice = buffer.remove(0);
                return Optional.of(choice)
                        .map(opcao -> {
                            try {
                                int index = Integer.valueOf(opcao) - 1;
                                System.out.println("Opção selecionada: " + index);
                                if (index < this.opcoesFn.size()) {
                                    return this.opcoesFn.get(index);
                                } else {
                                    System.err.println("Opção inválida!");
                                    return null;
                                }
                            } catch (NumberFormatException nfe) {
                                System.err.println("Opção inválida!");
                                return null;
                            }
                        });
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return Optional.empty();
    }

    public void limpaBuffer() {
        buffer.clear();
    }

    public String proximaLinha(boolean aceitaVazio) {
        while (buffer.isEmpty() || (!aceitaVazio && buffer.get(0).isEmpty())) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (!aceitaVazio && buffer.size() > 0 && buffer.get(0).isEmpty()) {
                buffer.remove(0);
            }
        }
        return buffer.remove(0);
    }

}

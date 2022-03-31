# Enquetes via Java RMI

## Compilando e Executando

Esse projeto usa Maven + Java 8, para criar os executáveis, execute no diretório raiz.

```bash
./mvnw clean package
```

Os executaveis serão gerados `client/target/cliente.jar` e `server/target/servidor.jar`. Ambos são jar executáveis e possuem alguns parâmetros para execução.

```bash
$ java -jar client/target/cliente.jar --help
Usage: enquete-cli [-hiV] [--host=<host>] -n=<nomeUsuario>
m [-p=<port>]
                   --private-key=<privateKeyFile> --public-key=<publicKeyFile>
Cliente para acesso a enquetes.
  -h, --help                 Show this help message and exit.
      --host=<host>          Host do Servidor de enquetes
  -i, --iniciar              Iniciar base de dados com enquetes.
  -n, --nome=<nomeUsuario>   Nome do usuário do servidor de enquetes
  -p, --porta=<port>         Porta do servidor de enquetes
                               Default: 1098
      --private-key=<privateKeyFile>
                             Chave privada
      --public-key=<publicKeyFile>
                             Chave pública
  -V, --version              Print version information and exit.

```

```bash
$ java -jar server/target/servidor.jar  -h
Usage: enquete-server [-hV] [-p=<port>]
Servidor de enquetes.
  -h, --help           Show this help message and exit.
  -p, --porta=<port>   Porta do servidor de enquetes
                         Default: 1098
  -V, --version        Print version information and exit.
```

## Implementação

O projeto está dividido em 3 modulos. 

O modulo `common` que conterá as classes em comum entre cliente e servidor. Nesse módulo não há nenhuma implementação, apenas interfaces para comunicação.

O modulo `server` implementará as funcionalidades de registro de enquetes. Nessa implementação preliminar não haverá persistência de dados e nem interface com usuário.

O modulo `client` implementará uma interface CLI para cadastro de enquetes.

## Roteiro para apresentação

* [ ] Iniciar Servidor
* [ ] Iniciar Cliente 1 (nome de usuário "Cliente 1")
* [ ] Iniciar Cliente 2 (nome de usuário "Cliente 2") 
* [ ] Cadastrar Enquete no Cliente 1
* [ ] Cadastrar Enquete no Cliente 2
* [ ] Votar no Cliente 2
* [ ] Requerer resultados no Cliente 2
* [ ] Requerer resultados no Cliente 1
* [ ] Cadastrar enquete com data limite no passado
# Enquetes via Java RMI

## Compilando e Executando

Esse projeto usa Maven + Java 8, para criar os executáveis, execute no diretório raiz.

```bash
mvn clean package
```

Os executaveis serão gerados `client/target/cliente.jar` e `server/target/servidor.jar`. Ambos são jar executáveis e possuem alguns parâmetros para execução.

```bash
$ java -jar client/target/cliente.jar --help
Usage: enquete-cli [-hV] [--host=<host>] -n=<nome> [-p=<port>]
Cliente para acesso a enquetes.
  -h, --help           Show this help message and exit.
      --host=<host>    Host do Servidor de enquetes
  -n, --nome=<nome>    Nome do usuário do servidor de enquetes
  -p, --porta=<port>   Porta do servidor de enquetes
                         Default: 1098
  -V, --version        Print version information and exit.

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
* [ ] Cadastrar Enquete
* [ ] Iniciar Cliente 2 (nome de usuário "Cliente 2") 

## Requisitos

### Cadastro usuário (valor 0,3):

> Ao acessar o sistema pela primeira vez, cada cliente deve
> informar seu nome, chave pública e sua referência de objeto
> remoto. Nesse cadastro, o cliente automaticamente atuará
> como subscriber, registrando interesse em receber
> notificações do servidor quando uma nova enquete for
> cadastrada

### Cadastro de enquete (valor 0,7):

> Cliente deve informar: seu nome, título da enquete, local do
> evento, propostas de tempo (datas e horários), data limite para
> obter respostas (valor 0,3). Nesse cadastro, o cliente
> automaticamente atuará como subscriber, registrando
> interesse em receber notificações do servidor quando essa
> enquete for finalizada.

> Em cada cadastro de enquete, o servidor atuará como
> publisher e enviará uma notificação assíncrona aos clientes
> avisando sobre esse novo evento. Essa notificação se dará na
> forma de uma chamada de método do servidor para o cliente.
> Para isso, o servidor utilizará as referências de objeto remoto
> dos clientes que se cadastraram (valor 0,4).

### Cadastro de voto em uma enquete (valor 0,3):

> Ao receber uma notificação de uma nova enquete, o cliente
> deve votar nessa enquete. Para isso, ele deve informar: seu
> nome, título da enquete e as propostas de tempo das quais ele
> pode participar. Nesse cadastro, o cliente automaticamente
> atuará como subscriber, registrando interesse em receber
> notificações do servidor quando essa enquete for finalizada.

### Cada cliente tem um método para o recebimento de notificações assíncronas de eventos ocorridos no servidor (valor 0,4):

> O cliente que se cadastrou no sistema receberá uma notificação
> de evento do servidor, via chamada de método, quando uma
> nova enquete for cadastrada no sistema. Essa mensagem conterá
> informações sobre a enquete (valor 0,2).
>
> O cliente que participa (através do cadastro ou da votação) de
> uma enquete receberá uma notificação de evento do servidor, via
> chamada de método, quando uma enquete for encerrada (i.e.,
> quando todos responderam ou quando a data limite expirar). Essa
> mensagem conterá informações sobre o resultado da enquete
> (valor 0,2)
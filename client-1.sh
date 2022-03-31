#!/bin/bash

java -Dfile.encoding=UTF-8 -jar client/target/cliente.jar -p 5099 -n "Cliente 1" --iniciar --private-key ./chaves/cliente-1.key --public-key ./chaves/cliente-1.pub
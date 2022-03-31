#!/bin/bash

mkdir -p chaves
rm -rf chaves/*

openssl genrsa -out chaves/cliente-1 2048
openssl pkcs8 -topk8 -inform PEM -outform DER -in chaves/cliente-1 -out chaves/cliente-1.key -nocrypt
openssl rsa -in chaves/cliente-1 -pubout -outform DER -out chaves/cliente-1.pub

openssl genrsa -out chaves/cliente-2 2048
openssl pkcs8 -topk8 -inform PEM -outform DER -in chaves/cliente-2 -out chaves/cliente-2.key -nocrypt
openssl rsa -in chaves/cliente-2 -pubout -outform DER -out chaves/cliente-2.pub

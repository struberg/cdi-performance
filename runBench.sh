#!/bin/bash

echo "benching OWB-1.2.6"
mvn clean install

echo "benching OWB-1.2.0"
mvn clean install -Dowb.version=1.2.0

echo "benching OWB-1.1.6"
mvn clean install -Dowb.version=1.1.6

echo "benching OWB-1.1.8"
mvn clean install -Dowb.version=1.1.8

echo "benching OWB-1.5.0-SNAPSHOT (CDI-1.2)"
mvn clean install -POWB15 -Dowb.version=1.5.0-SNAPSHOT

echo "benching Weld-1.1.23.Final"
mvn clean install -PWeld -Dweld.version=1.1.23.Final

echo "benching Weld-2.2.6.Final"
mvn clean install -PWeld

echo "benching Weld-2.2.5.Final"
mvn clean install -PWeld -Dweld.version=2.2.5.Final

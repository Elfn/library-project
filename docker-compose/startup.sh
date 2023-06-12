#!/bin/bash
# Attendre que Kafka soit prêt
while nc -zv localhost 9092; do sleep 5; done
# Configurer le sujet
kafka-configs.sh --alter --bootstrap-server localhost:9092 --entity-type topics --entity-name library-events  --add-config min.insync.replicas=2
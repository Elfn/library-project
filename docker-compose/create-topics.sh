#!/bin/bash
set -e

# Lister vos sujets ici
TOPICS=("library-events" "library-events.RETRY" "library-events.DLT")


for TOPIC in "${TOPICS[@]}"
do
    kafka-topics --create --if-not-exists --bootstrap-server kafka:9092 --topic $TOPIC
done

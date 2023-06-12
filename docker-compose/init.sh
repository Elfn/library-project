#!/bin/bash

# Attente pour s'assurer que les répertoires sont montés
sleep 10

# Changer les permissions du répertoire /etc/kafka/secrets
chmod 777 /etc/kafka/secrets

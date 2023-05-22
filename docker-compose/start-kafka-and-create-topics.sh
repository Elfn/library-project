#!/bin/bash
/usr/bin/start-kafka.sh &
sleep 20
kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic mon_topic
wait

#!/bin/bash

# run with command line argument: 
# $1 <- keyspace, e.g., demo
# $2 <- location of shell, e.g., /home/ubuntu/cassandra/bin/cqlsh
echo "CREATE KEYSPACE $1 WITH REPLICATION = {'class' : 'SimpleStrategy' , 'replication_factor' : 3}; exit" | $2
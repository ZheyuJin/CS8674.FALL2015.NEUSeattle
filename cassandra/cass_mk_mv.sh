#!/bin/bash

# run with command line argument: 
# $1 <- host, e.g., 127.0.0.1
# $2 <- keyspace, e.g., demo
# $3 <- mv table name, e.g., mv
javac -cp ".:./jars/*" MostExpensiveStateCodeMV.java
java -cp ".:./jars/*" MostExpensiveStateCodeMV $1 $2 $3
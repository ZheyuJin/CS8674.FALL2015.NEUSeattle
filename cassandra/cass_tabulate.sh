#!/bin/bash

# run with command line argument: 
# $1 <- host, e.g., 127.0.0.1
# $2 <- keyspace, e.g., demo
# $3 <- file, e.g., 2012_medicare_thousand_data.csv
javac -cp ".:./jars/*" Transform.java
java -cp ".:./jars/*" Transform $1 $2 $3
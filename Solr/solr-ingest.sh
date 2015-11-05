#!/bin/bash

INPUT=$1
CORE=$2
INGEST="http://localhost:8983/solr/${CORE}/update/csv?stream.file=${INPUT}&stream.contentType=text/plain&header=true;charset=utf-8"
RELOAD="http://localhost:8983/solr/admin/cores?action=RELOAD"

if [ $# -eq 2 ]; then
    curl $INGEST
    curl $RELOAD
else
    printf "Invalid arguments.\nUsage: solr-ingest.sh 
fi

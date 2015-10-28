#!/bin/bash

# run with command line argument: 
# $1 <- keyspace, e.g., demo
# $2 <- location of shell, e.g., /home/ubuntu/cassandra/bin/cqlsh
echo "USE $1;
CREATE TABLE providers ( npi text PRIMARY KEY, nppes_provider_last_org_name text, nppes_provider_first_name text, nppes_provider_mi text, nppes_credentials text, nppes_provider_gender text, nppes_entity_code text, nppes_provider_street1 text, nppes_provider_street2 text, nppes_provider_city text, nppes_provider_zip int, nppes_provider_state text, nppes_provider_country text, provider_type text, medicare_participation_indicator text );
CREATE TABLE proceduresInfo ( hcpcs_code text PRIMARY KEY, hcpcs_description text, hcpcs_drug_indicator text );
CREATE TABLE proceduresStats ( id text PRIMARY KEY, npi text, place_of_service text, hcpcs_code text, year int, line_srvc_cnt float, bene_unique_cnt float, bene_day_srvc_cnt float, average_Medicare_allowed_amt float, stdev_Medicare_allowed_amt float, average_submitted_chrg_amt float, stdev_submitted_chrg_amt float, average_Medicare_payment_amt float, stdev_Medicare_payment_amt float );
exit" | $2
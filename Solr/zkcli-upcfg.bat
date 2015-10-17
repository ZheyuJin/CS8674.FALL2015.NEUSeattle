set ZKCLI_HOME=%solr_home%\server\scripts\cloud-scripts
call %solr_home%\server\scripts\cloud-scripts\zkcli -zkhost localhost:9983 -cmd upconfig -confdir curcfg -confname csvtest

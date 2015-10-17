set ZKCLI_HOME=%solr_home%\server\scripts\cloud-scripts
if not exist curcfg\. mkdir curcfg
call %solr_home%\server\scripts\cloud-scripts\zkcli -zkhost localhost:9983 -cmd downconfig -confdir curcfg -confname csvtest

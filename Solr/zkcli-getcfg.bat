set ZKCLI_HOME=%solr_home%\server\scripts\cloud-scripts
call %zkcli_home%\zkcli -zkhost localhost:9983 -cmd list >tmp.txt

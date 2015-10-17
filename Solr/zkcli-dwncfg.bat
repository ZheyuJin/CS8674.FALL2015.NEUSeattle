if .%ZKCLI_HOME%. == .. set ZKCLI_HOME=%solr_home%\server\scripts\cloud-scripts
set collection=%1
if .%1. == .. set collection=csvtest
set cfgdir=%2
if .%2. == .. set cfgdir=curcfg

@echo Downloading config for collection %collection% to directory %cfgdir%
if not exist %cfgdir%\. mkdir %cfgdir%
call %solr_home%\server\scripts\cloud-scripts\zkcli -zkhost localhost:9983 -cmd downconfig -confdir %cfgdir% -confname %collection%

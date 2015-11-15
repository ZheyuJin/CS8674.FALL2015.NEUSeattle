if .%ZKCLI_HOME%. == .. set ZKCLI_HOME=%solr_home%\server\scripts\cloud-scripts
set collection=%1
if .%1. == .. set collection=csvtest
set cfgdir=%2
if .%2. == .. set cfgdir=curcfg
@set zkhost=%3
@if .%3. == .. set zkhost=localhost:9983

@echo Downloading config for collection %collection% to directory %cfgdir%
if not exist %cfgdir%\. mkdir %cfgdir%
call %solr_home%\server\scripts\cloud-scripts\zkcli -zkhost %zkhost% -cmd downconfig -confdir %cfgdir% -confname %collection%

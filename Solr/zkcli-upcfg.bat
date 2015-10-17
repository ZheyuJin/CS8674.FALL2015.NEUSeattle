@if .%ZKCLI_HOME%. == .. set ZKCLI_HOME=%solr_home%\server\scripts\cloud-scripts
@set collection=%1
@if .%1. == .. set collection=csvtest
@set cfgdir=%2
@if .%2. == .. set cfgdir=curcfg

@echo.
@echo Uploading config for collection %collection% from directory %cfgdir%
@echo.

@set fileerr=1
@if exist %cfgdir%\schema.xml if exist %cfgdir%\solrconfig.xml set fileerr=

@if .%fileerr%. == .. call %solr_home%\server\scripts\cloud-scripts\zkcli -zkhost localhost:9983 -cmd upconfig -confdir curcfg -confname csvtest
@if .%fileerr%. == .1. @echo Could not find %cfgdir%\schema.xml or %cfgdir%\solrconfig.xml
@echo.

@rem to do - add a small wait and then delete all (first url) then reload (second url - maybe not necessary after a delete?)
@rem http://localhost:8983/solr/csvtest/update?stream.body=%3Cdelete%3E%3Cquery%3E*:*%3C/query%3E%3C/delete%3E&commit=true
@rem http://localhost:8983/solr/admin/collections?action=RELOAD&name=csvtest&reindex=true&deleteAll=true

set ZKCLI_HOME=%solr_home%\server\scripts\cloud-scripts
if .%ZKCLI_HOME%. == .. set ZKCLI_HOME=%solr_home%\server\scripts\cloud-scripts
@set zkhost=%1
@if .%1. == .. set zkhost=localhost:9983

call %zkcli_home%\zkcli -zkhost %zkhost% -cmd list >tmp.txt

@echo off

cd /d %TIMELINERDIR%
start JRE\bin\javaw -classpath "audio-timeliner.jar" -Xmx196M timeliner.Timeliner %1
@echo off
setlocal

set RUNDIR=%~dp0
set LIBPATH=%LIBPATH%;%RUNDIR%..\*;%RUNDIR%..\lib\*
rem tnt4j property override
IF ["%TNT4J_PROPERTIES%"] EQU [""] set TNT4J_PROPERTIES=%RUNDIR%..\config\tnt4j.properties
set TNT4JOPTS=-Dtnt4j.config="%TNT4J_PROPERTIES%"
rem log4j property override
IF ["%LOG4J_PROPERTIES%"] EQU [""] set LOG4J_PROPERTIES=%RUNDIR%..\config\log4j.properties
set LOG4JOPTS=-Dlog4j.configuration="file:%LOG4J_PROPERTIES%"
REM set LOGBACKOPTS=-Dlogback.configurationFile="file:%RUNDIR%..\config\logback.xml"
set STREAMSOPTS=%STREAMSOPTS% %LOG4JOPTS% %TNT4JOPTS% -Dfile.encoding=UTF-8

IF ["%MAINCLASS%"] EQU [""] (
  set MAINCLASS=com.jkoolcloud.tnt4j.streams.StreamsAgent
)

IF ["%JAVA_HOME%"] EQU [""] (
  echo "JAVA_HOME" env. variable is not defined!..
) else (
  echo Will use java from: "%JAVA_HOME%"
)

@echo on
"%JAVA_HOME%\bin\java" %STREAMSOPTS% -classpath "%LIBPATH%" %MAINCLASS% -f:tnt-data-source.xml
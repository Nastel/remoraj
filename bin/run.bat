@echo off
setlocal enableDelayedExpansion
:START
IF EXIST token (
	echo "Reading token from file"
	set /P STREAMING_TOKEN=<token
) ELSE (
	echo "Token not found"
	set /P STREAMING_TOKEN=Enter Your Streaming token:
	echo !STREAMING_TOKEN!> token
	goto START
)
echo "TOKEN: %STREAMING_TOKEN%"
set STREAMSOPTS=%STREAMSOPTS% -DSTREAMING_TOKEN=%STREAMING_TOKEN%
cd tnt4j-streams\remora-streamer
start run.bat
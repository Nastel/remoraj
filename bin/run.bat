@echo off
:START
IF EXIST token (
	echo "Reading token from file"
	set /P STREAMING_TOKEN=<token
	echo "TOKEN: %STREAMING_TOKEN%"
) ELSE (
	echo "Token not found"
	set /P STREAMING_TOKEN=Enter Your Streaming token:
	@echo on
	echo %STREAMING_TOKEN% > token
	@echo off
	goto START
)
rem cd tnt4j-streams\remora-streamer
rem start run.bat
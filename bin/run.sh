#! /bin/bash
if [ -e token ] 
then
	echo "Reading token from file"
	export STREAMING_TOKEN=<token
	echo "TOKEN: %STREAMING_TOKEN%"
else
	echo "Token not found"
	read -p "Enter Your Streaming token:" STREAMING_TOKEN
	echo %STREAMING_TOKEN% > token
fi	
export STREAMSOPTS=%STREAMSOPTS% -DSTREAMING_TOKEN=%STREAMING_TOKEN%
cd tnt4j-streams/remora-streamer
./run.sh
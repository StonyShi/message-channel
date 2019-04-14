#!/bin/bash
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`


PIDS=`ps aux | grep java | grep "$DEPLOY_DIR" |awk '{print $2}'`
echo "Find PID: $PIDS"
if [ -z "$PIDS" ]; then
    echo "ERROR: The $DEPLOY_DIR does not started!"
    exit 1
fi

if [ "$1" == "dump" ]; then
    echo "EXEC DUMP $BIN_DIR/dump.sh  start"
    $BIN_DIR/dump.sh
fi

echo -e "Stopping the $DEPLOY_DIR ...\c"
for PID in $PIDS ; do
    kill -14 $PID > /dev/null 2>&1
done

PIDS=`ps aux | grep java | grep "$DEPLOY_DIR" |awk '{print $2}'`
for PID in $PIDS ; do
    kill -9 $PID > /dev/null 2>&1
done

sleep 1
PIDS=`ps aux | grep java | grep "$DEPLOY_DIR" |awk '{print $2}'`
echo "OK!"
echo "PID: $PIDS"
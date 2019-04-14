#!/bin/bash
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`


PIDS=`ps aux | grep java | grep "$DEPLOY_DIR" |awk '{print $2}'`
if [ -n "$PIDS" ]; then
    echo "ERROR: The $DEPLOY_DIR already started!"
    echo "PID: $PIDS"
    exit 1
fi

echo "The $DEPLOY_DIR Found PIDS : $PIDS"

if [ -f "${BIN_DIR}/env.sh" ]; then
  . "${BIN_DIR}/env.sh"
fi

LOGS_DIR=$DEPLOY_DIR/logs
if [ ! -d $LOGS_DIR ]; then
    mkdir $LOGS_DIR
fi

STDOUT_FILE=$LOGS_DIR/stdout.log

LIB_DIR=$DEPLOY_DIR/lib
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`

L_VELCP=.
for i in $LIB_DIR/*.jar
do
    L_VELCP=$L_VELCP:"$i"
done


CONFIG_DIR=$DEPLOY_DIR/config

HOST_NAME=$(uname -n)

if [ "x$LOG_CONFIG_FILE" = "x" ]; then
  export LOG_CONFIG_FILE="$CONFIG_DIR/log4j2.xml"
fi

if [ "x$APP_CONFIG_FILE" = "x" ]; then
  export APP_CONFIG_FILE="$CONFIG_DIR/application.yml"
fi

if [ "x$LOG4J_CONFIGURATION_FILE" = "x" ]; then
   export LOG4J_CONFIGURATION_FILE=$LOG_CONFIG_FILE
fi


if [ "x$LOG_OPTS" = "x" ]; then
  export LOG_OPTS="-Dlog4j.configurationFile=file:$LOG_CONFIG_FILE \
    -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector \
    -Dlogs.dir=$LOGS_DIR \
    -Dlog.config.file=$LOG_CONFIG_FILE "
fi


if [ "x$SERVER_NAME" = "x" ]; then
  export SERVER_NAME="-Dserver.name=$HOST_NAME"
fi

if [ "x$JAVA_HOME" = "x" ]; then
  export JAVA_HOME="/yongche/jdk8"
fi

JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "

## LOG_OPTS=" -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -Dlogs.dir=$LOGS_DIR -Dlog.config.file=$LOG_CONFIG_FILE "
HOME_OPTS=" -Dcatalina.home=$DEPLOY_DIR $SERVER_NAME -Dapp.config.file=$APP_CONFIG_FILE "

JAVA_MEM_OPTS=" "
BITS=`java -version 2>&1 | grep -i 64-bit`
if [ -n "$BITS" ]; then
    if [ "x$APP_HEAP" = "x" ]; then
        JAVA_MEM_OPTS=" -server -Xmx16g -Xms16g -XX:+UseG1GC "
    else
        JAVA_MEM_OPTS=" -server -Xmx$APP_HEAP -Xms$APP_HEAP -XX:+UseG1GC "
    fi
    ##JAVA_MEM_OPTS=" -server -Xmx16g -Xms16g -XX:+UseG1GC "
else
    JAVA_MEM_OPTS=" -server -Xmx8g -Xms8g -XX:+UseG1GC "
fi

echo -e "Starting the $DEPLOY_DIR ...\c"

nohup $JAVA_HOME/bin/java $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS $HOME_OPTS $LOG_OPTS -classpath $CONF_DIR:$LIB_JARS MainServer $* > $STDOUT_FILE 2>&1 &


echo "OK!"
sleep 1
PIDS=`ps aux | grep java | grep "$DEPLOY_DIR" | awk '{print $2}'` 
echo "PID: $PIDS"
echo "STDOUT: $STDOUT_FILE"
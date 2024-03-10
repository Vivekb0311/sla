#!/bin/sh

NAME=sla-service
APP_DIR=.
LOG_DIR=${APP_DIR}/logs
JAR=${APP_DIR}/${NAME}.jar

if [  -z  $XMX ] || [  -z $XMS ]; then
XMS=512m
XMX=1024m
fi

#CMD="java -cp $JAR:lib/*:. -Xms$XMS -Xmx$XMX  -XX:TieredStopAtLevel=1 -noverify -Xverify:none -XX:+UseParallelGC -XX:MaxMetaspaceSize=1024m -Dspring.config.location=file:./application.properties com.bootnext.Application"

CMD="java -jar -Xms$XMS -Xmx$XMX  -XX:TieredStopAtLevel=1 -noverify -Xverify:none -XX:+UseParallelGC -XX:MaxMetaspaceSize=1024m -Dspring.config.location=file:./application.properties $JAR"

LOG_FILE="$LOG_DIR/$NAME.log"
STDERR_LOG="$LOG_DIR/$NAME.err"
PID_FILE="$LOG_DIR/$NAME.pid"

#make the log directory if it doesn't exist
if [ ! -d "$LOG_DIR" ] ; then
	mkdir -p $LOG_DIR
	chmod 777 -R $LOG_DIR
fi

isRunning() {
	[ -f "$PID_FILE" ] && ps `cat $PID_FILE` > /dev/null 2>&1
}



case $1 in
	start)
		if isRunning; then
			echo "Already started"
		else
			echo "Starting $NAME"
			$CMD > "$LOG_FILE" 2> "$STDERR_LOG" & echo $! > "$PID_FILE"
			tail -f "$LOG_FILE"
			if ! isRunning; then
				echo "Unable to start, see $LOG_FILE and $stderr_log"
				exit 1
			fi
		fi
	;;
	stop)
		if isRunning; then
			echo "Stopping $NAME"
			kill `cat $PID_FILE`
			rm "$PID_FILE"
		else
			echo "Not running"
		fi
	;;
	restart)
		$0 stop
		$0 start
	;;
	status)
		if isRunning; then
			echo "Running"
		else
			echo "Not running"
		fi
	;;
	*)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
    ;;
esac

exit 0

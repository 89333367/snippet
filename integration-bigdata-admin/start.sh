#!/bin/sh

JAR_NAME=integration-bigdata-admin-1.0.0.jar
SERVER_PORT=10000

echo "检查${JAR_NAME}是否启动"
while [ 1=1 ]
do
  PID=`ps -ef |grep ${JAR_NAME} |egrep -v 'grep|tail' |awk '{print $2}'`
  if [ "${PID}" = "" ]
  then
    if [ "${KILL_PID}" != "" ]
    then
      echo "${JAR_NAME}已关闭"
    else
      echo "${JAR_NAME}未启动"
    fi
    break
  else
    if [ "${KILL_PID}" != "${PID}" ]
    then
      echo "${JAR_NAME}已启动，PID：${PID}"
      echo "执行kill操作"
      kill ${PID}
      KILL_PID=${PID}
    else
      echo "等待${JAR_NAME}关闭"
    fi
  fi
  sleep 1
done

echo "启动${JAR_NAME}，使用端口：${SERVER_PORT}"

java -jar ${JAR_NAME} --server.port=${SERVER_PORT} >${JAR_NAME}.log &

exit 0

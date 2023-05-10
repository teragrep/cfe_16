#!/usr/bin/env bash

if [ -z ${XMS} ]; then
  XMS=256m
fi
if [ -z ${XMX} ]; then
  XMX=512m
fi
echo "Xms=${XMS}"
echo "Xmx=${XMX}"
java -jar /opt/Fail-Safe/cfe-16/share/cfe-16.jar -Xms${XMS} -Xmx${XMX} -Dspring.config.location="file://${CONFIG_PATH}"

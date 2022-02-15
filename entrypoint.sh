#!/usr/bin/env bash

if [ -z ${XMS} ]; then
  XMS=256m
fi
if [ -z ${XMX} ]; then
  XMX=512m
fi
echo "Xms=${XMS}"
echo "Xmx=${XMX}"
java -jar /opt/teragrep/cfe_16/lib/cfe_16.jar -Xms${XMS} -Xmx${XMX} -Dspring.config.location="file://${CONFIG_PATH}"

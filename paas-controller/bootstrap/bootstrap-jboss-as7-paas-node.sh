#!/bin/sh

#//TODO move all configuration to jboss-configurator.jar

jboss_path=/opt/jboss-as/jboss-as-7

: ${LISTEN_ADDRESS:=$(ifconfig eth0 | grep "inet addr" | cut -f2 -d: | cut -d' ' -f1)}

sed -i "s/name=\"master\"/name=\"${LISTEN_ADDRESS}\"/g" $jboss_path/domain/configuration/host.xml
sed -i "s/127.0.0.1/${LISTEN_ADDRESS}/g" $jboss_path/domain/configuration/host.xml

jboss_configurator_args="server ${jboss_path}/domain/configuration/host.xml ${jboss_path}/bin/domain.sh"
java -jar $jboss_path/modules/org/jboss/as/paas/controller/main/jboss-as-paas-controller.jar $jboss_configurator_args

#!/bin/sh

#//TODO move all configuration to jboss-configurator.jar

jboss_path=/opt/jboss-as/jboss-as-7
jboss_bootstrap_path=/tmp/jboss-bootstrap

rm -rf $jboss_bootstrap_path
mkdir -p $jboss_bootstrap_path
unzip jboss-as-7-bootstrap-fiels.zip -d $jboss_bootstrap_path/

#rm -rf $jboss_path
#unzip jboss-as-7.1.0.Alpha1-SNAPSHOT.zip -d $jboss_path/

cp -rf $jboss_bootstrap_path/* $jboss_path/

: ${LISTEN_ADDRESS:=$(ifconfig eth0 | grep "inet addr" | cut -f2 -d: | cut -d' ' -f1)}

sed -i "s/name=\"master\"/name=\"${LISTEN_ADDRESS}\"/g" $jboss_path/domain/configuration/host.xml
sed -i "s/127.0.0.1/${LISTEN_ADDRESS}/g" $jboss_path/domain/configuration/host.xml

#chmod +x $jboss_path/bin/domain.sh
#chmod +x $jboss_path/bin/domain-debug.sh
#chmod +x $jboss_path/bin/jboss-admin.sh

#jboss_configurator_args="server ${jboss_path}/domain/configuration/host.xml ${jboss_path}/bin/domain.sh"
#java -jar {$jboss_bootstrap_path}/modules/org/jboss/as/paas/controller/main/jboss-as-paas-controller.jar $jboss_configurator_args

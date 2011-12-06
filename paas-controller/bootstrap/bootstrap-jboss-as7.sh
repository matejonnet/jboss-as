#!/bin/sh

#rm -rf /root/jboss-as-7-bootstrap-fiels
#unzip /root/jboss-as-7-bootstrap-fiels.zip

jboss_path=/opt/jboss/jboss-as-7.1.0.Alpha1-SNAPSHOT

#rm -rf $jboss_path

unzip jboss-as-7.1.0.Alpha1-SNAPSHOT.zip -d $jboss_path/

cp -rf /root/jboss-as-7-bootstrap-fiels/* $jboss_path/

: ${LISTEN_ADDRESS:=$(ifconfig eth0 | grep "inet addr" | cut -f2 -d: | cut -d' ' -f1)}

sed -i "s/name=\"master\"/name=\"${LISTEN_ADDRESS}\"/g" $jboss_path/domain/configuration/host.xml
sed -i "s/127.0.0.1/${LISTEN_ADDRESS}/g" $jboss_path/domain/configuration/host.xml

chmod +x $jboss_path/bin/domain.sh
chmod +x $jboss_path/bin/domain-debug.sh
chmod +x $jboss_path/bin/jboss-admin.sh

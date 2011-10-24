#!/bin/sh

#rm -rf /root/jboss-as-7-bootstrap-fiels
#unzip /root/jboss-as-7-bootstrap-fiels.zip

rm -rf /opt/jboss/jboss-as-7.1.0.Alpha1-SNAPSHOT

unzip jboss-as-7.1.0.Alpha1-SNAPSHOT.zip -d /opt/jboss/jboss-as-7.1.0.Alpha1-SNAPSHOT/

cp -rf /root/jboss-as-7-bootstrap-fiels/* /opt/jboss/jboss-as-7.1.0.Alpha1-SNAPSHOT/

: ${LISTEN_ADDRESS:=$(ifconfig eth0 | grep "inet addr" | cut -f2 -d: | cut -d' ' -f1)}

sed -i "s/name=\"master\"/name=\"${LISTEN_ADDRESS}\"/g" /opt/jboss/jboss-as-7.1.0.Alpha1-SNAPSHOT/domain/configuration/host.xml
sed -i "s/127.0.0.1/${LISTEN_ADDRESS}/g" /opt/jboss/jboss-as-7.1.0.Alpha1-SNAPSHOT/domain/configuration/host.xml

chmod +x /opt/jboss/jboss-as-7.1.0.Alpha1-SNAPSHOT/bin/domain.sh
chmod +x /opt/jboss/jboss-as-7.1.0.Alpha1-SNAPSHOT/bin/domain-debug.sh
chmod +x /opt/jboss/jboss-as-7.1.0.Alpha1-SNAPSHOT/bin/jboss-admin.sh

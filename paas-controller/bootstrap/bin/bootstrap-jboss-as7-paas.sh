#!/bin/sh
#
# add to /etc/rc.local
# /opt/jboss-as/jboss-as-7/bin/bootstrap-jboss-as7-paas.sh
# chmod +x /opt/jboss-as/jboss-as-7/bin/*.sh
#


echo "bootstraping jboss AS 7 paas"
echo "booting ... " > /var/log/jboss-paas-config.log
date >> /var/log/jboss-paas-config.log

jboss_path=/opt/jboss-as/jboss-as-7
#jboss_bootstrap_path=/tmp/jboss-bootstrap

#rm -rf $jboss_bootstrap_path/jboss-as-7-bootstrap-fiels
#unzip $jboss_bootstrap_path/jboss-as-7-bootstrap-fiels.zip

#rm -rf $jboss_path
#unzip jboss-as-7.1.0.Alpha1-SNAPSHOT.zip -d $jboss_path/

#cp -rf $jboss_bootstrap_path/* $jboss_path/

#wait for network
COUNT=0
while true; do
    ifconfig eth0 | grep "inet addr" | cut -f2 -d: | cut -d' ' -f1 > /tmp/local.ip
    if (test -s /tmp/local.ip); then
        break
    fi
    echo "Waiting network to get ip ...going to sleep for 1"
    sleep 1
    COUNT=$[ $COUNT + 1 ]
    if [ $COUNT -gt 30 ]; then
        echo "ERROR: Time exceeded waiting for ip address"
        break
    fi
done

LISTEN_ADDRESS=`cat /tmp/local.ip`

sed -i "s/name=\"master\"/name=\"${LISTEN_ADDRESS}\"/g" $jboss_path/domain/configuration/host.xml

jboss_configurator_args="server $jboss_path/domain/configuration/host.xml $jboss_path/bin/startjboss.sh"
/opt/java/jre1.6/bin/java -classpath $jboss_path/modules/org/jboss/as/paas/controller/main/jboss-as-paas-controller.jar org.jboss.as.paas.configurator.Main $jboss_configurator_args 2>&1 1>/var/log/jboss-paas-config.log &

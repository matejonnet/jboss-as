#!/bin/sh
#define autostart of this script
#//root@ubuntu:~# ln -s /opt/jboss-as/jboss-as-7/bin/bootstrap-jboss-as7-paas.sh /etc/rc3.d/S75paas
#//root@ubuntu:~# ln -s /opt/jboss-as/jboss-as-7/bin/bootstrap-jboss-as7-paas.sh /etc/rc2.d/S75paas
#//root@ubuntu:~# ln -s /opt/jboss-as/jboss-as-7/bin/bootstrap-jboss-as7-paas.sh /etc/rc1.d/S75paas
#
# add to /etc/rc.local before exit 0
# /opt/jboss-as/jboss-as-7/bin/bootstrap-jboss-as7-paas.sh

#root@ubuntu:~# chmod +x /opt/jboss-as/jboss-as-7/bin/bootstrap-jboss-as7-paas.sh


#//TODO move all configuration to jboss-configurator.jar ?

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

#: ${LISTEN_ADDRESS:=$(ifconfig eth0 | grep "inet addr" | cut -f2 -d: | cut -d' ' -f1)}
LISTEN_ADDRESS=`cat /tmp/local.ip`

sed -i "s/name=\"master\"/name=\"${LISTEN_ADDRESS}\"/g" $jboss_path/domain/configuration/host.xml
#sed -i "s/127.0.0.1/${LISTEN_ADDRESS}/g" $jboss_path/domain/configuration/host.xml

#chmod +x $jboss_path/bin/domain.sh
#chmod +x $jboss_path/bin/domain-debug.sh
#chmod +x $jboss_path/bin/jboss-admin.sh

jboss_configurator_args="server $jboss_path/domain/configuration/host.xml $jboss_path/bin/startjboss.sh"
#/opt/java/jre1.6/bin/java -Daddress.local.ip=$LISTEN_ADDRESS -classpath $jboss_path/modules/org/jboss/as/paas/controller/main/jboss-configurator.jar org.alterjoc.jbossconfigurator.Main $jboss_configurator_args 2>&1 1>/var/log/jboss-paas-config.log &
/opt/java/jre1.6/bin/java -classpath $jboss_path/modules/org/jboss/as/paas/controller/main/jboss-configurator.jar org.alterjoc.jbossconfigurator.Main $jboss_configurator_args 2>&1 1>/var/log/jboss-paas-config.log &


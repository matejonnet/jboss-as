Instalation

You need to prepare IaaS image that contains JBoss AS with orcinus extension (paas-controller)

A. Install JBoss on IaaS instance
1. extract jboss to /opt/jboss-as/jboss-as-7/ (JBOSS_HOME)
2. copy content of bootstrap folder to JBOSS_HOME
3. override jboss-as-paas-controller.jar (modules/org/jboss/as/paas/controller/main/) with source code built
4. create all scripts in bin folder executable
      chmod +x $JBOSS_HOME/bin/*.sh
5. configure $JBOSS_HOME/bin/bootstrap-jboss-as7-paas.sh to execute after server boots (add to /etc/rc.local)

B. Create IaaS image from IaaS instance (steps I used on eucalyptus). If running on static servers (non IaaS) skip this step.
   @instance
     rm /etc/udev/rules.d/70-persistent-net.rules
     restore default host.xml ( name & DC) if you were running jboss as
     vi /opt/jboss-as/jboss-as-7/domain/configuration/host.xml

     mkdir /mnt/remotefs
     sshfs root@<clc-host>:/root/bundles /mnt/remotefs/
     cd /mnt/remotefs
     . /mnt/remotefs/euca/eucarc

     euca-bundle-vol -c ${EC2_CERT} -k ${EC2_PRIVATE_KEY} -u ${EC2_USER_ID} --ec2cert ${EUCALYPTUS_CERT} --no-inherit --kernel eki-48211657 --ramdisk eri-8F621722 -d /mnt/remotefs -r i386 -p ubuntu.9-04.i386-java-v15 -s 1024 -e /mnt,/root/.ssh,/root/.bash_history

   @clc
     . .euca/eucarc
     euca-upload-bundle -b ubuntu-image-bucket -m /root/bundles/ubuntu.9-04.i386-java-v15.manifest.xml
     euca-register ubuntu-image-bucket/ubuntu.9-04.i386-java-v15.manifest.xml

After IaaS image is registered you can boot an instance of it and configure paas.xml ($JBOSS_HOME/domain/configuration/)

Sample paas.xml config:
    <profiles>
        <profile name="paas-controller">
            <subsystem xmlns="urn:jboss:domain:paas-controller:1.0">
                <iaas-providers>
                    <iaas-provider provider="euca-provider" driver="jcloud-eucalyptus" url="http://172.16.254.140:8773/services/Eucalyptus" username="<euca-key>" password="<euca-pass>" image-id="Eucalyptus/<ami eg. emi-01CD1578>"/>
                    <iaas-provider provider="myvm" driver="static" url="" username="" password="" image-id=""/>
                </iaas-providers>
                <instances>
                    <instance id="01-static" provider="myvm" ip="172.16.254.128"/>
                    <instance id="02-static" provider="myvm" ip="172.16.254.129"/>
                </instances>
            </subsystem>
        </profile>

Operations:
  /profile=paas-controller/subsystem=paas-controller:deploy(path=/root/hello-servlet-noEJBnoDist.war)
  /profile=paas-controller/subsystem=paas-controller:scale-up(name="hello-servlet-noEJBnoDist.war", provider=euca-provider)
  /profile=paas-controller/subsystem=paas-controller:scale-down(name="hello-servlet-noEJBnoDist.war")
  /profile=paas-controller/subsystem=paas-controller:status()

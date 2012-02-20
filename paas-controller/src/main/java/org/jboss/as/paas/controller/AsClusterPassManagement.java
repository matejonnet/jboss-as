/**
 *
 */
package org.jboss.as.paas.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.jboss.as.paas.util.Util;
import org.jboss.logging.Logger;
import org.jboss.sasl.util.UsernamePasswordHashUtil;
import org.jboss.security.javaee.exceptions.MissingArgumentsException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @see org.jboss.as.domain.management.security.AddPropertiesUser.AddUser;
 */
public class AsClusterPassManagement {

    private static final Logger log = Logger.getLogger(AsClusterPassManagement.class);

    private static final String MANAGEMENT_REALM = "ManagementRealm";

    private char[] secretPass;

    public AsClusterPassManagement() {
        String pass = System.getProperty("serverIdentities.secret");
        if (pass == null || "".equals(pass)) {
            throw new MissingArgumentsException("Missing system property 'serverIdentities.secret'.");
        }
        this.secretPass = pass.toCharArray();
    }

    public void addRemoteServer(String remoteHostName) {
        String entry;
        try {
            String hash = new UsernamePasswordHashUtil().generateHashedHexURP(remoteHostName, MANAGEMENT_REALM, secretPass);
            entry = remoteHostName + "=" + hash;
        } catch (NoSuchAlgorithmException e) {
            log.error("Cannot hash user password.", e);
            return;
        }

        File propFile = getPropertiesFile();
        try {
            append(entry, propFile);
            log.debugf("Added user '%s' to file '%s'\n", remoteHostName, propFile.getCanonicalPath());
        } catch (IOException e) {
            log.error("Unable to add user to " + propFile.getAbsolutePath() + " due to error " + e.getMessage());
        }
    }

    public void removeRemoteServer(String remoteHostIp) {
        File propFile = getPropertiesFile();
        try {
            removePassFromFile(remoteHostIp, propFile);
            log.debugf("User '%s' removed from file '%s'\n", remoteHostIp, propFile.getCanonicalPath());
        } catch (IOException e) {
            log.error("Unable to remove user from " + propFile.getAbsolutePath() + " due to error " + e.getMessage());
        }
    }

    /**
     * @return
     */
    private File getPropertiesFile() {
        String jbossHome = System.getenv("JBOSS_HOME");
        File domainProps = new File(jbossHome + "/domain/configuration/mgmt-users.properties");
        return domainProps;
    }

    private boolean removePassFromFile(final String user, final File file) throws IOException {
        File tempFile = new File(file.getPath() + "tmp");

        FileReader fr = null;
        BufferedReader reader = null;
        FileWriter fw = null;
        BufferedWriter writer = null;

        try {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            fw = new FileWriter(tempFile);
            writer = new BufferedWriter(fw);

            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if (trimmedLine.startsWith(user))
                    continue;
                writer.write(currentLine);
                writer.newLine();
            }
        } finally {
            Util.safeClose(reader);
            Util.safeClose(writer);
            Util.safeClose(fr);
            Util.safeClose(fw);
        }

        return tempFile.renameTo(file);
    }

    private void append(final String entry, final File toFile) throws IOException {
        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            fw = new FileWriter(toFile, true);
            bw = new BufferedWriter(fw);

            bw.append(entry);
            bw.newLine();
        } finally {
            Util.safeClose(bw);
            Util.safeClose(fw);
        }
    }

}

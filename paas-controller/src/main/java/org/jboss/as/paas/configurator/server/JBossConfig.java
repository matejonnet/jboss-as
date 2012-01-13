/**
 *
 */
package org.jboss.as.paas.configurator.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class JBossConfig {

    private String pathToHostXml;

    /**
     * @param pathToHostXml
     */
    public JBossConfig(String pathToHostXml) {
        super();
        this.pathToHostXml = pathToHostXml;
    }

    /**
     * @param inputLine
     * @throws IOException
     */
    public void setDomainControllerIp(String ip) throws IOException {
        File hostConfig = new File(pathToHostXml);
        File hostConfigTmp = new File(pathToHostXml + ".tmp");

        FileInputStream is = new FileInputStream(hostConfig);
        Scanner input = new Scanner(is);

        FileOutputStream os = new FileOutputStream(hostConfigTmp);
        OutputStreamWriter output = new OutputStreamWriter(os);

        try {
            replaceConfig(ip, input, output);
        } finally {
            output.close();
            os.close();
            input.close();
            is.close();
        }

        hostConfig.delete();
        hostConfigTmp.renameTo(hostConfig);
    }

    // TODO move here configurations from boostrap.sh
    private void replaceConfig(String ip, Scanner input, OutputStreamWriter output) throws IOException {
        StringBuffer result = new StringBuffer();

        String NL = System.getProperty("line.separator");

        String line;
        while (input.hasNextLine()) {
            line = input.nextLine();
            if (line.indexOf("<local/>") > -1) {
                String replace = line.replace("<local/>", "<remote host=\"" + ip + "\" port=\"9999\" security-realm=\"ManagementRealm\"/>");
                result.append(replace);
            } else {
                result.append(line);
            }
            result.append(NL);
        }
        output.write(result.toString());
    }

}

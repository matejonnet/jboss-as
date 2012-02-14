/**
 *
 */
package org.jboss.as.paas.util;

import java.io.Closeable;
import java.net.Socket;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Util {

    private static final Logger log = Logger.getLogger(Util.class);

    public static String getLocalIp() {
        return System.getProperty("address.local.ip");
    }

    public static boolean isLocalHost(String hostName) {
        return hostName.equals(getLocalIp());
    }

    public static void safeClose(final Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
            } catch (Throwable t) {
                log.error(t);
            }
    }

    public static void safeClose(final Socket closeable) {
        if (closeable != null)
            try {
                closeable.close();
            } catch (Throwable t) {
                log.error(t);
            }
    }
}

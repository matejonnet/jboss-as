/**
 *
 */
package org.jboss.as.paas.controller.dmr;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Zzzz {

    /**
     * @param args
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static void main(String[] args) throws UnknownHostException, SocketException {
//        String localIp = System.getProperty("address.local.ip");
//        //String localIp = InetAddress.getLocalHost().getHostAddress();
//        System.out.println(localIp);
        InetAddress   in  = InetAddress.getLocalHost();
        InetAddress[] all = InetAddress.getAllByName(in.getHostName());
        for (int i=0; i<all.length; i++) {
            System.out.println("  address = " + all[i]);
            System.out.println("  address = " + all[i]);

            Enumeration<InetAddress> ethAddresses = NetworkInterface.getByName("eth0").getInetAddresses();
            //ethAddresses.nextElement().
        }
    }

}

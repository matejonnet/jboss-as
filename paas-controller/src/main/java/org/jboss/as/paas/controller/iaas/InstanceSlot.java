/**
 *
 */
package org.jboss.as.paas.controller.iaas;


/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class InstanceSlot {

    private String instanceId;
    private String hostIP;
    private int slotPosition;

    /**
     * @param hostIP
     * @param slotPosition
     */
    public InstanceSlot(String hostIP, int slotPosition, String instanceId) {
        //TODO add instance instead of hostIP and instanceId
        super();
        this.hostIP = hostIP;
        this.slotPosition = slotPosition;
        this.instanceId = instanceId;
    }

    public int getPortOffset() {
        switch (slotPosition) {
            case 0:
                return 0;
            case 1:
                return 150;
            case 2:
                return 250;
            //TODO define other offsets
            default:
                throw new IllegalArgumentException("invalid slot position " + slotPosition + ".");
        }
    }

    /**
     * @return the hostIP
     */
    public String getHostIP() {
        return hostIP;
    }

    /**
     * @return the slotPosition
     */
    public int getSlotPosition() {
        return slotPosition;
    }

    /**
     * @return the instanceId
     */
    public String getInstanceId() {
        return instanceId;
    }
}

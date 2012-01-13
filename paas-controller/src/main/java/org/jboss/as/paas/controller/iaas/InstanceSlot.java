/**
 *
 */
package org.jboss.as.paas.controller.iaas;

import org.jboss.as.paas.controller.domain.Instance;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class InstanceSlot {

    private int slotPosition;
    private Instance instance;

    /** instance data that is not read from ResourceEntry */
    private Dirty dirty;

    /**
     * @param hostIP
     * @param slotPosition
     */
    public InstanceSlot(Instance instance, int slotPosition) {
        //TODO add instance instead of hostIP and instanceId
        super();
        this.instance = instance;
        this.slotPosition = slotPosition;
    }

    /**
     * @param hostIp
     * @param i
     * @param instanceId
     */
    public InstanceSlot(String hostIp, int slotPosition, String instanceId) {
        this.dirty = new Dirty();
        this.dirty.hostIp = hostIp;
        this.dirty.instanceId = instanceId;
        this.slotPosition = slotPosition;
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
        if (dirty != null)
            return dirty.hostIp;
        return instance.getHostIP();
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
        if (dirty != null)
            return dirty.instanceId;
        return instance.getInstanceId();
    }

    private class Dirty {
        String instanceId;
        String hostIp;
    }
}

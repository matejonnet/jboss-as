/**
 * 
 */
package org.jboss.as.paas.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ServerInstance {
    
    private String id;
    private String provider;
    private String imageId;
    
    private List<String> serverGroups = new ArrayList<String>();
    
    public int getNumberOfGroups() {
        return serverGroups.size();
    }
    
    public boolean addServerGroup(String serverGroup) {
        return serverGroups.add(serverGroup);
    }
    
    public boolean removeServerGroup(String serverGroup) {
        return serverGroups.remove(serverGroup);
    }
    
    /**
     * @param serverGroups the serverGroups to set
     */
    public void setServerGroups(List<String> serverGroups) {
        this.serverGroups = serverGroups;
    }
    
    /**
     * @return the serverGroups
     */
    public List<String> getServerGroups() {
        return serverGroups;
    }
    
    /**
     * @return the imageId
     */
    public String getImageId() {
        return imageId;
    }
    
    /**
     * @param imageId the imageId to set
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}

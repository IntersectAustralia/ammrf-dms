// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.core.service.dto;

import java.lang.String;

privileged aspect CreateDirectoryParameter_Roo_ToString {
    
    public String CreateDirectoryParameter.toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConnectionId: ").append(getConnectionId()).append(", ");
        sb.append("Name: ").append(getName()).append(", ");
        sb.append("Parent: ").append(getParent());
        return sb.toString();
    }
    
}
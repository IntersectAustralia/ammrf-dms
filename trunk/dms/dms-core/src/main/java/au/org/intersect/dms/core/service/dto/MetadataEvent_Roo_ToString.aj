// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.core.service.dto;

import java.lang.String;

privileged aspect MetadataEvent_Roo_ToString {
    
    public String MetadataEvent.toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("JobId: ").append(getJobId()).append(", ");
        sb.append("Items: ").append(getItems() == null ? "null" : getItems().size()).append(", ");
        sb.append("TopUrl: ").append(getTopUrl());
        return sb.toString();
    }
    
}

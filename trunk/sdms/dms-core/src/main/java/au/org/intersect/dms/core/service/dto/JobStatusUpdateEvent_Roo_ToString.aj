// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.core.service.dto;

import java.lang.String;

privileged aspect JobStatusUpdateEvent_Roo_ToString {
    
    public String JobStatusUpdateEvent.toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("JobId: ").append(getJobId()).append(", ");
        sb.append("Status: ").append(getStatus()).append(", ");
        sb.append("TimeStamp: ").append(getTimeStamp());
        return sb.toString();
    }
    
}

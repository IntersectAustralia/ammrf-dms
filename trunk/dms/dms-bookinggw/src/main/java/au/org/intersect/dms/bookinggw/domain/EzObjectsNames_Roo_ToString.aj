// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.bookinggw.domain;

import java.lang.String;

privileged aspect EzObjectsNames_Roo_ToString {
    
    public String EzObjectsNames.toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Objectdescription: ").append(getObjectdescription()).append(", ");
        sb.append("Objectname: ").append(getObjectname()).append(", ");
        sb.append("Id: ").append(getId());
        return sb.toString();
    }
    
}

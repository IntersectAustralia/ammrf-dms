// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.instrument.atomprobe;

import java.lang.String;

privileged aspect Experiment_Roo_ToString {
    
    public String Experiment.toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("FileName: ").append(getFileName()).append(", ");
        sb.append("Username: ").append(getUsername());
        return sb.toString();
    }
    
}

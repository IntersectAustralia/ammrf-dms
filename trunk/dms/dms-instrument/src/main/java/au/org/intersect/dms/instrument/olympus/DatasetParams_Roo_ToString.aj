// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.instrument.olympus;

import java.lang.String;

privileged aspect DatasetParams_Roo_ToString {
    
    public String DatasetParams.toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FromFiles: ").append(getFromFiles() == null ? "null" : getFromFiles().size()).append(", ");
        sb.append("Username: ").append(getUsername()).append(", ");
        sb.append("AbsolutePath: ").append(getAbsolutePath()).append(", ");
        sb.append("ModificationDate: ").append(getModificationDate()).append(", ");
        sb.append("InstrumentProfile: ").append(getInstrumentProfile());
        return sb.toString();
    }
    
}

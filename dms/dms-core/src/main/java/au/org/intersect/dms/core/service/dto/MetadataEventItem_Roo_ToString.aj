// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.core.service.dto;

import java.lang.String;

privileged aspect MetadataEventItem_Roo_ToString {
    
    public String MetadataEventItem.toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Url: ").append(getUrl()).append(", ");
        sb.append("Type: ").append(getType()).append(", ");
        sb.append("Schema: ").append(getSchema()).append(", ");
        sb.append("Metadata: ").append(getMetadata()).append(", ");
        sb.append("Size: ").append(getSize());
        return sb.toString();
    }
    
}

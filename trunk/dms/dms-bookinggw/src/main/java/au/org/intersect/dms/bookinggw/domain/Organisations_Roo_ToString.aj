// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.bookinggw.domain;

import java.lang.String;

privileged aspect Organisations_Roo_ToString {
    
    public String Organisations.toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Userss: ").append(getUserss() == null ? "null" : getUserss().size()).append(", ");
        sb.append("Organisation: ").append(getOrganisation()).append(", ");
        sb.append("Orgid: ").append(getOrgid());
        return sb.toString();
    }
    
}
// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.service.domain;

import au.org.intersect.dms.service.domain.Job;
import java.lang.String;
import java.util.List;

privileged aspect DmsUser_Roo_JavaBean {
    
    public String DmsUser.getUsername() {
        return this.username;
    }
    
    public void DmsUser.setUsername(String username) {
        this.username = username;
    }
    
    public List<Job> DmsUser.getJobs() {
        return this.jobs;
    }
    
    public void DmsUser.setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
    
    public boolean DmsUser.isAdmin() {
        return this.admin;
    }
    
    public void DmsUser.setAdmin(boolean admin) {
        this.admin = admin;
    }
    
}

// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.workerrouter.impl;

import java.lang.String;
import java.util.regex.Pattern;

privileged aspect WorkerRoute_Roo_JavaBean {
    
    public String WorkerRoute.getProtocol() {
        return this.protocol;
    }
    
    public void WorkerRoute.setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String WorkerRoute.getServerPattern() {
        return this.serverPattern;
    }
    
    public Pattern WorkerRoute.getPattern() {
        return this.pattern;
    }
    
    public void WorkerRoute.setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
    
}

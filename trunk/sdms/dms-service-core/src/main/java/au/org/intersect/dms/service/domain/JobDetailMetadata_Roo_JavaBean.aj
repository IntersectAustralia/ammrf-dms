// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.service.domain;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.service.domain.Job;
import java.lang.String;

privileged aspect JobDetailMetadata_Roo_JavaBean {
    
    public Job JobDetailMetadata.getJob() {
        return this.job;
    }
    
    public void JobDetailMetadata.setJob(Job job) {
        this.job = job;
    }
    
    public String JobDetailMetadata.getUrl() {
        return this.url;
    }
    
    public void JobDetailMetadata.setUrl(String url) {
        this.url = url;
    }
    
    public MetadataSchema JobDetailMetadata.getMetadataSchema() {
        return this.metadataSchema;
    }
    
    public void JobDetailMetadata.setMetadataSchema(MetadataSchema metadataSchema) {
        this.metadataSchema = metadataSchema;
    }
    
    public String JobDetailMetadata.getMetadata() {
        return this.metadata;
    }
    
    public void JobDetailMetadata.setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
}
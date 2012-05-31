// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.service.domain;

import au.org.intersect.dms.service.domain.JobDetailMetadata;
import java.lang.Integer;
import java.lang.Long;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import org.springframework.transaction.annotation.Transactional;

privileged aspect JobDetailMetadata_Roo_Entity {
    
    @PersistenceContext(unitName = "servicePU")
    transient EntityManager JobDetailMetadata.entityManager;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long JobDetailMetadata.id;
    
    @Version
    @Column(name = "version")
    private Integer JobDetailMetadata.version;
    
    public Long JobDetailMetadata.getId() {
        return this.id;
    }
    
    public void JobDetailMetadata.setId(Long id) {
        this.id = id;
    }
    
    public Integer JobDetailMetadata.getVersion() {
        return this.version;
    }
    
    public void JobDetailMetadata.setVersion(Integer version) {
        this.version = version;
    }
    
    @Transactional
    public void JobDetailMetadata.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void JobDetailMetadata.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            JobDetailMetadata attached = this.entityManager.find(this.getClass(), this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void JobDetailMetadata.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public JobDetailMetadata JobDetailMetadata.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        JobDetailMetadata merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public static final EntityManager JobDetailMetadata.entityManager() {
        EntityManager em = new JobDetailMetadata().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long JobDetailMetadata.countJobDetailMetadatas() {
        return entityManager().createQuery("select count(o) from JobDetailMetadata o", Long.class).getSingleResult();
    }
    
    public static List<JobDetailMetadata> JobDetailMetadata.findAllJobDetailMetadatas() {
        return entityManager().createQuery("select o from JobDetailMetadata o", JobDetailMetadata.class).getResultList();
    }
    
    public static JobDetailMetadata JobDetailMetadata.findJobDetailMetadata(Long id) {
        if (id == null) return null;
        return entityManager().find(JobDetailMetadata.class, id);
    }
    
    public static List<JobDetailMetadata> JobDetailMetadata.findJobDetailMetadataEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from JobDetailMetadata o", JobDetailMetadata.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
}

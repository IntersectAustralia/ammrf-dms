// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.bookinggw.domain;

import au.org.intersect.dms.bookinggw.domain.Users;
import java.lang.Long;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import org.springframework.transaction.annotation.Transactional;

privileged aspect Users_Roo_Entity {
    
    declare @type: Users: @Entity;
    
    declare @type: Users: @Table(name = "users");
    
    @PersistenceContext(unitName = "bookinggwPU")
    transient EntityManager Users.entityManager;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "userid")
    private Long Users.userid;
    
    public Long Users.getUserid() {
        return this.userid;
    }
    
    public void Users.setUserid(Long id) {
        this.userid = id;
    }
    
    @Transactional
    public void Users.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void Users.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Users attached = this.entityManager.find(this.getClass(), this.userid);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void Users.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public Users Users.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Users merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public static final EntityManager Users.entityManager() {
        EntityManager em = new Users().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long Users.countUserses() {
        return entityManager().createQuery("select count(o) from Users o", Long.class).getSingleResult();
    }
    
    public static List<Users> Users.findAllUserses() {
        return entityManager().createQuery("select o from Users o", Users.class).getResultList();
    }
    
    public static Users Users.findUsers(Long id) {
        if (id == null) return null;
        return entityManager().find(Users.class, id);
    }
    
    public static List<Users> Users.findUsersEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from Users o", Users.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
}

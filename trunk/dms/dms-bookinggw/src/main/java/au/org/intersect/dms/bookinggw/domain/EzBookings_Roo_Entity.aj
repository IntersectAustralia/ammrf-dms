// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.bookinggw.domain;

import au.org.intersect.dms.bookinggw.domain.EzBookings;
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

privileged aspect EzBookings_Roo_Entity {
    
    declare @type: EzBookings: @Entity;
    
    declare @type: EzBookings: @Table(name = "ez_bookings");
    
    @PersistenceContext(unitName = "bookinggwPU")
    transient EntityManager EzBookings.entityManager;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "bookingid")
    private Long EzBookings.bookingid;
    
    public Long EzBookings.getBookingid() {
        return this.bookingid;
    }
    
    public void EzBookings.setBookingid(Long id) {
        this.bookingid = id;
    }
    
    @Transactional
    public void EzBookings.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void EzBookings.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            EzBookings attached = this.entityManager.find(this.getClass(), this.bookingid);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void EzBookings.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public EzBookings EzBookings.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        EzBookings merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public static final EntityManager EzBookings.entityManager() {
        EntityManager em = new EzBookings().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long EzBookings.countEzBookingses() {
        return entityManager().createQuery("select count(o) from EzBookings o", Long.class).getSingleResult();
    }
    
    public static List<EzBookings> EzBookings.findAllEzBookingses() {
        return entityManager().createQuery("select o from EzBookings o", EzBookings.class).getResultList();
    }
    
    public static EzBookings EzBookings.findEzBookings(Long id) {
        if (id == null) return null;
        return entityManager().find(EzBookings.class, id);
    }
    
    public static List<EzBookings> EzBookings.findEzBookingsEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from EzBookings o", EzBookings.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
}
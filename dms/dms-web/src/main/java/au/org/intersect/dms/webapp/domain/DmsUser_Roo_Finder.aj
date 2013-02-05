// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.webapp.domain;

import au.org.intersect.dms.webapp.domain.DmsUser;
import java.lang.String;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

privileged aspect DmsUser_Roo_Finder {
    
    public static TypedQuery<DmsUser> DmsUser.findDmsUsersByUsername(String username) {
        if (username == null || username.length() == 0) throw new IllegalArgumentException("The username argument is required");
        EntityManager em = DmsUser.entityManager();
        TypedQuery<DmsUser> q = em.createQuery("SELECT DmsUser FROM DmsUser AS dmsuser WHERE dmsuser.username = :username", DmsUser.class);
        q.setParameter("username", username);
        return q;
    }
    
}
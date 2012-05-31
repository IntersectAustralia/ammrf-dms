// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.dms.bookinggw.domain;

import au.org.intersect.dms.bookinggw.domain.AgsUsers;
import java.lang.Double;
import java.lang.Long;
import java.lang.String;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

privileged aspect EzBookings_Roo_DbManaged {
    
    @ManyToOne
    @JoinColumn(name = "userid", referencedColumnName = "userid")
    private AgsUsers EzBookings.agsUsers;
    
    @Column(name = "bookingdate")
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date EzBookings.bookingdate;
    
    @Column(name = "comments", length = 255)
    @NotNull
    private String EzBookings.comments;
    
    @Column(name = "fromtime", precision = 22, scale = 0)
    @NotNull
    private Double EzBookings.fromtime;
    
    @Column(name = "fromminute", precision = 22, scale = 0)
    @NotNull
    private Double EzBookings.fromminute;
    
    @Column(name = "objectid")
    @NotNull
    private Long EzBookings.objectid;
    
    @Column(name = "totime", precision = 22, scale = 0)
    @NotNull
    private Double EzBookings.totime;
    
    @Column(name = "tominute", precision = 22, scale = 0)
    @NotNull
    private Double EzBookings.tominute;
    
    public AgsUsers EzBookings.getAgsUsers() {
        return this.agsUsers;
    }
    
    public void EzBookings.setAgsUsers(AgsUsers agsUsers) {
        this.agsUsers = agsUsers;
    }
    
    public Date EzBookings.getBookingdate() {
        return this.bookingdate;
    }
    
    public void EzBookings.setBookingdate(Date bookingdate) {
        this.bookingdate = bookingdate;
    }
    
    public String EzBookings.getComments() {
        return this.comments;
    }
    
    public void EzBookings.setComments(String comments) {
        this.comments = comments;
    }
    
    public Double EzBookings.getFromtime() {
        return this.fromtime;
    }
    
    public void EzBookings.setFromtime(Double fromtime) {
        this.fromtime = fromtime;
    }
    
    public Double EzBookings.getFromminute() {
        return this.fromminute;
    }
    
    public void EzBookings.setFromminute(Double fromminute) {
        this.fromminute = fromminute;
    }
    
    public Long EzBookings.getObjectid() {
        return this.objectid;
    }
    
    public void EzBookings.setObjectid(Long objectid) {
        this.objectid = objectid;
    }
    
    public Double EzBookings.getTotime() {
        return this.totime;
    }
    
    public void EzBookings.setTotime(Double totime) {
        this.totime = totime;
    }
    
    public Double EzBookings.getTominute() {
        return this.tominute;
    }
    
    public void EzBookings.setTominute(Double tominute) {
        this.tominute = tominute;
    }
    
}
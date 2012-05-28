/**
 * Project: Platforms for Collaboration at the AMMRF
 *
 * Copyright (c) Intersect Pty Ltd, 2011
 *
 * @see http://www.ammrf.org.au
 * @see http://www.intersect.org.au
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * This program contains open source third party libraries from a number of
 * sources, please read the THIRD_PARTY.txt file for more details.
 */

package au.org.intersect.dms.bookinggw.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.dms.bookinggw.Booking;
import au.org.intersect.dms.bookinggw.BookingGatewayInterface;
import au.org.intersect.dms.bookinggw.InvestigatorRole;
import au.org.intersect.dms.bookinggw.Project;
import au.org.intersect.dms.bookinggw.ProjectDetails;
import au.org.intersect.dms.bookinggw.ProjectParticipation;
import au.org.intersect.dms.bookinggw.TimeMinute;
import au.org.intersect.dms.bookinggw.UserDetails;
import au.org.intersect.dms.bookinggw.domain.AgsUsers;
import au.org.intersect.dms.bookinggw.domain.EzBookings;
import au.org.intersect.dms.bookinggw.domain.EzObjectsNames;
import au.org.intersect.dms.bookinggw.domain.EzObjectsNamesPK;
import au.org.intersect.dms.bookinggw.domain.Participants;
import au.org.intersect.dms.bookinggw.domain.Projects;
import au.org.intersect.dms.bookinggw.domain.Users;

/**
 * Describes the methods required to support the UI interaction and ingestion process at AMMRF-USyd. Although it may be
 * argued that those are two different orthogonal features, one single provider (interface) will make deployment and
 * maintenance for AMMRF-USyd simpler.
 * 
 * @author carlos
 */
@Service(value = "bookingGW")
public class BookingGatewayServiceImpl implements BookingGatewayInterface
{

    private static final String SPACE = " ";

    /**
     * Logger for calls.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BookingGatewayServiceImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, value = "bookinggw")
    public boolean checkPassword(String username, String password)
    {
        LOG.info("checkPassword username={}, password=******", username);
        AgsUsers user = findUserByName(username);
        if (user == null)
        {
            return false;
        }
        if (StringUtils.defaultString(user.getPassword()).length() == 0)
        {
            return StringUtils.defaultString(password).length() == 0;
        }
        return user.getPassword().equals(StringUtils.defaultString(password));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, value = "bookinggw")
    public Booking[] getBookingsRange(String username, long instrument, final Date fromDate, final Date toDate)
    {
        AgsUsers user = findUserByName(username);
        if (user == null)
        {
            return new Booking[0];
        }
        EzBookings[] rows = EzBookings.findByObjectAndDateRange(user.getUserid(), instrument, fromDate, toDate);
        return convertToBookings(rows);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Booking getBooking(String username, long instrument, Date date)
    {
        Date fromDate = DateUtils.getFromDate(date);
        Date toDate = DateUtils.getToDate(date);

        LOG.debug("Searching for booking fromDate: {}, toDate: {}", fromDate, toDate);
        Booking[] bookings = getBookingsRange(username, instrument, fromDate, toDate);
        if (bookings.length > 0)
        {
            return bookings[0];
        }
        else
        {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, value = "bookinggw")
    public ProjectParticipation[] getProjectParticipations(String username)
    {
        try
        {
            AgsUsers user = findUserByName(username);
            if (user == null)
            {
                return new ProjectParticipation[0];
            }
            Participants[] rows = Participants.findByUser(user.getUserid());
            return convertToProjectParticipation(rows);
        }
        catch (org.springframework.orm.jpa.JpaObjectRetrievalFailureException e)
        {
            return new ProjectParticipation[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true, value = "bookinggw")
    public ProjectDetails getProjectDetails(long projectCode, long bookingId)
    {
        Projects proj = Projects.findProjects(projectCode);
        if (proj == null)
        {
            return new ProjectDetails(projectCode, "", bookingId, "", "", new InvestigatorRole[0]);
        }
        String instrument = "";
        if (bookingId > 0)
        {
            EzBookings booking = EzBookings.findEzBookings(bookingId);
            if (booking != null && booking.getObjectid() != null)
            {
                EzObjectsNamesPK pk = new EzObjectsNamesPK("en", booking.getObjectid());
                EzObjectsNames objName = EzObjectsNames.findEzObjectsNames(pk);
                instrument = objName == null ? "" : objName.getObjectname();
            }
        }
        Set<Participants> partSet = proj.getParticipantss();
        InvestigatorRole[] invRoles;
        if (partSet == null)
        {
            invRoles = new InvestigatorRole[0];
        }
        else
        {
            invRoles = convertToInvestigatorRole(partSet);
        }
        return new ProjectDetails(projectCode, proj.getTitle(), bookingId, instrument, proj.getOutline(), invRoles);
    }

    /**
     * Find user by username.
     * 
     * @param username
     *            the username
     * @return the AgsUser or null if not found.
     */
    private AgsUsers findUserByName(String username)
    {
        return AgsUsers.findAgsUsersByUsernameEquals(username);
    }

    /**
     * Converts an EzBooking to a Booking.
     * 
     * @param anEzBooking
     *            the EzBooking
     * @return the booking
     */
    private static Booking[] convertToBookings(EzBookings[] ezBookings)
    {
        if (ezBookings == null)
        {
            return new Booking[0];
        }
        Booking[] resp = new Booking[ezBookings.length];
        for (int i = 0; i < ezBookings.length; i++)
        {
            Booking item = new Booking();
            EzBookings anEzBooking = ezBookings[i];
            item.setBookingId(anEzBooking.getBookingid());
            item.setBookingDate(anEzBooking.getBookingdate());
            item.setFrom(new TimeMinute(anEzBooking.getFromtime(), anEzBooking.getFromminute()));
            item.setTo(new TimeMinute(anEzBooking.getTotime(), anEzBooking.getTominute()));
            item.setComments(anEzBooking.getComments());
            resp[i] = item;
        }
        return resp;
    }

    /**
     * @param aParticipant
     *            the participant model from DB.
     * @param username
     *            the username
     * @return a project participation
     */
    private static ProjectParticipation[] convertToProjectParticipation(Participants[] participant)
    {
        if (participant == null)
        {
            return new ProjectParticipation[0];
        }
        ProjectParticipation[] resp = new ProjectParticipation[participant.length];
        for (int i = 0; i < participant.length; i++)
        {
            Participants aParticipant = participant[i];
            ProjectParticipation item = new ProjectParticipation(convertToProject(aParticipant.getProjects()),
                    aParticipant.getComments());
            resp[i] = item;
        }
        return resp;
    }

    /**
     * @param projectModel
     *            the project from model package.
     * @return a Project object carrying some data from project model
     */
    private static Project convertToProject(Projects projectModel)
    {
        return new Project(projectModel.getProjcode(), projectModel.getTitle());
    }

    /**
     * @param p
     *            the participation to convert
     * @return an InvestigatorRole object
     */
    private static InvestigatorRole[] convertToInvestigatorRole(Set<Participants> participations)
    {
        Set<InvestigatorRole> resp = new HashSet<InvestigatorRole>();
        for (Participants p : participations)
        {
            String fname = p.getAgsUsers1().getUserFname();
            String lname = p.getAgsUsers1().getUserLname();
            String mname;
            if (fname.indexOf(SPACE) > 0)
            {
                mname = fname.substring(fname.indexOf(SPACE) + 1);
                fname = fname.substring(0, fname.indexOf(SPACE));
            }
            else
            {
                mname = "";
            }
            String initials = calculateInitials(fname, mname, p.getAgsUsers1().getUserLname());
            String title;
            String organization;
            String orgId;
            Users user = Users.findUsers(p.getAgsUsers1().getUserid());
            if (user != null)
            {
                title = user.getTitle();
                organization = user.getOrganisations().getOrganisation();
                orgId = Long.toString(user.getOrganisations().getOrgid());
            }
            else
            {
                title = "";
                organization = "";
                orgId = "";
            }
            InvestigatorRole item = new InvestigatorRole(p.getAgsUsers1().getUserid(), new String[] {title, fname,
                mname, lname, initials}, organization, orgId);
            resp.add(item);
        }

        return resp.toArray(new InvestigatorRole[resp.size()]);
    }

    /**
     * Calculate initials from name parts.
     * 
     * @param fname
     *            the first name.
     * @param mname
     *            the middle name.
     * @param lname
     *            the last name.
     * @return initials
     */
    private static String calculateInitials(String fname, String mname, String lname)
    {
        StringTokenizer tokenizer = new StringTokenizer(fname + SPACE + mname + SPACE + lname);
        StringBuffer sb = new StringBuffer();
        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            if (token.length() > 0)
            {
                sb.append(Character.toUpperCase(token.charAt(0)));
            }
        }
        return sb.toString();
    }

    @Override
    public UserDetails getUserDetails(String username)
    {
        LOG.info("getUserDetails username={}", username);
        AgsUsers user = findUserByName(username);
        UserDetails userDetails = new UserDetails();
        if (user != null)
        {
            userDetails.setUsername(username);
            userDetails.setPasswordHash(user.getPassword());
            userDetails.setFirstName(user.getUserFname());
            userDetails.setLastName(user.getUserLname());
            userDetails.setEmail(user.getEmail());
        }
        else
        {
            userDetails.setUsername(username);
            userDetails.setPasswordHash("");
            userDetails.setFirstName("");
            userDetails.setLastName("");
            userDetails.setEmail("");
        }
        return userDetails;
    }

    @Override
    public Project[] getProjects(String username)
    {
        ProjectParticipation[] projectParticipations = getProjectParticipations(username);
        Project[] projects = new Project[projectParticipations.length];

        for (int i = 0; i < projectParticipations.length; i++)
        {
            projects[i] = projectParticipations[i].getProject();
        }
        return projects;
    }

}

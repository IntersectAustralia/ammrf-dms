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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.unitils.reflectionassert.ReflectionAssert.assertLenientEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import au.org.intersect.dms.bookinggw.Booking;
import au.org.intersect.dms.bookinggw.BookingGatewayInterface;
import au.org.intersect.dms.bookinggw.InvestigatorRole;
import au.org.intersect.dms.bookinggw.Project;
import au.org.intersect.dms.bookinggw.ProjectDetails;
import au.org.intersect.dms.bookinggw.ProjectParticipation;
import au.org.intersect.dms.bookinggw.TimeMinute;
import au.org.intersect.dms.test.DBUnitConfiguration;
import au.org.intersect.dms.test.DBUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/META-INF/spring/applicationContext-bookinggw.xml"})
@DBUnitConfiguration(locations = "dataset.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DBUnitTestExecutionListener.class})
public class BookingGatewayServiceImplTest extends AbstractJUnit4SpringContextTests
{

    private static final String USERNAME_2 = "user002";
    private static final String PASSWORD_1 = "plain001";
    private static final String USERNAME_1 = "user001";
    private static final String PROJECT_TITLE_2 = "Refraction of another alien sample";
    private static final String PROJECT_TITLE_1 = "Refraction of alien sample";
    private static final String DR = "Dr";
    @Autowired
    private BookingGatewayInterface impl;

    @Test
    public void checkPassword_Valid()
    {
        assertTrue(impl.checkPassword(USERNAME_1, PASSWORD_1)); // this password
        // should work
    }

    @Test
    public void checkPassword_Invalid()
    {
        // this password shouldn't work
        assertFalse(impl.checkPassword(USERNAME_1, "plain002"));
        // non-existent user
        assertFalse(impl.checkPassword("user999", "plain002"));
    }

    @Test
    public void checkPassword_NullPassword()
    {
        String[] usersWithEmptyPassword = {USERNAME_2, "user003"};
        for (String user : usersWithEmptyPassword)
        {
            assertTrue(impl.checkPassword(user, null));
            assertTrue(impl.checkPassword(user, ""));
        }
        assertFalse(impl.checkPassword(USERNAME_1, null));
        assertFalse(impl.checkPassword(USERNAME_1, ""));
    }

    @Test
    public void getBookingsRange_Empty()
    {
        Booking[] range = impl.getBookingsRange(USERNAME_1, 1, newDate(2008, 10, 1), newDate(2008, 10, 8));
        assertArrayEquals(new Booking[0], range);
        range = impl.getBookingsRange("user999", 1, newDate(2008, 10, 1), newDate(2008, 10, 8));
        assertArrayEquals(new Booking[0], range);
    }

    @Test
    public void getBookingsRange_ValidRange()
    {

        Booking[] range = impl.getBookingsRange(USERNAME_1, 1, newDate(2009, 6, 20), newDate(2009, 6, 20));
        assertEquals(1, range.length);
        assertLenientEquals(new Booking(101, newDate(2009, 6, 20), new TimeMinute(10, 0), new TimeMinute(12, 0),
                "Single comment"), range[0]);

        range = impl.getBookingsRange(USERNAME_2, 2, newDate(2009, 6, 20), newDate(2009, 6, 25));
        assertEquals(1, range.length);
        assertLenientEquals(new Booking(102, newDate(2009, 6, 20), new TimeMinute(10, 0), new TimeMinute(12, 0),
                "Single comment 2"), range[0]);

        range = impl.getBookingsRange(USERNAME_2, 1, newDate(2009, 6, 20), newDate(2009, 6, 25));
        Booking[] expected = new Booking[] {
            new Booking(103, newDate(2009, 6, 21), new TimeMinute(10, 0), new TimeMinute(11, 0), "Single comment 3"),
            new Booking(104, newDate(2009, 6, 22), new TimeMinute(10, 0), new TimeMinute(11, 30), "Single comment 4")};
        assertLenientEquals(expected, range);

    }
    
    @Test
    public void getBooking_ValidDate()
    {
        Booking booking = impl.getBooking(USERNAME_2, 1, newDate(2009, 6, 21));
        Booking expected = 
            new Booking(103, newDate(2009, 6, 21), new TimeMinute(10, 0), new TimeMinute(11, 0), "Single comment 3");
        assertLenientEquals(expected, booking);

    }

    @Test
    public void getBookingsRange_InvalidRange()
    {
        Booking[] range = impl.getBookingsRange(USERNAME_2, 1, newDate(2008, 6, 20), newDate(2008, 6, 21));
        assertEquals(0, range.length);
    }

    @Test
    public void getBookingsRange_InvalidUser()
    {
        Booking[] range = impl.getBookingsRange("user004", 1, newDate(2008, 6, 20), newDate(2009, 6, 22));
        assertEquals(0, range.length);
    }

    @Test
    public void getProjectParticipations_Empty()
    {
        ProjectParticipation[] resp = impl.getProjectParticipations("user003");
        assertEquals(0, resp.length);
    }

    @Test
    public void getProjectParticipations_ValidUser()
    {

        ProjectParticipation[] resp = impl.getProjectParticipations(USERNAME_1);
        ProjectParticipation[] expected = new ProjectParticipation[] {new ProjectParticipation(new Project(1,
                PROJECT_TITLE_1), "What is this for?")};
        assertLenientEquals(expected, resp);

        resp = impl.getProjectParticipations(USERNAME_2);
        expected = new ProjectParticipation[] {
            new ProjectParticipation(new Project(1, PROJECT_TITLE_1), "What is this for?"),
            new ProjectParticipation(new Project(2, PROJECT_TITLE_2), "What is this for again?")};
        assertLenientEquals(expected, resp);

    }

    @Test
    public void getProjectParticipations_InvalidUser()
    {
        ProjectParticipation[] resp = impl.getProjectParticipations("user005");
        assertEquals(0, resp.length);
    }

    @Test
    public void getProjectDetails_ValidArguments()
    {
        ProjectDetails actual = impl.getProjectDetails(1, 101);
        InvestigatorRole[] investigators = new InvestigatorRole[] {
            new InvestigatorRole(1, new String[] {DR, "John", "", "Smith", "JS"}, "Organisation 21", "21"),
            new InvestigatorRole(2, new String[] {DR, "Mary", "Josephine", "Poppins", "MJP"}, "The 22nd org",
                    "22")};
        ProjectDetails expected = new ProjectDetails(1, PROJECT_TITLE_1, 101, "X-Ray thing",
                "We got sample from a alleged alien spaceship and we are analysing its properties.", investigators);
        assertLenientEquals(expected, actual);

        actual = impl.getProjectDetails(2, 105);
        investigators = new InvestigatorRole[] {new InvestigatorRole(2, new String[] {DR, "Mary", "Josephine",
            "Poppins", "MJP"}, "The 22nd org", "22")};
        expected = new ProjectDetails(2, PROJECT_TITLE_2, 105, "Y-Ray thing",
                "We got another sample from a alleged alien spaceship and we are analysing its structure.",
                investigators);
        assertLenientEquals(expected, actual);
    }

    @Test
    public void getProjectDetails_InvalidArguments()
    {
        // TODO define with AMMRF fault conditions
    }

    private Date newDate(int year, int month, int date)
    {
        Calendar cal = GregorianCalendar.getInstance();
        cal.clear();
        cal.set(year, month, date);
        return cal.getTime();
    }

}

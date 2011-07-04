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

package au.org.intersect.dms.service.email;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.mock_javamail.Mailbox;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import au.org.intersect.dms.bookinggw.BookingGatewayInterface;
import au.org.intersect.dms.bookinggw.UserDetails;
import au.org.intersect.dms.catalogue.Dataset;
import au.org.intersect.dms.catalogue.MetadataRepository;

@RunWith(PowerMockRunner.class)
public class ReminderEmailSenderTest extends CamelTestSupport
{
    private static final String EMAIL_ADDRESS_SUFFIX = "@dms-test.org.au";

    private static final String USER2 = "user2";

    private static final String USER1 = "user1";

    private static final String EMAIL_CONTENT = "Test email";

    @Mock
    private ReminderEmailBuilder emailBuilder;

    @Mock
    private MetadataRepository repository;

    @Mock
    private BookingGatewayInterface bookingGateway;

    @InjectMocks
    private ReminderEmailSender emailSender = new ReminderEmailSender();

    private List<UserDetails> userDetails = new LinkedList<UserDetails>();

    private Map<String, List<Dataset>> datasets = new HashMap<String, List<Dataset>>();

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        ReflectionTestUtils.setField(emailSender, "producerTemplate", template);
        emailSender.setEmailServerEndpointURL("smtp://localhost");
        emailSender.setFromEmail("dms.test@intersesct.org.au");
        emailSender.setSubject("Test subject");

        userDetails.add(createAndSetUpUser(USER1));

        when(repository.findPrivateDatasets()).thenReturn(datasets);
    }

    private UserDetails createAndSetUpUser(String username)
    {
        UserDetails userDetails = new UserDetails();
        userDetails.setEmail(username + EMAIL_ADDRESS_SUFFIX);
        when(bookingGateway.getUserDetails(username)).thenReturn(userDetails);

        List<Dataset> userDatasets = createDatasets(username, 3);
        datasets.put(username, userDatasets);

        when(emailBuilder.buildEmail(userDetails, userDatasets)).thenReturn(
                EMAIL_CONTENT);

        return userDetails;
    }

    private List<Dataset> createDatasets(String owner, int number)
    {
        List<Dataset> datasets = new ArrayList<Dataset>(number);
        for (int i = 1; i <= number; i++)
        {
            Dataset dataset = new Dataset();
            dataset.setOwner(owner);
            dataset.setUrl("ftp://localhost/datsets/" + owner + "/" + i);
            datasets.add(dataset);
        }
        return datasets;
    }

    @After
    public void tearDown() throws Exception
    {
        datasets.clear();
        Mailbox.clearAll();
    }

    @Test
    public void sendOneEmail() throws IOException, MessagingException
    {
        emailSender.sendReminderEmails();
        verifyEmails(USER1);
    }

    @Test
    public void sendTwoEmails() throws IOException, MessagingException
    {
        userDetails.add(createAndSetUpUser(USER2));
        emailSender.sendReminderEmails();
        verifyEmails(USER1);
        verifyEmails(USER2);
    }

    @Test
    public void noEmails() throws AddressException
    {
        datasets.put(USER1, Collections.EMPTY_LIST);

        emailSender.sendReminderEmails();

        Mailbox inbox = Mailbox.get(USER1 + EMAIL_ADDRESS_SUFFIX);
        assertEquals("Should be no emails", 0, inbox.size());
    }

    private void verifyEmails(String username) throws IOException, MessagingException
    {
        Mailbox inbox = Mailbox.get(username + EMAIL_ADDRESS_SUFFIX);
        assertEquals("Should be only one email for one user", 1, inbox.size());
        Message message = inbox.get(0);
        assertEquals("Wrong content type", "text/html", message.getContentType());
        assertEquals("Message content is wrong", EMAIL_CONTENT, message.getContent().toString());
    }

}

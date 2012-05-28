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

package au.org.intersect.dms.email;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;

import au.org.intersect.dms.bookinggw.BookingGatewayInterface;
import au.org.intersect.dms.bookinggw.UserDetails;
import au.org.intersect.dms.catalogue.Dataset;
import au.org.intersect.dms.catalogue.MetadataRepository;

/**
 * Sends reminder email to researchers to publish data
 * 
 * @version $Rev: 29 $
 */
public class ReminderEmailSender
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderEmailSender.class);

    @Autowired
    private MetadataRepository repository;

    @Autowired
    private BookingGatewayInterface bookingGateway;

    @Autowired
    private ReminderEmailBuilder emailBuilder;

    @Autowired
    private JavaMailSender mailSender;

    private String fromEmail;

    private String subject;

    @Required
    public void setFromEmail(String fromEmail)
    {
        this.fromEmail = fromEmail;
    }

    @Required
    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    /**
     * Sends reminder emails to all users with unpublished datasets
     * 
     * @throws MessagingException
     */
    @Scheduled(cron = "${dms.email.schedule}")
    public void sendReminderEmails()
    {
        Map<String, List<Dataset>> datasets = findUnpublishedDatasets();

        LOGGER.info("Sending email reminder to {} user(s)", datasets.size());

        for (Entry<String, List<Dataset>> entry : datasets.entrySet())
        {
            String username = entry.getKey();
            List<Dataset> userDatasets = entry.getValue();
            if (!userDatasets.isEmpty())
            {
                UserDetails userDetails = bookingGateway.getUserDetails(username);

                String emailBody = emailBuilder.buildEmail(userDetails, userDatasets);

                LOGGER.debug("Sending email to <{}> email body:\n{}", userDetails.getEmail(), emailBody);
                if (userDetails.getEmail() != null)
                {
                    MimeMessage mailMessage = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mailMessage, "UTF-8");
                    try
                    {
                        helper.setTo(userDetails.getEmail());
                        helper.setFrom(fromEmail);
                        helper.setSubject(subject);
                        helper.setText(emailBody, true);
                    }
                    catch (MessagingException e)
                    {
                        LOGGER.error("Exception during building email to user " + userDetails.getUsername()
                                + ". Email skipped.", e);
                        continue;
                    }
                    mailSender.send(mailMessage);
                }
                else
                {
                    LOGGER.info("Email address is NULL for user: {}. No email will be sent.", username);
                }
            }
            else
            {
                LOGGER.info("No unpublished datasets for user {}. No emails will be sent.", username);
            }
        }
    }

    private Map<String, List<Dataset>> findUnpublishedDatasets()
    {
        return repository.findPrivateDatasets();
    }

}

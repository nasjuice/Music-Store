package com.pandamedia.beans.purchasing;

import com.pandamedia.utilities.Messages;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import jodd.mail.Email;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;
import jodd.mail.SmtpSslServer;
import persistence.entities.Invoice;
import persistence.entities.InvoiceAlbum;
import persistence.entities.InvoiceTrack;
import persistence.entities.ShopUser;

/**
 * This class provides methods to send an invoice by email
 * to a client.
 * 
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class EmailBean {
    private static final Logger LOG = Logger.getLogger("EmailBean.class");
    private final String emailAddress="ebourquesend@gmail.com";
    private final String emailPassword="erikasendemail";
    private final String smtpServerName="smtp.gmail.com";
    private Invoice invoice;
    
    /**
     * Default constructor.
     */
    public EmailBean()
    {
        super();
    }
    
    /**
     * This method prepares and sends the email.
     * 
     * @author Erika Bourque
     * @param userEmail
     * @param invoice 
     */
    public void sendInvoiceEmail(String userEmail, Invoice invoice)
    {
        this.invoice = invoice;
        Email email = new Email();
        String subject = Messages.getString("bundles.messages", "emailSubject", null);
        
        // Preparing the email fields
        email.to(userEmail).from(emailAddress).subject(subject);        
        email.addHtml(buildMessage());

        // Send email
        send(email);
    }
    
    
    /**
     * This method prepares the content of the email.
     * 
     * @author Erika Bourque
     * @return The content of the email
     */
    private String buildMessage()
    {
        LOG.info("Building email.");
        StringBuilder builder = new StringBuilder();
        
        // Email body start
        builder.append("<html><META http-equiv=Content-Type content=\"text/html; charset=utf-8\"><body><h3>");
        builder.append(Messages.getString("bundles.messages", "emailGreeting", new Object[]{invoice.getUserId().getFirstName()}));
        builder.append("</h3><p>");
        builder.append(Messages.getString("bundles.messages", "emailThanks", null));
        builder.append("</p>");
        
        // Add the invoice details
        invoiceDetailsTable(builder);        
        builder.append("<br/>");
        billingInfoTable(builder);
        builder.append("<br/>");
        if (!invoice.getInvoiceAlbumList().isEmpty())
        {
            albumInfoTable(builder);
            builder.append("<br/>");
        }
        if (!invoice.getInvoiceTrackList().isEmpty())
        {
            trackInfoTable(builder);
            builder.append("<br/>");
        }
        
        // Email body end
        builder.append("</body></html>");
        
        return builder.toString();
    }
    
    /**
     * This method sends the email through the Jodd Mail API.
     * 
     * @author Erika Bourque
     */
    private void send(Email email)
    {
        LOG.info("Sending email.");        
        SmtpServer<SmtpSslServer> smtpServer = SmtpSslServer
                .create(smtpServerName)
                .authenticateWith(emailAddress, emailPassword);
        // Display Java Mail debug conversation with the server
        smtpServer.debug(true);
        SendMailSession session = smtpServer.createSession();
        session.open();
        session.sendMail(email);
        session.close();
    }
    
    /**
     * This method builds the invoice details table in html.
     * 
     * @author Erika Bourque
     * @param builder the StringBuilder being used for the email content
     */
    private void invoiceDetailsTable(StringBuilder builder)
    {
        String[] headers = {"invoiceNumHeader", "saleDateHeader", "subtotalHeader", 
            "gstLbl", "pstLbl", "hstLbl", "totalHeader"};
        Object[] fields = {invoice.getId(), invoice.getSaleDate(), invoice.getTotalGrossValue(), 
            invoice.getGstTax(), invoice.getPstTax(), invoice.getHstTax(), invoice.getTotalNetValue()};
        
        // Header
        builder.append("<table><tr><th colspan=2>");
        builder.append(Messages.getString("bundles.messages", "invoiceSummaryTitle", null));
        builder.append("</th></tr>");
        
        // Invoice details
        for(int i = 0; i < headers.length; i++)
        {
            builder.append("<tr><td>");
            builder.append(Messages.getString("bundles.messages", headers[i], null));
            builder.append("</td><td>");
            builder.append(fields[i]);
            builder.append("</td></tr>");
        }
        
        builder.append("</table>");
    }
    
    /**
     * This method builds the billing info table in html.
     * 
     * @author Erika Bourque
     * @param builder the StringBuilder being used for the email content
     */
    private void billingInfoTable(StringBuilder builder)
    {
        ShopUser user = invoice.getUserId();
        
        // Creating lines for billing info
        String line1 = user.getTitle() + " " + user.getFirstName() + " " + user.getLastName();
        String line2 = user.getStreetAddress() + " " + user.getStreetAddress2();
        String line3 = user.getCity() + " " + user.getProvinceId().getName() + " " + user.getPostalCode();
        String line5 = user.getHomePhone() + " " + user.getCellPhone();
        String[] fields = {line1, line2, line3, user.getCountry(), line5};
        
        // Header
        builder.append("<table><tr><th>");
        builder.append(Messages.getString("bundles.messages", "billingTitle", null));
        builder.append("</th></tr>");
        
        // Billing detail lines
        for (String field : fields) {
            builder.append("<tr><td>");
            builder.append(field);
            builder.append("</td></tr>");
        }
        
        builder.append("</table>");
    }
    
    /**
     * This method builds the track purchase details table in html.
     * 
     * @author Erika Bourque
     * @param builder the StringBuilder being used for the email content
     */
    private void trackInfoTable(StringBuilder builder)
    {
        List<InvoiceTrack> tracks = invoice.getInvoiceTrackList();
        
        // Headers
        builder.append("<table><tr><th colspan=3>");
        builder.append(Messages.getString("bundles.messages", "trackLbl", null));
        builder.append("</th></tr><tr><th>");
        builder.append(Messages.getString("bundles.messages", "titleHeader", null));
        builder.append("</th><th>");
        builder.append(Messages.getString("bundles.messages", "artistHeader", null));
        builder.append("</th><th>");
        builder.append(Messages.getString("bundles.messages", "priceHeader", null));
        builder.append("</th></tr>");
        
        // Track details
        for(InvoiceTrack track : tracks)
        {
            builder.append("<tr><td>");
            builder.append(track.getTrack().getTitle());
            builder.append("</td><td>");
            builder.append(track.getTrack().getArtistId().getName());
            builder.append("</td><td>");
            builder.append(track.getFinalPrice());
            builder.append("</td></tr>");
        }
        
        builder.append("</table>");
    }
    
    /**
     * This method builds the album purchase details table in html.
     * 
     * @author Erika Bourque
     * @param builder the StringBuilder being used for the email content
     */
    private void albumInfoTable(StringBuilder builder)
    {
        List<InvoiceAlbum> albums = invoice.getInvoiceAlbumList();
        
        // Headers
        builder.append("<table><tr><th colspan=3>");
        builder.append(Messages.getString("bundles.messages", "albumLbl", null));
        builder.append("</th></tr><tr><th>");
        builder.append(Messages.getString("bundles.messages", "titleHeader", null));
        builder.append("</th><th>");
        builder.append(Messages.getString("bundles.messages", "artistHeader", null));
        builder.append("</th><th>");
        builder.append(Messages.getString("bundles.messages", "priceHeader", null));
        builder.append("</th></tr>");
        
        // Album details
        for(InvoiceAlbum album : albums)
        {
            builder.append("<tr><td>");
            builder.append(album.getAlbum().getTitle());
            builder.append("</td><td>");
            builder.append(album.getAlbum().getArtistId().getName());
            builder.append("</td><td>");
            builder.append(album.getFinalPrice());
            builder.append("</td></tr>");
        }
        
        builder.append("</table>");
    }
}

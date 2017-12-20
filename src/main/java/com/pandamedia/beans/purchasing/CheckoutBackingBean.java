package com.pandamedia.beans.purchasing;

import com.pandamedia.beans.UserActionBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import persistence.controllers.InvoiceAlbumJpaController;
import persistence.controllers.InvoiceJpaController;
import persistence.controllers.InvoiceTrackJpaController;
import persistence.entities.Album;
import persistence.entities.Invoice;
import persistence.entities.InvoiceAlbum;
import persistence.entities.InvoiceAlbumPK;
import persistence.entities.InvoiceTrack;
import persistence.entities.InvoiceTrackPK;
import persistence.entities.ShopUser;
import persistence.entities.Track;

/**
 * This class provides the methods through which a client can pay 
 * for their purchases and receive their invoice by email.
 * 
 * @author Erika Bourque
 */
@Named("checkout")
@RequestScoped
public class CheckoutBackingBean implements Serializable {

    @Inject
    private ShoppingCart cart;

    @Inject
    private UserActionBean uab;

    @Inject
    private InvoiceJpaController invoiceController;
    
    @Inject
    private InvoiceAlbumJpaController invoiceAlbumController;
    
    @Inject
    private InvoiceTrackJpaController invoiceTrackController;
    
    @Inject
    private EmailBean emailer;

    /**
     * This method creates a list of months in the form of SelectItems for use
     * in the select one menu tag.
     *
     * @author Erika Bourque
     * @return The list of months
     */
    public List<String> getMonthSelector() {
        List<String> list = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            if (i < 10) // For display purposes, adds a 0 in front
            {
                list.add("0" + i);
            } else // Uses value as display by default
            {
                list.add("" + i);
            }
        }

        return list;
    }

    /**
     * This method creates a list of years in the form of SelectItems for use in
     * the select one menu tag.
     *
     * @author Erika Bourque
     * @return The list of years
     */
    public List<String> getYearSelector() {
        List<String> list = new ArrayList<>();
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        // Default max value of exp years = 10
        int maxYears = 10;

        for (int i = 0; i < maxYears; i++) {
            int temp = curYear + i;
            list.add("" + temp);
        }

        return list;
    }

    /**
     * Getter for gst total of the purchase.
     *
     * @author Erika Bourque
     * @return the gst
     */
    public double getGst() {
        return cart.getSubTotal() * uab.getCurrUser().getProvinceId().getGstRate();
    }

    /**
     * Getter for pst total of the purchase.
     *
     * @author Erika Bourque
     * @return the pst
     */
    public double getPst() {
        return cart.getSubTotal() * uab.getCurrUser().getProvinceId().getPstRate();
    }

    /**
     * Getter for hst total of the purchase.
     *
     * @author Erika Bourque
     * @return the hst
     */
    public double getHst() {
        return cart.getSubTotal() * uab.getCurrUser().getProvinceId().getHstRate();
    }

    /**
     * Getter for net total of the purchase.
     *
     * @author Erika Bourque
     * @return the net total
     */
    public double getTotal() {
        return cart.getSubTotal() + getGst() + getPst() + getHst();
    }

    /**
     * This method finalizes the transaction and redirects the user to the
     * invoice summary page.
     *
     * @author Erika Bourque
     * @return  The name of the page to redirect to
     * @throws Exception    if the persist of the invoice fails
     */
    public String finalizePurchase() throws Exception {
        // Create the invoice
        Invoice invoice = buildInvoice();
        
        // Persist invoice
        invoiceController.create(invoice);

        // Persist invoice purchases and set in current object        
        invoice.setInvoiceAlbumList(buildInvoiceAlbumList(invoice));
        invoice.setInvoiceTrackList(buildInvoiceTrackList(invoice));

        // Emptying the cart of all purchases
        cart.clearCart();
        
        // Send email of invoice details
        emailer.sendInvoiceEmail(uab.getCurrUser().getEmail(), invoice);
        
        // Redirecting to invoice summary page
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("invoice", invoice);
        return "invoicesummary";
    }

    /**
     * This method creates an invoice based on the purchase details 
     * and the cart.
     * 
     * @author Erika Bourque
     * @return The invoice
     */
    private Invoice buildInvoice()
    {
        Invoice invoice = new Invoice();
        
        // Setting the invoice details
        invoice.setSaleDate(Calendar.getInstance().getTime());
        invoice.setTotalGrossValue(cart.getSubTotal());
        invoice.setGstTax(getGst());
        invoice.setHstTax(getHst());
        invoice.setPstTax(getPst());
        invoice.setTotalNetValue(getTotal());
        invoice.setUserId(uab.getCurrUser());
        
        return invoice;
    }
    
    /**
     * This method creates a list of the InvoiceAlbums, based on the albums
     * in the cart.
     * 
     * @author Erika Bourque
     * @return  The list of InvoiceAlbums
     */
    private List<InvoiceAlbum> buildInvoiceAlbumList(Invoice invoice) throws Exception {
        List<InvoiceAlbum> list = new ArrayList<>();
        List<Album> albums = cart.getAlbumsFromCart();

        // Create each InvoiceAlbum object
        for (int i = 0; i < albums.size(); i++) {
            double finalCost = albums.get(i).getListPrice() - albums.get(i).getSalePrice();
            list.add(new InvoiceAlbum());
            list.get(i).setInvoiceAlbumPK(new InvoiceAlbumPK());
            list.get(i).getInvoiceAlbumPK().setAlbumId(albums.get(i).getId());
            list.get(i).getInvoiceAlbumPK().setInvoiceId(invoice.getId());
            list.get(i).setInvoice(invoice);
            list.get(i).setAlbum(albums.get(i));
            list.get(i).setFinalPrice(finalCost);
            invoiceAlbumController.create(list.get(i));
        }
        
        return list;
    }

    /**
     * This method creates a list of the InvoiceTracks, based on the tracks
     * in the cart.
     * 
     * @author Erika Bourque
     * @return  The list of InvoiceTracks
     */
    private List<InvoiceTrack> buildInvoiceTrackList(Invoice invoice) throws Exception {
        List<InvoiceTrack> list = new ArrayList<>();
        List<Track> tracks = cart.getTracksFromCart();

        // Create each InvoiceTrack object
        for (int i = 0; i < tracks.size(); i++) {
            double finalCost = tracks.get(i).getListPrice() - tracks.get(i).getSalePrice();
            list.add(new InvoiceTrack());
            list.get(i).setInvoiceTrackPK(new InvoiceTrackPK());
            list.get(i).getInvoiceTrackPK().setTrackId(tracks.get(i).getId());
            list.get(i).getInvoiceTrackPK().setInvoiceId(invoice.getId());
            list.get(i).setInvoice(invoice);
            list.get(i).setTrack(tracks.get(i));
            list.get(i).setFinalPrice(finalCost);
            invoiceTrackController.create(list.get(i));
        }
        
        return list;
    }
}

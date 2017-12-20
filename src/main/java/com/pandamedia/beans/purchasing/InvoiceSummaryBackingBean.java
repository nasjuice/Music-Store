package com.pandamedia.beans.purchasing;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import persistence.entities.Invoice;

/**
 * This class is the backing bean for the invoice summary page.
 * 
 * @author Erika Bourque
 */
@Named("invoiceSum")
@RequestScoped
public class InvoiceSummaryBackingBean {
    Invoice invoice;
    
    /**
     * Retrieving the invoice from the flash.
     * 
     * @author Erika Bourque
     */
    @PostConstruct
    public void init() {
        invoice = (Invoice) FacesContext.getCurrentInstance().getExternalContext().getFlash().get("invoice");
    }
    
    /**
     * Getter for invoice.
     * 
     * @author Erika Bourque
     * @return The invoice
     */
    public Invoice getInvoice()
    {
        return invoice;
    }
    
    /**
     * Checks if the invoice has been successfully retrieved from the flash.
     * 
     * @author Erika Bourque
     * @return true if the invoice has been retrieved
     */
    public boolean invoiceExists()
    {
        return invoice != null;
    }
}

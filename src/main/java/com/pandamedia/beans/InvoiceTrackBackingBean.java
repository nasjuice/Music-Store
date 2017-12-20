
package com.pandamedia.beans;

import java.io.Serializable;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import persistence.controllers.InvoiceTrackJpaController;
import persistence.controllers.exceptions.RollbackFailureException;
import persistence.entities.InvoiceTrack;
import persistence.entities.InvoiceTrackPK;


/**
 * This class will be used as the invoice track backing bean. It can update,
 * delete and query invoice tracks.
 * @author  Naasir Jusab
 */
@Named("invoiceTrackBacking")
@SessionScoped
public class InvoiceTrackBackingBean implements Serializable{
    
    @Inject
    private InvoiceTrackJpaController invoiceTrackController;
    private InvoiceTrack invoiceTrack;
    @PersistenceContext
    private EntityManager em;
    
    /**
     * This method will return an invoice track if it exists already. Otherwise, 
     * it will return a new invoice track.
     * @return invoice object
     */
    public InvoiceTrack getInvoiceTrack(){
        
        if(invoiceTrack == null){
            invoiceTrack = new InvoiceTrack();
        }
        return invoiceTrack;
    }
    
    
    /**
     * This method will remove an invoice track by searching its embedded id
     * then changing its removal status to 1 and the removal date to the date
     * when the remove was clicked. Then, the controller edits these values
     * so they do not show on the data table.
     * @param invTrackPK InvoiceTrackPK object
     * @return null string make it stay on the same page
     */
    public String removeInvoiceTrack(InvoiceTrackPK invTrackPK)
    {
        InvoiceTrack invTrack = invoiceTrackController.findInvoiceTrack(invTrackPK);
        
        if(invTrack.getRemovalStatus() != 1)
        {
            short i = 1;
            invTrack.setRemovalStatus(i);
            invTrack.setRemovalDate(Calendar.getInstance().getTime());

            try
            {
                invoiceTrackController.edit(invTrack);
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
        return null; 
        
    }
    
    public String addInvoiceTrack(InvoiceTrackPK invTrackPK){
        InvoiceTrack invTrack = invoiceTrackController.findInvoiceTrack(invTrackPK);
        if(invTrack.getRemovalStatus() != 0){
            short removalStatus = 0;
            invTrack.setRemovalStatus(removalStatus);
            invTrack.setRemovalDate(null);
            
            try {
                invoiceTrackController.edit(invTrack);
            } catch (Exception ex) {
                Logger.getLogger(InvoiceTrackBackingBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        return null;
    }
    
    
}

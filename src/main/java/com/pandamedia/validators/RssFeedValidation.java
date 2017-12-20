
package com.pandamedia.validators;

import java.util.Calendar;
import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;

/**
 *
 * @author Naasir
 */
@Named
@RequestScoped
public class RssFeedValidation implements Validator {
    
    @Override
    public void validate(FacesContext context, UIComponent component,
            Object value) {
        if (value == null) 
        {
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "feedCannotBeNull", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
        
        String rssFeed = (String) value;
        
        if(!rssFeed.startsWith("https://") && !rssFeed.startsWith("http://"))
        {
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "feedIsInvalid", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
        
        if(!rssFeed.endsWith(".xml") && !rssFeed.endsWith(".rss"))
            throw new ValidatorException(new FacesMessage("RSS Feed is invalid"));
        
        
               
    }
    
}

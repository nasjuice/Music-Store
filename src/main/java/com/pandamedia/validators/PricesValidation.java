
package com.pandamedia.validators;

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
public class PricesValidation implements Validator{
    
    @Override
    public void validate(FacesContext context, UIComponent component,
            Object value) {
        if (value == null) 
        {
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "priceCannotBeNull", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }

        Double price = (Double) value;
        
        if(price < 0 )
        {
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "priceCannotBeNegative", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }

               
    }
    
}

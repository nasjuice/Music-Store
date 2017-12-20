
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
public class PhoneValidation implements Validator {
    
        
    @Override
    public void validate(FacesContext context, UIComponent component,
            Object value) {
        String cellNum = (String) value;
        if (cellNum != null && cellNum.length() != 0)
         if(!cellNum.matches("^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$"))
         {
             FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "phoneCannotNull", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
         }
        
               
    }
    
}

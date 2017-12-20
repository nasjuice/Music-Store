
package com.pandamedia.validators;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;
import jodd.mail.EmailAddress;

/**
 *
 * @author Naasir
 */
@Named 
@RequestScoped
public class EmailValidation implements Validator {
    
    
    @Override
    public void validate(FacesContext fc, UIComponent c, Object value) {
        EmailAddress email = new EmailAddress((String) value);

        if (!email.isValid()) 
        {
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "emailInvalid", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }
        
    
}

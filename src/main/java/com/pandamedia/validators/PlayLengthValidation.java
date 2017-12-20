
package com.pandamedia.validators;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;
import org.primefaces.component.outputlabel.OutputLabel;

/**
 *
 * @author Naasir 
 */
@Named
@RequestScoped
public class PlayLengthValidation implements Validator {
    
    @Override
    public void validate(FacesContext context, UIComponent component,
            Object value) {
        if (value == null) 
        {
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "playLengthCannotBeNull", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
        
        String playLength = (String) value;
        int colon = playLength.indexOf(":");
        if(colon == -1)
        {
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "playLengthInvalid", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
        
        String firstPart = playLength.substring(0,colon);
        String secondPart = playLength.substring(colon+1);
        
        try
        {
            Integer.parseInt(firstPart);
            Integer.parseInt(secondPart);
        }
        catch(Exception e)
        {
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "playLengthInvalid", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }
    
}

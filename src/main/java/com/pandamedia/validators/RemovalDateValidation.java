
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
import org.primefaces.component.inputtext.InputText;

/**
 *
 * @author Naasir
 */
@Named
@RequestScoped
public class RemovalDateValidation implements Validator{
    
    @Override
    public void validate(FacesContext context, UIComponent component,
            Object value) {
        
        InputText removalField = (InputText)component.findComponent("removalStatus");
        short removalStatus = (short)removalField.getValue();
        if(removalStatus == 1)
        {
            if (value == null) 
            {
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "dateCannotBeNull", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
            }

            Date date = (Date)value;
            Date todaysDate =(Date) Calendar.getInstance().getTime();
            if(date.after(todaysDate))
            {
                FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "dateCannotBeFuture", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
            }
        }
        
        else
        {
            if(value != null)
            {
                FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "dateHasToBeNull", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
            }
        }
               
    }
    
}

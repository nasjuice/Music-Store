package com.pandamedia.validators;

import java.util.Calendar;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author Erika Bourque
 */
@FacesValidator("creditMonthValidator")
public class CreditCardMonthValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        // Must read this value from user interface
        UIInput monthInput = (UIInput) component.findComponent("credit:cardMonth");

        System.out.println("Year: " + value);
        System.out.println("Month: " + monthInput.getLocalValue());
        
        int month = ((Integer) monthInput.getLocalValue());
        int year = ((Integer) value);

        if (!isValidDate(month, year))
        {
            // Date in past
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "cardPastDateError", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }
    
    private boolean isValidDate(int month, int year)
    {
        Calendar current = Calendar.getInstance();
        
        Calendar given = Calendar.getInstance();
        given.set(Calendar.YEAR, year);
        given.set(Calendar.MONTH, month);       
        
        return given.after(current);
    }
    
}

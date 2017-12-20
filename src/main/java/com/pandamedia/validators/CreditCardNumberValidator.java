package com.pandamedia.validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author Erika Bourque
 */
@FacesValidator("creditNumValidator")
public class CreditCardNumberValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String number = value.toString();
        String regex = "^\\d+$";
        
        if (!number.matches(regex))
        {
            // Invalid character
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "cardNumIllegalChar", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
        
        // Visa allows 13, 16, or 19 digits, MasterCard allows 16 digits
        if ((number.length() != 13) && (number.length() != 16) && (number.length() != 19))
        {
            // Incorrect number of digits
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "cardNumWrongNumDigits", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
        
        if (!luhnCheck(number))
        {
            // Luhn check failed
            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "cardNumBadInput", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }

    /**
     * 
     * @author Ken Fogel
     * @param numbers
     * @return 
     */
    private boolean luhnCheck(String numbers)
    {
        int sum = 0;

        for (int i = numbers.length() - 1; i >= 0; i -= 2) {
            sum += Integer.parseInt(numbers.substring(i, i + 1));
            if (i > 0) {
                int d = 2 * Integer.parseInt(numbers.substring(i - 1, i));
                if (d > 9) {
                    d -= 9;
                }
                sum += d;
            }
        }

        return sum % 10 == 0;
    }    
}

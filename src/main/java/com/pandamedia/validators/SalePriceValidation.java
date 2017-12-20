
package com.pandamedia.validators;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.outputlabel.OutputLabel;



/**
 *
 * @author Naasir
 */
@Named
@RequestScoped
public class SalePriceValidation implements Validator {
    
    @Override
    public void validate(FacesContext context, UIComponent component,
            Object value) {
        if (value == null) 
        {
             FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "salePriceCannotBeNull", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
        
        Double salePrice = (Double) value;
        if(salePrice < 0)
        {
             FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "salePriceCannotBeNegative", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
        
        String id = component.getId();
        Double listPrice;
        if(id.equals("salePrice"))
        {
            OutputLabel listField = (OutputLabel) component.findComponent("listPrice");
        
            listPrice = (Double)listField.getValue();    
        }
        
        else
        {
            InputText listField = (InputText) component.findComponent("listPrice");
            
            listPrice = (Double)listField.getValue();
                           
        }
        
        if(salePrice >= listPrice)
        {
             FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "salePriceCannotBeGreaterThanList", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
        
    }
    
    
}

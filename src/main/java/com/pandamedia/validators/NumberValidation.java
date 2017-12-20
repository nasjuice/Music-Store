
package com.pandamedia.validators;


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
public class NumberValidation implements Validator{
    
    @Override
    public void validate(FacesContext context, UIComponent component,
            Object value) {
        if (value == null) 
        {
           FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "numCannotBeNull", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
        
        InputText partOfAlbumText = (InputText)component.findComponent("partOfAlbum");
        
        short partOfAlbumValue = (short)partOfAlbumText.getValue();
        
        int albumTrackNumber = (int) value;
        if(partOfAlbumValue == 1)
        {
            if(albumTrackNumber <= 0 )
            {
                  FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "numNotNegative", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
            }
        }
        
        else
        {
            if(albumTrackNumber != 0)
            {
                            FacesMessage message = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "numNotTrackNumber", null);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
            }
        }

               
    }
    
}

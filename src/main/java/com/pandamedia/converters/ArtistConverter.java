
package com.pandamedia.converters;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;
import persistence.controllers.ArtistJpaController;
import persistence.entities.Artist;

 


/**
 * This class will be used as the artist converter for the primefaces selectOneMenu
 * so that the manager can choose an artist from the list of artists.
 * @author Naasir Jusab
 */
@RequestScoped
@Named("artistConverter")
public class ArtistConverter  implements Converter {
    
    @Inject
    private ArtistJpaController service;
 
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if(value != null && value.trim().length() > 0) {
            try {   
                return service.findArtist(Integer.parseInt(value));
               
            } catch(NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid theme."));
            }
        }
        else {
            return null;
        }
    }
 
    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if(object != null) {
            return String.valueOf(((Artist) object).getId());
        }
        else {
            return null;
        }
    }   
}         

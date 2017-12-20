
package com.pandamedia.converters;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;
import persistence.controllers.AlbumJpaController;
import persistence.entities.Album;

/**
 * This class will be used as the album converter for the primefaces selectOneMenu
 * so that the manager can choose an album from the list of albums.
 * @author Naasir Jusab
 */
@RequestScoped
@Named
public class AlbumConverter implements Converter {
    
    @Inject
    AlbumJpaController service;
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {

        if(value != null && value.trim().length() > 0) {
            try {   
                return service.findAlbum(Integer.parseInt(value));    
            } catch(NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid theme."));
            }
        }
        else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object object) {
        if(object != null) {
            return String.valueOf(((Album) object).getId());
        }
        else {
            return null;
        }
    }   
}        



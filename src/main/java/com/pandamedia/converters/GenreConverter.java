
package com.pandamedia.converters;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;
import persistence.controllers.GenreJpaController;
import persistence.entities.Genre;

/**
 * This class will be used as the genre converter for the primefaces selectOneMenu
 * so that the manager can choose a genre from the list of genres.
 * @author Naasir Jusab
 */
@RequestScoped
@Named("genreConverter")
public class GenreConverter  implements Converter {
    
    @Inject
    private GenreJpaController service;
 
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if(value != null && value.trim().length() > 0) {
            try {   
                return service.findGenre(Integer.parseInt(value));
                
            } catch(NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid theme."));
            }
        }
        else {
            return null;
        }
    }
 
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if(object != null) {
            return String.valueOf(((Genre) object).getId());
        }
        else {
            return null;
        }
    }   
}

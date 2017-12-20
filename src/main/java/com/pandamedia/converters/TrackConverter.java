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
import persistence.controllers.TrackJpaController;
import persistence.entities.Track;

/**
 * This class will be used as the track converter for the primefaces selectOneMenu
 * so that the manager can choose a track from the list of tracks.
 * @author Naasir Jusab
 */
@RequestScoped
@Named
public class TrackConverter implements Converter,Serializable {
    
    @Inject
    TrackJpaController service;
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if(value != null && value.trim().length() > 0) {
            try {   
                return service.findTrack(Integer.parseInt(value));
               
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
            return String.valueOf(((Track) object).getId());
        }
        else {
            return null;
        }
    }
}

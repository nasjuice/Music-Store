
package com.pandamedia.converters;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Inject;
import javax.inject.Named;
import persistence.controllers.RecordingLabelJpaController;
import persistence.entities.RecordingLabel;

/**
 * This class will be used as the recordingLabel converter for the primefaces  
 * selectOneMenu so that the manager can choose a recordingLabel from the list 
 * of recordingLabels.
 * @author Naasir Jusab
 */
@RequestScoped
@Named("recordingLabelConverter")
public class RecordingLabelConverter  implements Converter {
    
    @Inject
    private RecordingLabelJpaController service;
 
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if(value != null && value.trim().length() > 0) {
            try {   
                return service.findRecordingLabel(Integer.parseInt(value));
                
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
            return String.valueOf(((RecordingLabel) object).getId());
        }
        else {
            return null;
        }
    }   
}

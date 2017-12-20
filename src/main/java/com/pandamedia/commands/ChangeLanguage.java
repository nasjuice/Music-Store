package com.pandamedia.commands;

import java.io.Serializable;
import java.util.Locale;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 *
 * @author Pierre Azelart
 */
@Named("ChangeLanguage")
@SessionScoped
public class ChangeLanguage implements Serializable {
    
    private Locale localeValue;
    
    public String frenchAction() {
        FacesContext context = FacesContext.getCurrentInstance();
        localeValue = Locale.CANADA_FRENCH;
        context.getViewRoot().setLocale(localeValue);
        return null;
    }
    public String englishAction() {
        FacesContext context = FacesContext.getCurrentInstance();
        localeValue = Locale.CANADA;
        context.getViewRoot().setLocale(localeValue);
        return null;
    }
    
    public Locale getLocale()
    {
        if (localeValue == null)
        {
            localeValue = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        }
        
        return localeValue;
    }
    
    
}

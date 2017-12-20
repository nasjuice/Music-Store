/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pandamedia.commands;

import java.io.Serializable;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 *
 * @author Pierre Azelart
 */
@Named("searchDropdown")
@SessionScoped
public class SearchDropdown implements Serializable{
    
    private FacesContext context;
    private ResourceBundle msgs;
    private String toDisplay = "Type";
    private String type;

    @PostConstruct
    public void init(){
        try{
            //Default search type
            type = "tracks";
            //Displays chosen type in correct language
            FacesContext context = FacesContext.getCurrentInstance();
            ResourceBundle msgs = ResourceBundle.getBundle("bundles.messages", context.getViewRoot().getLocale());
            String toDisplay = msgs.getString(type);
        }
        catch(java.lang.ClassCastException ex){
        }
    }

    public String getType() {
        return type;
    }

    public String setType(String type) {
        this.type = type;
        
        //Updates the diplayed type to match selected language
        setToDisplay(type);
        if(type.equals("date")){
            return "advanced_search";
        }
        return null;
    }

    public String getToDisplay() {
        return toDisplay;
    }

    public void setToDisplay(String type) {
        context = FacesContext.getCurrentInstance();
        msgs = ResourceBundle.getBundle("bundles.messages", context.getViewRoot().getLocale());
        this.toDisplay = msgs.getString(type);
    }
}

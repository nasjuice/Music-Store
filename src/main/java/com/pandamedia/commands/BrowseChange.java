/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pandamedia.commands;

import com.mysql.jdbc.log.Log;
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
@Named("browseChange")
@SessionScoped
public class BrowseChange implements Serializable{
    
    private FacesContext context;
    private ResourceBundle msgs;
    private String type;

    @PostConstruct
    public void init(){
        try{
            //Default search type
            //Displays chosen type in correct language
            
            FacesContext context = FacesContext.getCurrentInstance();
            ResourceBundle msgs = ResourceBundle.getBundle("bundles.messages", context.getViewRoot().getLocale());
            String toDisplay = msgs.getString("albums");
        }
        catch(java.lang.ClassCastException ex){}
    }
    
    public String browseAlbums(){
        context = FacesContext.getCurrentInstance();
        msgs = ResourceBundle.getBundle("bundles.messages", context.getViewRoot().getLocale());
        this.type = msgs.getString("albums");
        return "browsealbum";
    }
    
    public String browseArtists(){
        context = FacesContext.getCurrentInstance();
        msgs = ResourceBundle.getBundle("bundles.messages", context.getViewRoot().getLocale());
        this.type = msgs.getString("artists");
        return "browseartist";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package com.pandamedia.beans.purchasing;

import com.pandamedia.beans.UserActionBean;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import persistence.entities.Track;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import persistence.entities.Album;
import persistence.entities.Invoice;
import persistence.entities.InvoiceAlbum;
import persistence.entities.InvoiceTrack;

/**
 * This class represents a shopping cart for a customer.
 * 
 * @author Evangelo Glicakis
 * @author Erika Bourque
 */
@Named("cart")
@SessionScoped
public class ShoppingCart implements Serializable {
    private List<Album> albums;
    private List<Track> tracks;
    private UIViewRoot prevPage;
    @Inject
    private UserActionBean user;

    public ShoppingCart() {
        albums = new ArrayList<>();
        tracks = new ArrayList<>();
    }

    /**
     * Gathers the shopping cart album objects and returns them as a list to be
     * displayed in the cart.
     *
     * @author Evangelo Glicakis
     * @author Erika Bourque
     * @return
     */
    public List<Album> getAlbumsFromCart() {
        return albums;
    }

    /**
     * gets the tracks from the cart.
     * 
     * @author Evangelo Glicakis
     * @author Erika Bourque
     * @return
     */
    public List<Track> getTracksFromCart() {
        return tracks;
    }
    /**
     * Calculates the sub total of the tracks and albums costs.
     * 
     * @author Evangelo Glicakis
     * @author Erika Bourque
     * @return 
     */
    public double getSubTotal() {
        double subtotal = 0;
        for (Album a : albums) {
            subtotal += a.getListPrice() - a.getSalePrice();
        }
        for (Track t : tracks) {
            subtotal += t.getListPrice() - t.getSalePrice();
        }
        return subtotal;
    }

    /**
     * returns the amount of items in the cart, to be used by the navigation bar
     * to display the amount of items currently in the cart.
     * 
     * @author Evangelo Glicakis
     * @return
     */
    public String getCartCount() {
        int size = albums.size() + tracks.size();
        if (size > 0) {
            return " ( " + size + " )";
        } else {
            return "";
        }
    }

    /**
     * Checks if both album list and track list are empty.
     * 
     * @author Evangelo Glicakis
     * @return 
     */
    public boolean getIsCartEmpty() {
        return (albums.size() + tracks.size()) == 0;
    }

    /**
     * Removes a particular album from the album list.
     * 
     * @author Evangelo Glicakis
     * @param a 
     */
    public void removeAlbumFromCart(Album a) {
        albums.remove(a);
    }

    /**
     * Removes a particular track from the track list.
     * 
     * @author Evangelo Glicakis
     * @param t 
     */
    public void removeTrackFromCart(Track t) {
        tracks.remove(t);
    }

    /**
     * Sets the UIViewRoot object, to be called when the shopping cart icon in
     * the navigation bar is clicked is clicked.
     *
     * @author Evangelo Glicakis
     * @return
     */
    public String setPrevPage() {
        prevPage = FacesContext.getCurrentInstance().getViewRoot();
        return "cart";
    }

    /**
     * returns the user to the location of the ui where the prevPage object is
     * holding. If the prevPage is null or not defined, return them to the
     * mainpage.
     * 
     * @author Evangelo Glicakis
     */
    public void continueShopping() throws IOException {
        if (prevPage != null) {
            FacesContext.getCurrentInstance().setViewRoot(prevPage);
            FacesContext.getCurrentInstance().renderResponse();
        } else {
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            context.redirect(context.getRequestContextPath() + "/mainpage.xhtml");
            System.out.println(context.getRequestContextPath());
        }

    }

    /**
     * Adds an album to the album list if it not already in
     * the list.
     * 
     * @author Evangelo Glicakis
     * @param album 
     */
    public void addAlbum(Album album) {
        if (!albums.contains(album)) {
            albums.add(album);
        }
    }
    /**
     * Adds track to the shopping cart.
     * If the user adds all the tracks of an album it adds the album automatically
     * to the album list
     * Checks if the track is already in the track list.
     * 
     * @author Evangelo Glicakis
     * @param track 
     */
    public void addTrack(Track track) {
        // if the user already has the album of the track they are trying to add,
        // do not add the track. Super dirty and gross, sorry.
        if (albums.contains(track.getAlbumId())) {
            return;
        }
        // get the album that the track belongs to.
        Album album = track.getAlbumId();
        // list of tracks that are in the album.
        List<Track> album_tracks = new ArrayList();
        if (!tracks.contains(track)) {
            tracks.add(track);
            int i = 0;
            for (Track t : tracks) {
                if (t.getAlbumId().getId() == album.getId()) {
                    album_tracks.add(t);
                    i++;
                }
            }
            if(tracks.containsAll(album.getTrackList())){
                tracks.removeAll(album_tracks);
                albums.add(album);
            }
        }

    }

    /**
     * Empties both lists of all items.
     * 
     * @author Erika Bourque
     */
    public void clearCart() {
        albums = new ArrayList<>();
        tracks = new ArrayList<>();
    }
    
    /**
     * This method verifies the album and track lists to
     * see if any have already been purchased by the user.
     * 
     * @author Erika Bourque
     * @return  The list of FacesMessages for all previous purchased items.
     */
    private List<FacesMessage> itemsPreviouslyPurchased()
    {
        List<FacesMessage> list = new ArrayList<>();
        
        if (!albums.isEmpty())
        {
            checkAlbums(list);
        }
        
        if (!tracks.isEmpty())
        {
            checkTracks(list);
        }
        
        return list;
    }
    
    /**
     * This method checks if the albums in the cart have already been 
     * purchased by the logged in user, and creates a message for each 
     * previously purchased album.
     * 
     * @author Erika Bourque
     * @param list  The list to add the messages to
     */
    private void checkAlbums(List<FacesMessage> list)
    {
        List<Album> purchased = getPurchasedAlbums();
               
        // Comparing each album in cart to see if has already been purchased
        for(Album a : albums)
        {
            if (purchased.contains(a))
            {
                // Adding message for purchased album
                list.add(com.pandamedia.utilities.Messages.getMessage(
                        "bundles.messages", "prevPurchasedAlbum", new Object[]{a.getTitle()}));
            }
        }
    }
    
    /**
     * This method checks if the tracks in the cart have already been 
     * purchased by the logged in user, either individually or as part of an 
     * album, and creates a message for each previously purchased track.
     * 
     * @author Erika Bourque
     * @param list  The list to add the messages to
     */
    private void checkTracks(List<FacesMessage> list)
    {
        List<Track> purchasedTracks = getPurchasedTracks();
        List<Album> purchasedAlbums = getPurchasedAlbums();
        
        // Comparing each track
        for(Track t : tracks)
        {
            // Making sure track nor its album has not been purchased
            if (purchasedTracks.contains(t) || purchasedAlbums.contains(t.getAlbumId()))
            {
                // Adding message for purchased track
                list.add(com.pandamedia.utilities.Messages.getMessage(
                        "bundles.messages", "prevPurchasedTrack", new Object[]{t.getTitle()}));
            }
        }
    }
    
    /**
     * This method retrieves all the user's purchased albums.
     * 
     * @author Erika Bourque
     * @return  The list of purchased albums
     */
    private List<Album> getPurchasedAlbums()
    {
        List<Album> list = new ArrayList<>();
        
        // Getting all user's invoices
        for(Invoice i : user.getCurrUser().getInvoiceList())
        {
            // Getting all user's invoice albums
            for(InvoiceAlbum ia : i.getInvoiceAlbumList())
            {
                // Adding each album to purchased list
                list.add(ia.getAlbum());
            }
        }
        
        return list;
    }
    
    /**
     * This method retrieves all the user's purchased tracks.
     * 
     * @author Erika Bourque
     * @return  The list of purchased tracks
     */
    private List<Track> getPurchasedTracks()
    {
        List<Track> list = new ArrayList<>();
        
        // Getting all user's invoices
        for(Invoice i : user.getCurrUser().getInvoiceList())
        {
            // Getting all user's invoice tracks
            for(InvoiceTrack it : i.getInvoiceTrackList())
            {
                // Adding each track to purchased list
                list.add(it.getTrack());
            }
        }
        
        return list;
    }
    
    public String verifyCartContents()
    {   
        //set the prev page in the user action bean so they get redirected once they log in.

        user.setPrevPageLogin();

        // Remains null if warnings exist
        String page = null;
        
        // Must make sure user is logged in before checking previous purchases
        if (user.isLogin())
        {
            List<FacesMessage> warnings = itemsPreviouslyPurchased();
            if (warnings.isEmpty())
            {
                // Redirect to finalization page
                page = "finalization";
            }
            else
            {
                // Add warnings to cart page
                FacesContext context = FacesContext.getCurrentInstance();
                
                for(FacesMessage msg : warnings)
                {
                    context.addMessage(null, msg);
                }
            }
        }
        else
        {
            // Force user to login
            page = "login";
        }
        
        return page;
    }
}
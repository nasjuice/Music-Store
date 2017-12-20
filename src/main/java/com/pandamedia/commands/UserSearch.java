package com.pandamedia.commands;

import com.pandamedia.beans.AlbumBackingBean;
import com.pandamedia.beans.ArtistBackingBean;
import com.pandamedia.beans.TrackBackingBean;
import java.io.Serializable;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import persistence.entities.Album;
import persistence.entities.Artist;
import persistence.entities.Track;

/**
 *
 * @author Pierre Azelart
 */
@Named("Search")
//Doesnt update the search result list when changing type in dropdown
//If reducing scope is needed, could use ajax instead
@SessionScoped
public class UserSearch implements Serializable {

    //Error string
    private String notFound;
    //Necessary Lists for different types
    private List<Track> trackResultsList;
    private List<Album> albumResultsList;
    private List<Artist> artistResultsList;
    //Stores the last type researched by user
    private String typeSearched;
    private String parameters;
    private Date date1;
    private Date date2;
    private String paramDate1;
    private String paramDate2;

    @Inject
    private AlbumBackingBean albb;
    @Inject
    private ArtistBackingBean arbb;
    @Inject
    private TrackBackingBean tbb;
    @Inject
    private SearchDropdown sd;

    @PersistenceContext
    private EntityManager em;

    /**
     * Clears lists if not empty (Because SessionScoped)
     *
     *
     */
    private void reset() {
        if (trackResultsList != null && !trackResultsList.isEmpty()) {
            trackResultsList.clear();
        }
        if (albumResultsList != null && !albumResultsList.isEmpty()) {
            albumResultsList.clear();
        }
        if (artistResultsList != null && !artistResultsList.isEmpty()) {
            artistResultsList.clear();
        }
        notFound = "";
    }

    /**
     * Returns true if given query does not return empty or null else returns
     * false and sets 404 as error message
     *
     * @param query
     * @return
     */
    private boolean errorCheck(TypedQuery query) {
        if (query.getResultList() != null && !query.getResultList().isEmpty()) {
            return true;
        } else {
            //Displays error
            notFound = "404"; //if improved from 404, needs bundle
            return false;
        }
    }

    /**
     * Called when search button is pressed. Executes the correct search
     * function depending on selected type
     *
     * @return
     */
    public String executeSearch() throws Exception {
        String str = sd.getType();
        typeSearched = str;
        reset();
        switch (str) {
            case "tracks":
                searchTracks();
                break;
            case "albums":
                searchAlbums();
                break;
            case "artists":
                searchArtists();
                break;
            case "date":
                searchDate();
                break;
        }
        try {
            if (trackResultsList.size() == 1) {
                return tbb.trackPage(trackResultsList.get(0));
            }

            if (albumResultsList.size() == 1) {
                return albb.albumPage(albumResultsList.get(0));
            }
            if (artistResultsList.size() == 1) {
                return arbb.artistPage(artistResultsList.get(0));
            }
        } catch (NullPointerException ex) {

        }
        return "search";

    }

    private void searchTracks() {
        //Creates query that returns a list of tracks with a name relevant to "parameters"
        String q = "SELECT t FROM Track t WHERE t.title LIKE :var";
        TypedQuery<Track> query = em.createQuery(q, Track.class);
        query.setParameter("var", "%" + parameters + "%");

        if (errorCheck(query)) {
            trackResultsList = query.getResultList();
        }
    }

    private void searchAlbums() {
        //Creates query that returns a list of albums with a name relevant to "parameters"
        String q = "SELECT a FROM Album a WHERE a.title LIKE :var";
        TypedQuery<Album> query = em.createQuery(q, Album.class);
        query.setParameter("var", "%" + parameters + "%");
        if (errorCheck(query)) {
            albumResultsList = query.getResultList();
        }

    }

    private void searchArtists() {
        //Creates query that returns a list of artists with a name relevant to "parameters"
        String q = "SELECT a FROM Artist a WHERE a.name LIKE :var";
        TypedQuery<Artist> query = em.createQuery(q, Artist.class);
        query.setParameter("var", "%" + parameters + "%");
        if (errorCheck(query)) {
            artistResultsList = query.getResultList();
        }
    }

    private void searchDate() throws Exception {
        //Creates query that returns a list of tracks with a release date relevant to "parameters"

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        if (paramDate1 != null && paramDate2 != null) {
            if (!paramDate1.isEmpty() && !paramDate2.isEmpty()) {
                try {
                    date1 = df.parse(paramDate1);
                    date2 = df.parse(paramDate2);
                } catch (Exception ex) {
                }

                String q1 = "SELECT a FROM Album a WHERE a.releaseDate > :from AND a.releaseDate < :until";
                TypedQuery<Album> query1 = em.createQuery(q1, Album.class);

                query1.setParameter("from", date1);
                query1.setParameter("until", date2);
                if (errorCheck(query1)) {
                    albumResultsList = query1.getResultList();
                }
                String q2 = "SELECT a FROM Track a WHERE a.releaseDate > :from AND a.releaseDate < :until";
                TypedQuery<Track> query2 = em.createQuery(q2, Track.class);

                query2.setParameter("from", date1);
                query2.setParameter("until", date2);
                if (errorCheck(query2)) {
                    trackResultsList = query2.getResultList();
                }
            }
        }
    }

    /*Getters and Setters*/
    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;

    }

    public String getParamDate1() {
        return paramDate1;
    }

    public void setParamDate1(String paramDate1) {
        this.paramDate1 = paramDate1;
    }

    public String getParamDate2() {
        return paramDate2;
    }

    public void setParamDate2(String paramDate2) {
        this.paramDate2 = paramDate2;
    }

    public List getTrackResultsList() {
        return trackResultsList;
    }

    public void setTrackResultsList(List trackResultsList) {
        this.trackResultsList = trackResultsList;
    }

    public List getAlbumResultsList() {
        return albumResultsList;
    }

    public void setAlbumResultsList(List albumResultsList) {
        this.albumResultsList = albumResultsList;
    }

    public List getArtistResultsList() {
        return artistResultsList;
    }

    public void setArtistResultsList(List artistResultsList) {
        this.artistResultsList = artistResultsList;
    }

    public String getNotFound() {
        return notFound;
    }

    public void setNotFound(String notFound) {
        this.notFound = notFound;
    }

    public String getTypeSearched() {
        return typeSearched;
    }

    public void setTypeSearched(String typeSearched) {
        this.typeSearched = typeSearched;
    }
}

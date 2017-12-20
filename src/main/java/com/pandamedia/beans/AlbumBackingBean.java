package com.pandamedia.beans;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import persistence.controllers.AlbumJpaController;
import persistence.entities.Album;
import persistence.entities.Album_;
import persistence.entities.Artist;
import persistence.entities.Genre;
import persistence.entities.InvoiceAlbum_;
import persistence.entities.InvoiceTrack_;
import persistence.entities.Invoice_;

/**
 * This class will be used as the album backing bean. It can create, update,
 * delete and query albums.
 * @author Evan Glicakis, Naasir Jusab
 */
@Named("albumBacking")
@SessionScoped
public class AlbumBackingBean implements Serializable{
    
    @Inject
    private AlbumJpaController albumController;
    @Inject
    private ClientTrackingBean clientTracking;
    private Album album;
    private List<Album> albums;
    private List<Album> filteredAlbums;
    @PersistenceContext
    private EntityManager em;
    private String genreString;
    private List<Album> genrelist;
    private int albumid;
    private boolean isAlbumSale = true;
    
    
    
    public AlbumBackingBean(){
        genrelist = new ArrayList();
    }
    
    
    
    /**
     * This method will return all the albums in a list so it can be displayed
     * on the data table.
     * @return all albums in the database
     */
    public List<Album> getAlbums()
    {
        return albums;
    }
    
    /**
     * This method will set a list of albums to make changes to the current
     * list of all albums.
     * @param albums all albums in the database
     */
    public void setAlbums(List<Album> albums)
    {
        this.albums = albums;
    }
    
    /**
     * This method will return an album if it exists already. Otherwise, it will
     * return a new album.
     * @return album object
     * @author Evan G.
     */
    public Album getAlbum(){
        if(album == null){
            album = new Album();
        }
        return album;
    }

    /**
     * This method will return the ID of the album.
     * @return album id
     */
    public int getAlbumid() {
        return albumid;
    }

    /**
     * This method will change the ID of the album.
     * @param albumid 
     */
    public void setAlbumid(int albumid) {
        this.albumid = albumid;
    }
    
    /**
     * This method will change the current album object.
     * @param album new album object
     */
    public void setAlbum(Album album)
    {
        this.album = album;
    }
    

    public List<Album> getGenrelist() {
        return genrelist;
    }

    public void setGenrelist(List<Album> genrelist) {
        this.genrelist = genrelist;
    }

    public String getGenreString() {
        return genreString;
    }

    public void setGenreString(String genreString) {
        this.genreString = genreString;
    }

    public boolean isIsAlbumSale() {
        return isAlbumSale;
    }
    
    
    
    /**
     * sets the album variable and returns the string of the url to the album page.
     * persists the client tracking, either sets the users last seached genre or
     * writes the genre to a cookie.
     * @param a
     * @return 
     * @author Evan G.
     */
    public String albumPage(Album a){
        this.album = a;
//        System.out.println("" + a.getId() +"\n" + a.getTitle() +"\n" + a.getArtistId().getName());
        // persist the searched genre to the suggested
        clientTracking.peristTracking(a.getGenreId());
        return "album";
    }
    /**
     * Returns a list of albums that are on sale.
     * Where in the database the sale_price column is not equal to 0
     * @return 
     * @author Evan G.
     */
    public List<Album> getSaleAlbums(){
        String q = "SELECT a FROM Album a WHERE a.salePrice != 0";
        TypedQuery<Album> query = em.createQuery(q, Album.class);
        if(query.getResultList().isEmpty()){
            isAlbumSale = false; //there are no albums on sale, do not render  
        }
        return query.getResultList();
        
    }
    /**
     * Gets the top selling albums of the current week.
     * @return 
     * @author Evan G. Ripped from Erika's report backing bean and modified.
     */
    public List<Album> getPopularAlbums(){
        Date startDate = new Date(); //get current date
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        System.out.println("endDate = "+ cal.getTime());
        System.out.println("startDate= "+startDate);
        Date endDate = cal.getTime();
        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Album> albumRoot = query.from(Album.class);
        Join invoiceAlbumJoin = albumRoot.join(Album_.invoiceAlbumList);
        Join invoiceJoin = invoiceAlbumJoin.join(InvoiceAlbum_.invoice);
        query.multiselect(cb.sum(invoiceAlbumJoin.get(InvoiceAlbum_.finalPrice)), albumRoot);
        query.groupBy(albumRoot);

        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.between(invoiceJoin.get(Invoice_.saleDate).as(Date.class), endDate, startDate));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        predicates.add(cb.equal(invoiceAlbumJoin.get(InvoiceTrack_.removalStatus), 0));
        predicates.add(cb.equal(albumRoot.get(Album_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        // Order by clause
        query.orderBy(cb.desc(cb.sum(invoiceAlbumJoin.get(InvoiceAlbum_.finalPrice))));
        
        List<Album> albums = new ArrayList();
        TypedQuery<Object[]> typedQuery = em.createQuery(query).setMaxResults(6);
        List<Object[]> l = typedQuery.getResultList();
        for(Object[] o: l){
//            System.out.println(((Album)o[1]).getTitle());
            albums.add((Album)o[1]);//retrieve the album id from the multiselect and cast the object, from id to album object.
        }
        return albums;
        
    }
    
    /**
     * Finds the album from its id.
     * This method does not seem to be used since we changed the way we go from
     * page to album page. Done through server navigation as opposed to
     * view params.
     * @return album object
     * @author Evan G.
     */
    public Album findAlbumById(){
        //album = albumController.findAlbum(id); // questionable, do I set just the album variable and return void?
        // the purpose is to hold onto the ablum object so I don't have to keep htting the db everytime I want to use it.
        FacesContext context = FacesContext.getCurrentInstance();
        String album_id = context.getExternalContext().getRequestParameterMap()
                .get("albumId");
        int id = Integer.parseInt(album_id);
        album = albumController.findAlbum(id);
        return albumController.findAlbum(id);
        
    }
    
    /**
     * returns a list of the latest (5) released albums/recently added to the database.
     * @return 
     * @author Evan G.
     */
    public List<Album> getLatestAlbums(){
        String q = "SELECT a FROM Album a ORDER BY a.releaseDate DESC";
        TypedQuery<Album> query =  em.createQuery(q, Album.class).setMaxResults(5);
        return query.getResultList();
        
    }
    /**
     * Searches the database with the genre key term returns a list of albums.
     * It uses a global variable called genreString to get the albums specific
     * to that key.
     * 
     * from clienttracking.
     * @return 
     * @author Evan G
     */
    public List<Album> getAlbumFromGenre(){
        System.out.println(genreString);
        if(genreString == null){
            return null;
        }
        int genre_id = getGenreId(genreString);
        String q = "SELECT a FROM Album a WHERE a.genreId.id = :genre_id";
        TypedQuery<Album> query = em.createQuery(q, Album.class);
        query.setParameter("genre_id", genre_id);
        return query.getResultList();
    }
    /**
     * Gets the suggested albums for the album page, matching similar albums 
     * based on the genre of the current album.
     * @param genre
     * @return 
     * @author Evan G.
     */
    public List<Album> getSuggestedAlbums(String genre){
        genreString = genre;
        List<Album> list = getAlbumFromGenre();
        // if the list contains a reference to the current album object, remove it
        // from the suggested list.
        if(list.contains(album)){
            list.remove(album);
        }
        return list;
    }
    /**
     * Gets a list of albums from an specified artist object.
     * @author Evan G.
     * @param a
     * @return 
     */
    public List<Album> albumsFromArtist(Artist a){
        if(a != null){
            String q = "SELECT a FROM Album a WHERE a.artistId.id = :artist_id";
            TypedQuery<Album> query = em.createQuery(q, Album.class);
            query.setParameter("artist_id", a.getId());
            return query.getResultList();
        }
        return null;
    }
    /**
     * private helper method used by the getAlbumsFromGenre method, its purpose is
     * to return the genre id of the genre name as a string.
     * @param genre
     * @return id -- genre_id
     */
    private int getGenreId(String genre){
        String q = "SELECT g FROM Genre g WHERE g.name = :name";
        TypedQuery<Genre> query = em.createQuery(q, Genre.class);
        query.setParameter("name", genre);
        //return query.getResultList().get(0).getId();//this should be query.getSingleResult, however, since we have like 5 genres with the same name with the 
        // test data, we get a list and get the first result, test data should have been sanitized.
        return query.getSingleResult().getId();
    }
    
    /**
     * This method will add an album that has been removed. It will change
     * the removal status to 0 which means that it is available for purchase.
     * 1 means that it is not available for purchase. It will set the removal 
     * date to null since it has not been removed. The return type null
     * should refresh the page. At the end, the album is set to null so that it  
     * does not stay in session scoped and the filtered albums are regenerated. 
     * The return type null should make it stay on the same page.
     * @param id of the album that will be added
     * @return null make it stay on the same page
     */
    public String addItem(Integer id)
    {
        album = albumController.findAlbum(id);
        if(album.getRemovalStatus() != 0)
        {
            short i = 0;
            album.setRemovalStatus(i);
            album.setRemovalDate(null);

            try
            {
                albumController.edit(album);      
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
        this.album = null;
        System.out.println(getAll().get(0).getRemovalStatus());
        this.filteredAlbums = albumController.findAlbumEntities();
        System.out.println(filteredAlbums.get(0).getRemovalStatus());
        return null; 
    }
    
    /**
     * This method will remove an album that has been added. It will change
     * the removal status to 1 which means that it is not available for purchase.
     * 0 means that it is available for purchase. It will set the removal 
     * date to the date when you clicked on the remove. At the end, the album is   
     * set to null so that it does not stay in session scoped and the filtered  
     * albums are regenerated. The return type null should make it stay on the
     * same page.
     * @param id of the album that will be removed
     * @return null make it stay on the same page
     */
    public String removeItem(Integer id)
    {       
        album = albumController.findAlbum(id);
        if(album.getRemovalStatus() != 1)
        {
            short i = 1;
            album.setRemovalStatus(i);
            album.setRemovalDate(Calendar.getInstance().getTime());
            try
            {
                albumController.edit(album);   
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Remove: " + album.getRemovalStatus());
        this.album = null;
        this.filteredAlbums = albumController.findAlbumEntities();
        return null; 
    }
    
    /**
     * This method will set the album so that when the editAlbum.xhtml loads.
     * The fields of the page will have values already. All the manager has to do 
     * is change the values. The id will make sure that the right album is being 
     * edited and the return type will display the edit page for the album.
     * @param id of an album that will be edited
     * @return string that is the edit page for an album
     */
    public String loadEditForIndex(Integer id)
    {
        this.album = albumController.findAlbum(id);        
        return "maneditalbum";
    }
    
    /**
     * This method will be called to edit an album. At the end, the album is   
     * set to null so that it does not stay in session scoped and the filtered  
     * albums are regenerated.
     * @return string that is the inventory page
     */
    public String edit()
    {
        try
        {
            albumController.edit(album);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        this.album = null;
        this.filteredAlbums = albumController.findAlbumEntities();
        return "manindex";
    }
    
    /**
     * This method will set a list of filtered albums to change the current
     * list of filtered albums.
     * @param filteredAlbums list of filtered albums
     */
    public void setFilteredAlbums(List<Album> filteredAlbums)
    {
        this.filteredAlbums = filteredAlbums;
    }
    
    /**
     * This method will return a list of filtered albums so that the manager
     * can make searches on albums.
     * @return list of filteredAlbums
     */
    public List<Album> getFilteredAlbums()
    {
        return this.filteredAlbums;
    }
    
    /**
     * This method will return all the albums in the database so it can be
     * displayed on the data table.
     * @return list of albums
     */
    public List<Album> getAll()
    {
        return albumController.findAlbumEntities();
    }
    
    /**
     * This method will set the album so that when the editSalesAlbum.xhtml loads.
     * The fields of the page will have values already. All the manager has to  
     * do is change the values. The id will make sure that the right album is
     * being edited and the return type will display the edit page for the 
     * album.
     * @param id of the album that will be edited
     * @return string that represents the page where the sales of an album
     * can be edited
     */
    public String loadEditForSales(Integer id)
    {
        this.album = albumController.findAlbum(id);        
        return "maneditsalesalbum";
    }
    
    /**
     * This method will edit the sales of an album, if the sale price is less
     * than the list price. Otherwise, it will just refresh the page until the
     * manager puts a value where the sale price is less than the list price.
     * At the end, the album is set to null so that it does not stay in session  
     * scoped and the filtered albums are regenerated.
     * @return string that is the salesPage.xhtml
     * 
     */
    public String editSales()
    {
        double salePrice = album.getSalePrice();
        double listPrice = album.getListPrice();
        
        //Add a popup msg
        if(salePrice >= listPrice)
          return null;
        
        else
        {
            try
            { 
                albumController.edit(album); 
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
            this.album = null;
            this.filteredAlbums = albumController.findAlbumEntities();
            return "mansales";
        }
    }
    
    /**
     * This method will be called to create an album. At the end, the album is   
     * set to null so that it does not stay in session scoped and the filtered 
     * albums are regenerated.   
     * @return string that is the inventory page
     */
    public String create() 
    {
        try
        {
            albumController.create(album);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        
        this.album = null;
        this.filteredAlbums = albumController.findAlbumEntities();        
        return "manindex";
    }
    
    /**
     * This method is used to return back to the manager home page. Also, the 
     * album is set to null so that it does not stay in session scoped and the
     * filtered albums are regenerated. 
     * @return manager home page
     */
    public String back()
    {
        this.album = null;
        this.filteredAlbums = albumController.findAlbumEntities();
        return "manindex";
    }
    
    /**
     * This method is used to return back to the manager sales page. Also, the  
     * album is set to null so that it does not stay in session scoped and the
     * filtered albums are regenerated. 
     * @return manager sales page
     */
    public String backSales()
    {
        this.album = null;
        this.filteredAlbums = albumController.findAlbumEntities();        
        return "mansales";
    }
    
    /**
     * This method is used to get the total sales of an album to this date. The
     * number formatter is used to make the sales only two digits after the 
     * decimal point and if there are no sales made by the album then 0 is
     * returned.
     * @param id of the album whose sales will be displayed
     * @return string that is the sales of the album
     */
    public String getAlbumSales(Integer id) 
    {

        // Query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Double> query = cb.createQuery(Double.class);
        Root<Album> albumRoot = query.from(Album.class);
        Join invoiceAlbumJoin = albumRoot.join(Album_.invoiceAlbumList);
        Join invoiceJoin = invoiceAlbumJoin.join(InvoiceTrack_.invoice);
        query.select(cb.sum(invoiceAlbumJoin.get(InvoiceAlbum_.finalPrice)));
        query.groupBy(albumRoot.get(Album_.id));

        // Where clause
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(albumRoot.get(Album_.id), id));
        predicates.add(cb.equal(invoiceJoin.get(Invoice_.removalStatus), 0));
        predicates.add(cb.equal(invoiceAlbumJoin.get(InvoiceTrack_.removalStatus), 0));
        predicates.add(cb.equal(albumRoot.get(Album_.removalStatus), 0));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        TypedQuery<Double> typedQuery = em.createQuery(query);
        NumberFormat formatter = new DecimalFormat("#0.00"); 
        
        if(typedQuery.getResultList().size() == 0)
            return "0.0";
        else
            return formatter.format(typedQuery.getResultList().get(0));
    }
    
    public String loadCreateAlbum()
    {
        this.album = new Album();        
        return "manaddalbum";
    }
}

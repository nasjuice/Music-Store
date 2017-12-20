
package com.pandamedia.beans;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import persistence.controllers.RecordingLabelJpaController;
import persistence.entities.RecordingLabel;


/**
 * This class will be used as the recordingLabel backing bean. It is used as a 
 * means of getting recordingLabels and querying them.
 * @author Naasir Jusab
 */
@Named("recordingLabelBacking")
@SessionScoped
public class RecordingLabelBackingBean implements Serializable {
    @Inject
    private RecordingLabelJpaController recordingLabelController;
    private RecordingLabel recordingLabel;
    @PersistenceContext
    private EntityManager em;
    
    /**
     * This method will return a recording label if it exists already. Otherwise, 
     * it will return a new recording label object.
     * @return recording label object
     */
    public RecordingLabel getRecordingLabel(){
        if(recordingLabel == null){
            recordingLabel = new RecordingLabel();
        }
        return recordingLabel;
    }
    
    /**
     * Finds the RecordingLabel from its id.
     * @param id of the recording label
     * @return recording label object
     */
    public RecordingLabel findRecordingLabelById(int id){
        return recordingLabelController.findRecordingLabel(id); 
    }
    
    /**
     * This method will return all the recording labels in the database so it 
     * can be displayed on the data table.
     * @return list of all the recording labels
     */
    public List<RecordingLabel> getAll()
    {
        return recordingLabelController.findRecordingLabelEntities();
    }
    
}

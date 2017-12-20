
package persistence.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Erika Bourque
 */
@Entity
@Table(name = "survey")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Survey.findAll", query = "SELECT s FROM Survey s")
    , @NamedQuery(name = "Survey.findById", query = "SELECT s FROM Survey s WHERE s.id = :id")
    , @NamedQuery(name = "Survey.findByQuestion", query = "SELECT s FROM Survey s WHERE s.question = :question")
    , @NamedQuery(name = "Survey.findByAnswerA", query = "SELECT s FROM Survey s WHERE s.answerA = :answerA")
    , @NamedQuery(name = "Survey.findByAnswerB", query = "SELECT s FROM Survey s WHERE s.answerB = :answerB")
    , @NamedQuery(name = "Survey.findByAnswerC", query = "SELECT s FROM Survey s WHERE s.answerC = :answerC")
    , @NamedQuery(name = "Survey.findByAnswerD", query = "SELECT s FROM Survey s WHERE s.answerD = :answerD")
    , @NamedQuery(name = "Survey.findByVotesA", query = "SELECT s FROM Survey s WHERE s.votesA = :votesA")
    , @NamedQuery(name = "Survey.findByVotesB", query = "SELECT s FROM Survey s WHERE s.votesB = :votesB")
    , @NamedQuery(name = "Survey.findByVotesC", query = "SELECT s FROM Survey s WHERE s.votesC = :votesC")
    , @NamedQuery(name = "Survey.findByVotesD", query = "SELECT s FROM Survey s WHERE s.votesD = :votesD")})
public class Survey implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "question")
    private String question;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "answer_a")
    private String answerA;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "answer_b")
    private String answerB;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "answer_c")
    private String answerC;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "answer_d")
    private String answerD;
    @Basic(optional = false)
    @NotNull
    @Column(name = "votes_a")
    private int votesA;
    @Basic(optional = false)
    @NotNull
    @Column(name = "votes_b")
    private int votesB;
    @Basic(optional = false)
    @NotNull
    @Column(name = "votes_c")
    private int votesC;
    @Basic(optional = false)
    @NotNull
    @Column(name = "votes_d")
    private int votesD;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "surveyId")
    private List<FrontPageSettings> frontPageSettingsList;

    public Survey() {
    }

    public Survey(Integer id) {
        this.id = id;
    }

    public Survey(Integer id, String question, String answerA, String answerB, String answerC, String answerD, int votesA, int votesB, int votesC, int votesD) {
        this.id = id;
        this.question = question;
        this.answerA = answerA;
        this.answerB = answerB;
        this.answerC = answerC;
        this.answerD = answerD;
        this.votesA = votesA;
        this.votesB = votesB;
        this.votesC = votesC;
        this.votesD = votesD;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswerA() {
        return answerA;
    }

    public void setAnswerA(String answerA) {
        this.answerA = answerA;
    }

    public String getAnswerB() {
        return answerB;
    }

    public void setAnswerB(String answerB) {
        this.answerB = answerB;
    }

    public String getAnswerC() {
        return answerC;
    }

    public void setAnswerC(String answerC) {
        this.answerC = answerC;
    }

    public String getAnswerD() {
        return answerD;
    }

    public void setAnswerD(String answerD) {
        this.answerD = answerD;
    }

    public int getVotesA() {
        return votesA;
    }

    public void setVotesA(int votesA) {
        this.votesA = votesA;
    }

    public int getVotesB() {
        return votesB;
    }

    public void setVotesB(int votesB) {
        this.votesB = votesB;
    }

    public int getVotesC() {
        return votesC;
    }

    public void setVotesC(int votesC) {
        this.votesC = votesC;
    }

    public int getVotesD() {
        return votesD;
    }

    public void setVotesD(int votesD) {
        this.votesD = votesD;
    }

    @XmlTransient
    public List<FrontPageSettings> getFrontPageSettingsList() {
        return frontPageSettingsList;
    }

    public void setFrontPageSettingsList(List<FrontPageSettings> frontPageSettingsList) {
        this.frontPageSettingsList = frontPageSettingsList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Survey)) {
            return false;
        }
        Survey other = (Survey) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "persistence.entities.Survey[ id=" + id + " ]";
    }
    
}

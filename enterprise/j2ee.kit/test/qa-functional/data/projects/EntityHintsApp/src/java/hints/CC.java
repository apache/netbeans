/*
 * CC.java
 *
 * Created on June 19, 2006, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hints;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author jindra
 */
@Entity
public class CC implements Serializable {

    CC cc;
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    /**
     * Creates a new instance of CC
     */
    public CC() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CC)) {
            return false;
        }
        CC other = (CC)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) { return false; }
        return true;
    }

    public String toString() {
        return "hints.C[id=" + id + "]";
    }
    
}

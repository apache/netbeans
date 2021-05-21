/*
 * B.java
 *
 * Created on June 19, 2006, 6:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hints;

import java.io.Serializable;
import java.util.List;
import java.util.Queue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author jindra
 */
@Entity
public class B implements Serializable {

    A a;
     
    List<A> listA;
  
    Queue<A> queueA;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    /** Creates a new instance of B */
    public B() {
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
        if (!(object instanceof B)) {
            return false;
        }
        B other = (B)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) { return false; }
        return true;
    }

    public String toString() {
        return "hints.B[id=" + id + "]";
    }
    
}

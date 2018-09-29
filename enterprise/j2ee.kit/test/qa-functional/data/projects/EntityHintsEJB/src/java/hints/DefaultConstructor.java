/*
 * NewEntity.java
 * 
 * Created on Jul 26, 2007, 2:52:23 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hints;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author jindra
 */
@Entity
public class DefaultConstructor implements Serializable {

    public DefaultConstructor(Long id){
        this.id = id;
    }
    
    @Id
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package other;

import javax.inject.Named;
import javax.enterprise.context.Dependent;

/**
 *
 * @author vriha
 */
@Named(value = "otherBean")
@Dependent
public class OtherBean {

    /**
     * Creates a new instance of OtherBean
     */
    public OtherBean() {
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

/**
 *
 * @author vriha
 */
@FacesComponent(createTag = true, tagName = "myComponent")
public class NewFacesComponent1 extends UIComponentBase {
    
    @Override
    public String getFamily() {
        return "test";
    }
    
}

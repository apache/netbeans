/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.test.java.hints.TooStrongCastTest;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author sdedic
 */
public class ReturnType {
    public Collection castReturnType() {
        Object l  = null;
        return (List)l;  
    }
    
    public Collection castReturnTypeFromDeep() {
        Object l = null;
        while (true) {
            try {
               if (false) {
                   return (List)l;
               } 
            } catch (Exception ex) {
                
            }
        }
    }
    
    public List castReturnTypeOK() {
        Collection l  = null;
        return (List)l;  
    }
    
    public Collection<String> castReturnGenericStuff() {
        Object l = null;
        return (List<String>)l;
    }
    
    public <T extends Cloneable> Collection<T> castReturnGenericTypeparam() {
        Object l = null;
        return (List<T>)l;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.hints.regex.parser;

/**
 *
 * @author sandeemi
 */
public class Repetition extends RegEx {
    
    private RegEx internal;
    
    public Repetition(RegEx internal) {
        this.internal = internal;
    }

    public RegEx getInternal() {
        return internal;
    }
    
}

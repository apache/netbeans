/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.tools.generator;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import org.openide.util.NbBundle;

/**
 * This is a on change validator giving a user color and tooltip
 * feedback on entering invalid value.
 * It cen also act as a ComboBoxEditor.
 *
 * @serial The serialization is not implemented.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class ValidatingTextField extends JTextField implements ComboBoxEditor {

    private static final long serialVersionUID = 23746913L;
    
    private transient Validator validator = null;
    
    private transient String tooltip = null;  // original tooltip that is overwritten by validation tooltip

    private transient DocumentListener adapter = null;
    
    public ValidatingTextField() {
        
        // let ENTER pushes default button
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        Keymap map = getKeymap();
        map.removeKeyStrokeBinding(enter);
        
    }
    
    /**
     * Set new validator that will be used for test input value validity.
     * @param validator or null if validation should be switched off.
     */
    public void setValidator(Validator validator) {

        Validator old = this.validator;
        
        this.validator = validator;
        
        if (old == null && validator != null) {
        
            // attach very simple validation
            
            adapter = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { feedback(); }
                public void removeUpdate(DocumentEvent e) { feedback(); }                
                public void changedUpdate(DocumentEvent e) {}
            };
                        
            getDocument().addDocumentListener(adapter);            
            
        } else if (old != null && validator == null) {
            
            // remove attached validator
            
            getDocument().removeDocumentListener(adapter);
                                    
            adapter = null;
        }
        
        feedback();
        
    }

    //
    // depending on validity set foreground color and tooltip
    //
    private void feedback() {
        
        // document callback can come from whatever thread place into AWT thread
        
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if (validator == null || validator.isValid(getText())) {
                    ValidatingTextField.super.setToolTipText(tooltip);
                    ValidatingTextField.this.setForeground(Color.black);
                } else {
                    String reason = validator.getReason();
                    ValidatingTextField.super.setToolTipText(reason == null ?
                        NbBundle.getMessage(ValidatingTextField.class, "MSG_invalid") : reason);
                    ValidatingTextField.this.setForeground(Color.red);
                }        
            }
        });
    }
    
    // ~~~~~~ ComboBoxEditor interface implementation ~~~~~~~~~~~~~~~~~~~~
    
    public java.lang.Object getItem() {
        return getText();
    }
        
    public void setItem(java.lang.Object obj) {
        setText((String) obj);
    }
            
    public java.awt.Component getEditorComponent() {
        return this;
    }

    /*
     * Set new tooltip that will be displyed whenever entered value is valid.
     */    
    public void setToolTipText(String text) {
        tooltip = text;
        feedback();
    }
    
    /*
     * Reentarant validator of entered value.
     * It can provide reason of invalidity.
     */
    public static interface Validator {
        
        /*
         * Test the passed value returning false on invalidity.
         */
        public boolean isValid(String text);
        
        /**
         * Return invalidity reason on null. It is used for tooltip.
         */
        public String getReason();
    }
   
}

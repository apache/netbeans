/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

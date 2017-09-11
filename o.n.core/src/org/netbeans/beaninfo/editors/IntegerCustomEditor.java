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
package org.netbeans.beaninfo.editors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.openide.explorer.propertysheet.PropertyEnv;


/**
 * This class is result of fix for 
 * IZ#129804 - "Dimensions" dialog does not display warning for the second time.
 * 
 * Custom editors such as PointCustomEditor , InsetsCustomEditor and 
 * RectangleCustomEditor are refactored with inheritance from this class. 
 * @author ads
 *
 */
abstract class IntegerCustomEditor extends JPanel implements
        PropertyChangeListener, KeyListener
{

    private static final String ERROR_FOREGROUND = "nb.errorForeground";    // NOI18N
    
    public IntegerCustomEditor( PropertyEnv env) {
        myEnv = env;
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed( KeyEvent arg0 ) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased( KeyEvent arg0 ) {
        if (checkValues()) {
            updateValues();
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped( KeyEvent arg0 ) {
    }
    
    protected abstract void updateValues() ;
    
    protected void setPanel( JPanel panel ){
        myPanel = panel;
    }
    
    protected HashMap<JTextField,JLabel> getMap(){
        return myLabelMap;
    }
    
    protected boolean validFor(JTextField c) {
        String s = c.getText().trim();
        try {
            Integer.parseInt(s);
            handleValid(c);
            return true;
        } catch (NumberFormatException e) {
            handleInvalid(c);
            return false;
        }
    }
    
    protected void handleValid(JTextField field) {
        field.setForeground(getForeground());
        getMap().get(field).setForeground(getForeground());
    }
    
    protected void handleInvalid(JTextField field) {
        field.setForeground(getErrorColor());
        getMap().get(field).setForeground(getErrorColor());
    }
    
    private Color getErrorColor() {
        Color c=UIManager.getColor(ERROR_FOREGROUND);
        if (c == null) {
            c = Color.RED;
        }
        return c;
    }
    
    private PropertyEnv getEnv(){
        return myEnv;
    }
    
    private boolean checkValues() {
        Component[] components = myPanel.getComponents();
        boolean valid=true;
        for (int i=0; i < components.length; i++) {
            if (components[i] instanceof JTextField) {
                valid &= validFor((JTextField) components[i]);
            }
        }
        if (getEnv() != null) {
            getEnv().setState(valid ? PropertyEnv.STATE_VALID : 
                PropertyEnv.STATE_INVALID);
        }
        return valid;
    }
    
    private JPanel myPanel;
    private PropertyEnv myEnv;
    private HashMap<JTextField,JLabel> myLabelMap 
        = new HashMap<JTextField,JLabel>();
    
}

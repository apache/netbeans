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

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

import java.beans.*;
import java.text.MessageFormat;
import org.netbeans.core.UIExceptions;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * Abstract class represents Editor for Wrappers of 8 known primitive types
 * (Byte, Short, Integer, Long, Boolean, Float, Double, Character)
 *
 * @author  Josef Kozak
 */
public abstract class WrappersEditor implements ExPropertyEditor {

    protected PropertyEditor pe = null;
    
    public WrappersEditor(Class type) {
        super();
        pe = PropertyEditorManager.findEditor(type);
    }
    
    public void setValue(Object newValue) throws IllegalArgumentException {
        pe.setValue(newValue);
    }
    
    public Object getValue() {
	return pe.getValue();
    }        
    
    public boolean isPaintable() {
	return pe.isPaintable();
    }

    public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
        pe.paintValue(gfx, box);
    }        
    
    public String getAsText () {
        if ( pe.getValue() == null )
            return "null";              // NOI18N
        return pe.getAsText();
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if ( "null".equals( text ) )    // NOI18N
            return;
        try {
            pe.setAsText(text);
        } catch (Exception e) {
            //Reasonable to assume any exceptions from core/jdk editors are legit
            IllegalArgumentException iae = new IllegalArgumentException (e.getMessage());
            String msg = e.getLocalizedMessage();
            if (msg == null || e.getMessage().equals(msg)) {
                msg = MessageFormat.format(
                NbBundle.getMessage(
                    WrappersEditor.class, "FMT_EXC_GENERIC_BAD_VALUE"), text); //NOI18N
            }
            UIExceptions.annotateUser(iae, iae.getMessage(), msg, e,
                                     new java.util.Date());
            throw iae;
        }
    }
    
    public String[] getTags() {
	return pe.getTags();
    }
    
    public java.awt.Component getCustomEditor() {
	return pe.getCustomEditor();
    }

    public boolean supportsCustomEditor() {
	return pe.supportsCustomEditor();
    }
  
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        pe.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        pe.removePropertyChangeListener(listener);
    }    
    
    public void attachEnv(PropertyEnv env) {
        //Delegate if the primitive editor is an ExPropertyEditor -
        //boolean and int editors will be
        if (pe instanceof ExPropertyEditor) {
            ((ExPropertyEditor) pe).attachEnv (env);
        }
    }
}

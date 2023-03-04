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

package org.netbeans.core.output2.ui;

import org.openide.util.WeakListeners;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Action which delegates to a weakly referenced original.
 *
 * @author  Tim Boudreau
 */
class WeakAction implements Action, PropertyChangeListener {
    private Reference<Action> original;
    private Icon icon;
    private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    private String name = null;

    /** Creates a new instance of WeakAction */
    public WeakAction(Action original) {
        wasEnabled = original.isEnabled();
        icon = (Icon) original.getValue (SMALL_ICON);
        name = (String) original.getValue (NAME);
        this.original = new WeakReference<Action> (original);
        original.addPropertyChangeListener(WeakListeners.propertyChange(this, original));
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        Action orig = getOriginal();
        if (orig != null) {
            orig.actionPerformed (actionEvent);
        }
    }
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener pce) {
        listeners.add (pce);
    }
    
    public Object getValue(String str) {
        if (SMALL_ICON.equals(str)) {
            return icon;
        } else {
            Action orig = getOriginal();
            if (orig != null) {
                return orig.getValue(str);
            } else if (NAME.equals(str)) {
                //Avoid NPE if action is disposed but shown in popup
                return name;
            }
        }
        return null;
    }
    
    private boolean wasEnabled = true;
    public boolean isEnabled() {
        Action orig = getOriginal();
        if (orig != null) {
            wasEnabled = orig.isEnabled();
            return wasEnabled;
        }
        return false;
    }
    
    public void putValue(String str, Object obj) {
        if (SMALL_ICON.equals(str)) {
            icon = (Icon) obj;
        } else {
            Action orig = getOriginal();
            if (orig != null) {
                orig.putValue(str, obj);
            }
        }
    }
    
    public synchronized void removePropertyChangeListener(PropertyChangeListener pce) {
        listeners.remove (pce);
    }
    
    public void setEnabled(boolean val) {
        Action orig = getOriginal();
        if (orig != null) {
            orig.setEnabled(val);
        }
    }
    
    private boolean hadOriginal = true;
    private Action getOriginal() {
        Action result = original.get();
        if (result == null && hadOriginal && wasEnabled) {
            hadOriginal = false;
            firePropertyChange ("enabled", Boolean.TRUE, Boolean.FALSE); //NOI18N
        }
        return result;
    }
    
    private synchronized void firePropertyChange(String nm, Object old, Object nue) {
        PropertyChangeEvent pce = new PropertyChangeEvent (this, nm, old, nue);
        for (PropertyChangeListener pcl: listeners) {
            pcl.propertyChange(pce);
        }
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent pce) {
        firePropertyChange (pce.getPropertyName(), pce.getOldValue(), 
            pce.getNewValue());
        if ("enabled".equals(pce.getPropertyName())) { //NOI18n
            wasEnabled = Boolean.TRUE.equals(pce.getNewValue());
       }
    }
    
}

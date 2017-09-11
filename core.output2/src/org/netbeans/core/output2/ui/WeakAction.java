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

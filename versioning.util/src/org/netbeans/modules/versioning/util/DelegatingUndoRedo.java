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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.awt.UndoRedo;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import java.util.*;

/**
 * Delegates UndoRedo to the currently active component's UndoRedo.

 * @author Maros Sandor
 */
public class DelegatingUndoRedo implements UndoRedo, ChangeListener, PropertyChangeListener {

    private List<ChangeListener> listeners = new ArrayList<ChangeListener>(2);
        
    private UndoRedo delegate = UndoRedo.NONE;
    private JComponent comp = null;

    public void setDiffView(JComponent componentDelegate) {
        if (componentDelegate == null) {
            setDelegate(UndoRedo.NONE);
        } else {
            if (comp != null) {
                comp.removePropertyChangeListener(this);
            }
            comp = componentDelegate;
            comp.addPropertyChangeListener(this);
            UndoRedo delegate = (UndoRedo) componentDelegate.getClientProperty(UndoRedo.class);
            if (delegate == null) delegate = UndoRedo.NONE; 
            setDelegate(delegate);
        }
    }

    private void setDelegate(UndoRedo newDelegate) {
        if (newDelegate == delegate) return;
        delegate.removeChangeListener(this);
        delegate = newDelegate;
        stateChanged(new ChangeEvent(this));
        delegate.addChangeListener(this);
    }
        
    public void stateChanged(ChangeEvent e) {
        List<ChangeListener> currentListeners;
        synchronized(this) {
            currentListeners = listeners;
        }
        for (ChangeListener listener : currentListeners) {
            listener.stateChanged(e);
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (UndoRedo.class.toString().equals(evt.getPropertyName())) {
            setDiffView(comp);
        }
    }

    public boolean canUndo() {
        return delegate.canUndo();
    }

    public boolean canRedo() {
        return delegate.canRedo();
    }

    public void undo() throws CannotUndoException {
        delegate.undo();
    }

    public void redo() throws CannotRedoException {
        delegate.redo();
    }
        
    public synchronized void addChangeListener(ChangeListener l) {
        List<ChangeListener> newListeners = new ArrayList<ChangeListener>(listeners);
        newListeners.add(l);
        listeners = newListeners;
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        List<ChangeListener> newListeners = new ArrayList<ChangeListener>(listeners);
        newListeners.remove(l);
        listeners = newListeners;
    }

    public String getUndoPresentationName() {
        return delegate.getUndoPresentationName();
    }

    public String getRedoPresentationName() {
        return delegate.getRedoPresentationName();
    }
}

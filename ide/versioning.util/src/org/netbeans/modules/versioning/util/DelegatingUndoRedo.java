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

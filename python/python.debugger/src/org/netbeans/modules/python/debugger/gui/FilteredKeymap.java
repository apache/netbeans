/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.python.debugger.gui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.openide.util.RequestProcessor;

/**
 * A keymap that filters ENTER, ESC and TAB, which have special meaning in dialogs
 *
 */
public class FilteredKeymap implements Keymap {

    private final javax.swing.KeyStroke enter = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0);
    private final javax.swing.KeyStroke esc = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
    private final javax.swing.KeyStroke tab = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, 0);
    private final Keymap keyMap; // The original keymap
    
    /** Creates a new instance of FilteredKeymap */
    public FilteredKeymap(final JTextComponent component) {
        
        class KeymapUpdater implements Runnable {
            @Override
            public void run() {
                component.setKeymap(new FilteredKeymap(component));
            }
        }
        
        this.keyMap = component.getKeymap();
        component.addPropertyChangeListener("keymap", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!(evt.getNewValue() instanceof FilteredKeymap)) {
                    // We have to do that lazily, because the property change
                    // is fired *before* the keymap is actually changed!
                    component.removePropertyChangeListener("keymap", this);
                    if (EventQueue.isDispatchThread()) {
                        EventQueue.invokeLater(new KeymapUpdater());
                    } else {
                        RequestProcessor.getDefault().post(new KeymapUpdater(), 100);
                    }
                }
            }
        });
    }
    
    @Override
    public void addActionForKeyStroke(KeyStroke key, Action a) {
        keyMap.addActionForKeyStroke(key, a);
    }
    @Override
    public Action getAction(KeyStroke key) {
        if (enter.equals(key) ||
            esc.equals(key) ||
            tab.equals(key)) {

            return null;
        } else {
            return keyMap.getAction(key);
        }
    }
    @Override
    public Action[] getBoundActions() {
        return keyMap.getBoundActions();
    }
    @Override
    public KeyStroke[] getBoundKeyStrokes() {
        return keyMap.getBoundKeyStrokes();
    }
    @Override
    public Action getDefaultAction() {
        return keyMap.getDefaultAction();
    }
    @Override
    public KeyStroke[] getKeyStrokesForAction(Action a) {
        return keyMap.getKeyStrokesForAction(a);
    }
    @Override
    public String getName() {
        return keyMap.getName()+"_Filtered"; //NOI18N
    }
    @Override
    public javax.swing.text.Keymap getResolveParent() {
        return keyMap.getResolveParent();
    }
    @Override
    public boolean isLocallyDefined(KeyStroke key) {
        if (enter.equals(key) ||
            esc.equals(key) ||
            tab.equals(key)) {
            
            return false;
        } else {
            return keyMap.isLocallyDefined(key);
        }
    }
    @Override
    public void removeBindings() {
        keyMap.removeBindings();
    }
    @Override
    public void removeKeyStrokeBinding(KeyStroke keys) {
        keyMap.removeKeyStrokeBinding(keys);
    }
    @Override
    public void setDefaultAction(Action a) {
        keyMap.setDefaultAction(a);
    }
    @Override
    public void setResolveParent(javax.swing.text.Keymap parent) {
        keyMap.setResolveParent(parent);
    }
    
}
